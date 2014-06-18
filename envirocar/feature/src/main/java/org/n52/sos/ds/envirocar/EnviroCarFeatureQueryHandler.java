/**
 * Copyright (C) 2012-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.sos.ds.envirocar;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.n52.sos.ds.FeatureQueryHandler;
import org.n52.sos.ds.HibernateDatasourceConstants;
import org.n52.sos.ds.hibernate.HibernateSessionHolder;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.gml.AbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnviroCarFeatureQueryHandler implements FeatureQueryHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EnviroCarFeatureQueryHandler.class);

    @Override
    public AbstractFeature getFeatureByID(String featureID, Object connection, String version, int responseSrid)
            throws OwsExceptionReport {
        final Session session = HibernateSessionHolder.getSession(connection);
        return null;
    }

    @Override
    public Collection<String> getFeatureIDs(SpatialFilter filter, Object connection) throws OwsExceptionReport {
        final Session session = HibernateSessionHolder.getSession(connection);
        return null;
    }

    @Override
    public Map<String, AbstractFeature> getFeatures(Collection<String> foiIDs, List<SpatialFilter> list,
            Object connection, String version, int responseSrid) throws OwsExceptionReport {
        final Session session = HibernateSessionHolder.getSession(connection);
        return null;
    }

    @Override
    public SosEnvelope getEnvelopeForFeatureIDs(Collection<String> featureIDs, Object connection)
            throws OwsExceptionReport {
        final Session session = HibernateSessionHolder.getSession(connection);
        return null;
    }

    @Override
    public String insertFeature(SamplingFeature samplingFeature, Object connection) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getConnectionProviderIdentifier() {
        return HibernateDatasourceConstants.OGM_CONNECTION_PROVIDER_IDENTIFIER;
    }

    @Override
    public int getDefaultEPSG() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getDefault3DEPSG() {
        // TODO Auto-generated method stub
        return 0;
    }

}
