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

import java.util.List;

import org.exoplatform.rhmanagement.entity.VacationRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

/**
 * Created by The eXo Platform SAS
 *
 * @author boubaker.khanfir@exoplatform.com
 * @since Apr 27, 2016
 */
public class VacationRequestDAO extends GenericDAOJPAImpl<VacationRequestEntity, String> {
    private static final Logger LOG = LoggerFactory.getLogger(VacationRequestDAO.class);

    public List<VacationRequestEntity> getVacationRequests(int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findAllOrderBy", VacationRequestEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .getResultList();
            } else {
                return findAll();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

    public  long getVacationRequestesCount() {
        try {
            return getEntityManager().createNamedQuery("vacationRequestEntity.count", Long.class).getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests count.", e);
            throw e;
        }
    }


    public List<VacationRequestEntity> getVacationRequestsByUserId(String userId, int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findByUserId", VacationRequestEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("userId", userId)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findByUserId", VacationRequestEntity.class)
                        .setParameter("userId", userId)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }


    public List<VacationRequestEntity> getVacationRequestsByUserIdAndStatus(String userId,String status, int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findByUserIdAndStatus", VacationRequestEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("userId", userId)
                        .setParameter("status", status)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findByUserIdAndStatus", VacationRequestEntity.class)
                        .setParameter("userId", userId)
                        .setParameter("status", status)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }


    public List<VacationRequestEntity> getActiveVacationRequestsByUserId(String userId, int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findActiveByUserId", VacationRequestEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("userId", userId)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findActiveByUserId", VacationRequestEntity.class)
                        .setParameter("userId", userId)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

    public List<VacationRequestEntity> getVacationRequestbyId(long id) {
        try {
            return getEntityManager().createNamedQuery("vacatioRequestEntity.findById", VacationRequestEntity.class)
                    .setParameter("id", id)
                    .getResultList();
        }  catch (Exception e) {
            LOG.warn("Exception while attempting to get request", e);
            throw e;
        }
    }

}