/*
 * Copyright (C) 2012-2017 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.n52.iceland.ds.ConnectionProvider;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.om.OmObservableProperty;
import org.n52.shetland.ogc.ows.exception.InvalidParameterValueException;
import org.n52.shetland.ogc.ows.exception.NoApplicableCodeException;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.ogc.sensorML.SensorML;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosConstants;
import org.n52.shetland.ogc.sos.SosOffering;
import org.n52.shetland.ogc.sos.SosProcedureDescription;
import org.n52.shetland.ogc.sos.request.InsertSensorRequest;
import org.n52.shetland.ogc.sos.response.InsertSensorResponse;
import org.n52.shetland.ogc.swes.SwesFeatureRelationship;
import org.n52.sos.ds.AbstractInsertSensorHandler;
import org.n52.sos.ds.hibernate.dao.DaoFactory;
import org.n52.sos.ds.hibernate.dao.FeatureOfInterestTypeDAO;
import org.n52.sos.ds.hibernate.dao.ObservablePropertyDAO;
import org.n52.sos.ds.hibernate.dao.ObservationConstellationDAO;
import org.n52.sos.ds.hibernate.dao.ObservationTypeDAO;
import org.n52.sos.ds.hibernate.dao.OfferingDAO;
import org.n52.sos.ds.hibernate.dao.ProcedureDAO;
import org.n52.sos.ds.hibernate.dao.ProcedureDescriptionFormatDAO;
import org.n52.sos.ds.hibernate.dao.RelatedFeatureDAO;
import org.n52.sos.ds.hibernate.dao.RelatedFeatureRoleDAO;
import org.n52.sos.ds.hibernate.dao.ValidProcedureTimeDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ProcedureDescriptionFormat;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.RelatedFeatureRole;
import org.n52.sos.service.operator.ServiceOperatorKey;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * Implementation of the abstract class AbstractInsertSensorHandler
 *
 * @since 4.0.0
 *
 */
public class InsertSensorDAO extends AbstractInsertSensorHandler {

    private HibernateSessionHolder sessionHolder;
    private DaoFactory daoFactory;

    public InsertSensorDAO() {
        super(SosConstants.SOS);
    }

    @Inject
    public void setDaoFactory(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    @Inject
    public void setConnectionProvider(ConnectionProvider connectionProvider) {
        this.sessionHolder = new HibernateSessionHolder(connectionProvider);
    }

    @Override
    public synchronized InsertSensorResponse insertSensor(final InsertSensorRequest request) throws OwsExceptionReport {
        checkForTransactionalEntity();
        final InsertSensorResponse response = new InsertSensorResponse();
        response.setService(request.getService());
        response.setVersion(request.getVersion());
        final String assignedProcedureID = request.getAssignedProcedureIdentifier();
        // we use only the first offering for the response because swes 2.0
        // specifies only one single element
        final SosOffering firstAssignedOffering = request.getFirstAssignedOffering();
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionHolder.getSession();
            transaction = session.beginTransaction();
            final ProcedureDescriptionFormat procedureDescriptionFormat =
                    new ProcedureDescriptionFormatDAO().getOrInsertProcedureDescriptionFormat(
                            request.getProcedureDescriptionFormat(), session);
            if (procedureDescriptionFormat != null)  {
                final Procedure hProcedure =
                        new ProcedureDAO(daoFactory).getOrInsertProcedure(assignedProcedureID, procedureDescriptionFormat,
                                request.getProcedureDescription(), request.isType(),session);
                // TODO: set correct validTime,
                new ValidProcedureTimeDAO(daoFactory).insertValidProcedureTime(
                        hProcedure,
                        procedureDescriptionFormat,
                        getSensorDescriptionFromProcedureDescription(request.getProcedureDescription()), new DateTime(DateTimeZone.UTC), session);
                if (!request.isType()) {
                    final List<ObservationType> observationTypes =
                            new ObservationTypeDAO().getOrInsertObservationTypes(request.getMetadata().getObservationTypes(),
                                    session);
                    final List<FeatureOfInterestType> featureOfInterestTypes =
                            new FeatureOfInterestTypeDAO().getOrInsertFeatureOfInterestTypes(request.getMetadata()
                                    .getFeatureOfInterestTypes(), session);
                    if (observationTypes != null && featureOfInterestTypes != null) {
                        final List<ObservableProperty> hObservableProperties =
                                getOrInsertNewObservableProperties(request.getObservableProperty(), request.getProcedureDescription(), session);
                        final ObservationConstellationDAO observationConstellationDAO = daoFactory.getObservationConstellationDAO();
                        final OfferingDAO offeringDAO = daoFactory.getOfferingDAO();
                        for (final SosOffering assignedOffering : request.getAssignedOfferings()) {
                            final List<RelatedFeature> hRelatedFeatures = new LinkedList<>();
                            if (request.getRelatedFeatures() != null && !request.getRelatedFeatures().isEmpty()) {
                                final RelatedFeatureDAO relatedFeatureDAO = new RelatedFeatureDAO(daoFactory);
                                final RelatedFeatureRoleDAO relatedFeatureRoleDAO = new RelatedFeatureRoleDAO();
                                for (final SwesFeatureRelationship relatedFeature : request.getRelatedFeatures()) {
                                    final List<RelatedFeatureRole> relatedFeatureRoles =
                                            relatedFeatureRoleDAO.getOrInsertRelatedFeatureRole(relatedFeature.getRole(),
                                                    session);
                                    hRelatedFeatures.addAll(relatedFeatureDAO.getOrInsertRelatedFeature(
                                            relatedFeature.getFeature(), relatedFeatureRoles, session));
                                }
                            }
                            final Offering hOffering =
                                    offeringDAO.getAndUpdateOrInsertNewOffering(assignedOffering, hRelatedFeatures, observationTypes,
                                            featureOfInterestTypes, session);
                            for (final ObservableProperty hObservableProperty : hObservableProperties) {
                                observationConstellationDAO.checkOrInsertObservationConstellation(hProcedure,
                                        hObservableProperty, hOffering, assignedOffering.isParentOffering(), session);
                            }
                        }
                        // TODO: parent and child procedures
//                        response.setAssignedProcedure(assignedProcedureID);
//                        response.setAssignedOffering(firstAssignedOffering.getIdentifier());
                    } else {
                        throw new NoApplicableCodeException().withMessage("Error while inserting InsertSensor into database!");
                    }
                }
                response.setAssignedProcedure(assignedProcedureID);
                response.setAssignedOffering(firstAssignedOffering.getIdentifier());
            } else {
                throw new InvalidParameterValueException(Sos2Constants.InsertSensorParams.procedureDescriptionFormat, request.getProcedureDescriptionFormat());
            }
            session.flush();
            transaction.commit();
        } catch (final HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new NoApplicableCodeException().causedBy(he).withMessage(
                    "Error while inserting sensor data into database!");
        } finally {
            sessionHolder.returnSession(session);
        }
        return response;
    }

