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
import org.exoplatform.rhmanagement.entity.UserRHDataEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 *
 * @author boubaker.khanfir@exoplatform.com
 * @since Apr 27, 2016
 */
public class UserRHDataDAO extends GenericDAOJPAImpl<UserRHDataEntity, String> {
    private static final Logger LOG = LoggerFactory.getLogger(UserRHDataDAO.class);

    public UserRHDataEntity getUserRHDataDAOByUserId(String userId) {
        try {
                return getEntityManager().createNamedQuery("userRHDataEntity.findByUserId", UserRHDataEntity.class)
                        .setParameter("userId", userId)
                        .getSingleResult();

        } catch (Exception e) {
            LOG.warn("Exception while attempting to get user data ", e);
            throw e;
        }
    }

    public List<UserRHDataEntity> getAllUsersRhData(int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("userRHDataEntity.findAll", UserRHDataEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("userRHDataEntity.findAll", UserRHDataEntity.class)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get rh data with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

}