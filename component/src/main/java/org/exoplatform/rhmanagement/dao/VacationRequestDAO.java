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
import org.exoplatform.rhmanagement.entity.VacationRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by The eXo Platform SAS
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
            return getEntityManager().createNamedQuery("vacatioRequestEntity.count", Long.class).getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests count.", e);
            throw e;
        }
    }

    public List<VacationRequestEntity> getActiveVacationRequests(int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findActive", VacationRequestEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("currentDate", new Date(System.currentTimeMillis()-24*60*60*1000))
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findActive", VacationRequestEntity.class)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }



    public List<VacationRequestEntity> getWaitingVacationRequests(int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findWaiting", VacationRequestEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findWaiting", VacationRequestEntity.class)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }


    public  long getActiveVacationRequestsCount() {
        try {
            return getEntityManager().createNamedQuery("vacatioRequestEntity.countActive", Long.class)
                    .setParameter("currentDate", new Date(System.currentTimeMillis()-24*60*60*1000))
                    .getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests count.", e);
            throw e;
        }
    }

    public  long getWaitingVacationRequestsCount() {
        try {
            return getEntityManager().createNamedQuery("vacatioRequestEntity.countWaiting", Long.class)
                    .getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests count.", e);
            throw e;
        }
    }

    public  long getVacationRequestsByTypeCount(String type) {
        try {
            return getEntityManager().createNamedQuery("vacatioRequestEntity.countByType", Long.class)
                    .setParameter("type", type)
                    .getSingleResult();
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

    public List<VacationRequestEntity> getVacationRequestsByType(String type, int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findByType", VacationRequestEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("type", type)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findByType", VacationRequestEntity.class)
                        .setParameter("type", type)
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
                        .setParameter("currentDate", new Date(System.currentTimeMillis()-24*60*60*1000))
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



    public List<VacationRequestEntity> getVacationRequestsByValidator(String userId, int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findByValidator", VacationRequestEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("userId", userId)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findByValidator", VacationRequestEntity.class)
                        .setParameter("userId", userId)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

    public List<VacationRequestEntity> getActiveVacationRequestsByValidator(String userId, int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findActiveByValidator", VacationRequestEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("userId", userId)
                        .setParameter("currentDate", new Date(System.currentTimeMillis()-24*60*60*1000))
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findActiveByValidator", VacationRequestEntity.class)
                        .setParameter("userId", userId)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }


    public List<VacationRequestEntity> getVacationRequestsByValidatorAndStatus(String userId,String status, int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findByValidatorAndStatus", VacationRequestEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("userId", userId)
                        .setParameter("status", status)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findByValidatorAndStatus", VacationRequestEntity.class)
                        .setParameter("userId", userId)
                        .setParameter("status", status)
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

    public List<VacationRequestEntity>  getVacationRequestsbyDate (Date date){
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            Date minDate = cal.getTime();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            Date maxDate = cal.getTime();
            return getEntityManager().createNamedQuery("vacatioRequestEntity.findByDate", VacationRequestEntity.class)
                    .setParameter("minDate", minDate)
                    .setParameter("maxDate", maxDate)
                    .getResultList();
        }  catch (Exception e) {
            LOG.warn("Exception while attempting to get request", e);
            throw e;
        }
    }


    public List<VacationRequestEntity> getActiveVacationRequestsByManager(String managerId, List<String> listSubs, int offset, int limit) {
        try {
           // String listManagers = String.join(",", listSubs);
            if(listSubs.size()==0){
                return new ArrayList<VacationRequestEntity>();
            }
            else if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findByManager", VacationRequestEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("managerId", managerId)
                        .setParameter("listSubs", listSubs)
                        .setParameter("currentDate", new Date(System.currentTimeMillis()-24*60*60*1000))
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("vacatioRequestEntity.findByManager", VacationRequestEntity.class)
                        .setParameter("managerId", managerId)
                        .setParameter("listSubs", listSubs)
                        .setParameter("currentDate", new Date(System.currentTimeMillis()-24*60*60*1000))
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }


}