    /**
     * Create OmObservableProperty objects from observableProperty identifiers
     * and get or insert them into the database
     *
     * @param obsProps
     *            observableProperty identifiers
     * @param sosProcedureDescription 
     * @param session
     *            Hibernate Session
     * @return ObservableProperty entities
     */
    private List<ObservableProperty> getOrInsertNewObservableProperties(final List<String> obsProps,
            SosProcedureDescription sosProcedureDescription, final Session session) {
        final List<OmObservableProperty> observableProperties = new ArrayList<>(obsProps.size());
        for (final String observableProperty : obsProps) {
            OmObservableProperty omObservableProperty = new OmObservableProperty(observableProperty);
            if (sosProcedureDescription.supportsObservablePropertyName()) {
                if (sosProcedureDescription.isSetObservablePropertyNameFor(observableProperty)) {
                    String name = sosProcedureDescription.getObservablePropertyNameFor(observableProperty);
                    if (!Strings.isNullOrEmpty(name)) {
                        omObservableProperty.addName(name);
                    }
                }
            }
            observableProperties.add(omObservableProperty);
        }
        return new ObservablePropertyDAO(daoFactory).getOrInsertObservableProperty(observableProperties, false, session);
    }

    /**
     * Get SensorDescription String from procedure description
     *
     * @param procedureDescription
     *            Procedure description
     * @return SensorDescription String
     */
    private String getSensorDescriptionFromProcedureDescription(SosProcedureDescription<?> procedureDescription ) {
        if (procedureDescription.getProcedureDescription() instanceof SensorML) {
            final SensorML sensorML = (SensorML) procedureDescription.getProcedureDescription();
            // if SensorML is not a wrapper
            if (!sensorML.isWrapper()) {
                return sensorML.getXml();
            }
            // if SensorML is a wrapper and member size is 1
            else if (sensorML.isWrapper() && sensorML.getMembers().size() == 1) {
                return sensorML.getMembers().get(0).getXml();
            } else {
                // TODO: get sensor description for procedure identifier
                return "";
            }
        } else if (procedureDescription.getProcedureDescription() instanceof AbstractFeature) {
            return procedureDescription.getProcedureDescription().getXml();
        } else if (procedureDescription.isSetXml()) {
            return procedureDescription.getXml();
        }
        return "";
    }

    private void checkForTransactionalEntity() throws CodedException {
        if (!HibernateHelper.isEntitySupported(TProcedure.class)) {
            throw new NoApplicableCodeException().withMessage("The transactional database profile is not activated!");
        }
    }
    
    @Override
    public CapabilitiesExtension getExtension() {
        final SosInsertionCapabilities insertionCapabilities = new SosInsertionCapabilities();
        insertionCapabilities.addFeatureOfInterestTypes(getCache().getFeatureOfInterestTypes());
        insertionCapabilities.addObservationTypes(getCache().getObservationTypes());
        insertionCapabilities.addProcedureDescriptionFormats(CodingRepository.getInstance()
                .getSupportedTransactionalProcedureDescriptionFormats(SosConstants.SOS, Sos2Constants.SERVICEVERSION));
        return insertionCapabilities;
    }

    @Override
    public CapabilitiesExtensionKey getCapabilitiesExtensionKey() {
        return new CapabilitiesExtensionKey(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
    }

    @Override
    public boolean hasRelatedOperation() {
        return true;
    }

    @Override
    public String getRelatedOperation() {
        return getOperationName();
    }
    
    @Override
    public boolean isSupported() {
        return HibernateHelper.isEntitySupported(ValidProcedureTime.class);
    }

}
