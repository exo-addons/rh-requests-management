/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.rhmanagement.dao;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.rhmanagement.entity.ConventionalVacationEntity;
import org.exoplatform.rhmanagement.entity.ValidatorEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 */
public class ConventionalVacationDAO extends GenericDAOJPAImpl<ConventionalVacationEntity, String> {
    private static final Logger LOG = LoggerFactory.getLogger(ConventionalVacationDAO.class);

    public List<ConventionalVacationEntity> getConventionalVacations(int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("conventionalVacationEntity.findAll", ConventionalVacationEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("conventionalVacationEntity.findAll", ConventionalVacationEntity.class)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get Conventional vacations with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

    public  long getConventionalVacationsCount() {
        try {
            return getEntityManager().createNamedQuery("conventionalVacationEntity.count", Long.class).getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get Conventional vacations count.", e);
            throw e;
        }
    }

    public ConventionalVacationEntity getConventionalVacationById (long id) {
        try {
                return getEntityManager().createNamedQuery("conventionalVacationEntity.findConventionalVacationbyId", ConventionalVacationEntity.class)
                        .setParameter("id", id)
                        .getSingleResult();

        } catch (Exception e) {
            LOG.warn("Exception while attempting to get Conventional vacation ", e);
            throw e;
        }
    }




}