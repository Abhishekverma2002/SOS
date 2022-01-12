/*
 * Copyright (C) 2012-2022 52°North Spatial Information Research GmbH
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
package org.n52.sos.ds.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.shetland.ogc.om.values.CategoryValue;
import org.n52.shetland.ogc.swe.simpleType.SweCategory;
import org.n52.shetland.ogc.swe.simpleType.SweText;
import org.n52.sos.ds.hibernate.util.HibernateHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryDAO extends AbstractIdentifierNameDescriptionDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryDAO.class);

    public CategoryDAO(DaoFactory daoFactory) {
        super(daoFactory);
    }

    public CategoryEntity getCategoryForIdentifier(String identifier, Session session) {
        Criteria criteria = session.createCriteria(CategoryEntity.class)
                .add(Restrictions.eq(CategoryEntity.IDENTIFIER, identifier));
        LOGGER.trace("QUERY getCategoryForIdentifier(identifier): {}",
                HibernateHelper.getSqlString(criteria));
        return (CategoryEntity) criteria.uniqueResult();
    }

    public CategoryEntity getOrInsertCategory(String value, Session session) {
        return getOrInsertCategory(value, value, null, session);
    }

    public CategoryEntity getOrInsertCategory(String value, String name, String description, Session session) {
        CategoryEntity category = getCategoryForIdentifier(value, session);
        if (category == null) {
            category = new CategoryEntity();
            addIdentifier(category, value, session);
            addName(category, name, session);
            addDescription(category, description);
            session.save(category);
            session.flush();
            session.refresh(category);
        }
        return category;
    }

    public CategoryEntity getOrInsertCategory(SweText sweText, Session session) {
        CategoryEntity category = getCategoryForIdentifier(sweText.getValue(), session);
        if (category == null) {
            category = new CategoryEntity();
            addIdentifier(category, sweText.getValue(), session);
            addName(category, sweText.getName(), session);
            addDescription(category, sweText.getDescription());
            session.save(category);
            session.flush();
            session.refresh(category);
        }
        return category;
    }

    public CategoryEntity getOrInsertCategory(SweCategory sweCategory, Session session) {
        CategoryEntity category = getCategoryForIdentifier(sweCategory.getValue(), session);
        if (category == null) {
            category = new CategoryEntity();
            addIdentifier(category, sweCategory.getValue(), session);
            addName(category, sweCategory.getName(), session);
            addDescription(category, sweCategory.getDescription());
            session.save(category);
            session.flush();
            session.refresh(category);
        }
        return category;
    }

    public CategoryEntity getOrInsertCategory(CategoryEntity categoryEntity, Session session) {
        CategoryEntity category = getCategoryForIdentifier(categoryEntity.getIdentifier(), session);
        if (category == null) {
            category = categoryEntity;
            session.save(category);
            session.flush();
            session.refresh(category);
        }
        return category;
    }

    public CategoryEntity getOrInsertCategory(PhenomenonEntity obsProp, Session session) {
        CategoryEntity category = getCategoryForIdentifier(obsProp.getIdentifier(), session);
        if (category == null) {
            category = new CategoryEntity();
            category.setIdentifier(obsProp.getIdentifier(), getDaoFactory().isStaSupportsUrls());
            category.setIdentifierCodespace(obsProp.getIdentifierCodespace());
            category.setName(obsProp.getName());
            category.setNameCodespace(obsProp.getNameCodespace());
            category.setDescription(obsProp.getDescription());
            session.save(category);
            session.flush();
            session.refresh(category);
        }
        return category;
    }
}
