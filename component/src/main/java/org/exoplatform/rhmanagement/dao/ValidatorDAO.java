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
import org.exoplatform.rhmanagement.entity.ValidatorEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 *
 * @author boubaker.khanfir@exoplatform.com
 * @since Apr 27, 2016
 */
public class ValidatorDAO extends GenericDAOJPAImpl<ValidatorEntity, String> {
    private static final Logger LOG = LoggerFactory.getLogger(ValidatorDAO.class);

    public List<ValidatorEntity> getValidatorsByRequestId(long id, int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("validatorEntity.findAllByRequestId", ValidatorEntity.class)
                        .setParameter("requestId", id)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("validatorEntity.findAllByRequestId", ValidatorEntity.class)
                        .setParameter("requestId", id)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get comments with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

    public  long getValidatorsByRequestIdCount(long id) {
        try {
            return getEntityManager().createNamedQuery("validatorEntity.count", Long.class).setParameter("requesId", id).getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get requests count.", e);
            throw e;
        }
    }

    public List<ValidatorEntity> getValidatorsByUserID(String userId) {
        try {
                return getEntityManager().createNamedQuery("validatorEntity.findAllByUsertId", ValidatorEntity.class)
                        .setParameter("userId", userId)
                        .getResultList();

        } catch (Exception e) {
            LOG.warn("Exception while attempting to get validators ", e);
            throw e;
        }
    }


    public List<ValidatorEntity> getValidatorsByValidatorUserId(String userId, int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("validatorEntity.findAllByValidatorUserId", ValidatorEntity.class)
                        .setParameter("validatorUserId", userId)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("validatorEntity.findAllByValidatorUserId", ValidatorEntity.class)
                        .setParameter("validatorUserId", userId)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get validators with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

    public List<ValidatorEntity> getValidatorsByValidatorUserIdandRequestId(String userId,long requestId) {
        try {

                return getEntityManager().createNamedQuery("validatorEntity.finByValidatorUserIdandRequestId", ValidatorEntity.class)
                        .setParameter("validatorUserId", userId)
                        .setParameter("requestId", requestId)
                        .getResultList();

        } catch (Exception e) {
            LOG.warn("Exception while attempting to get validators", e);
            throw e;
        }
    }


}