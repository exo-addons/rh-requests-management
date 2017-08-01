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
import org.exoplatform.rhmanagement.entity.BalanceHistoryEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 */
public class BalanceHistoryDAO extends GenericDAOJPAImpl<BalanceHistoryEntity, String> {
    private static final Logger LOG = LoggerFactory.getLogger(BalanceHistoryDAO.class);

    public List<BalanceHistoryEntity> getBalanceHistoryByUserId(String id, String name, long fromDate, long toDate, int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("balanceHistoryEntity.findByUserId", BalanceHistoryEntity.class)
                        .setParameter("userId", id)
                        .setParameter("name", name)
                        .setParameter("fromDate", fromDate)
                        .setParameter("toDate", toDate)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("balanceHistoryEntity.findByUserId", BalanceHistoryEntity.class)
                        .setParameter("userId", id)
                        .setParameter("name", name)
                        .setParameter("fromDate", fromDate)
                        .setParameter("toDate", toDate)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get history with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }


    public List<BalanceHistoryEntity> getBalanceHistoryByDate( long fromDate, long toDate, int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("balanceHistoryEntity.findByDate", BalanceHistoryEntity.class)
                        .setParameter("fromDate", fromDate)
                        .setParameter("toDate", toDate)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("balanceHistoryEntity.findByDate", BalanceHistoryEntity.class)
                        .setParameter("fromDate", fromDate)
                        .setParameter("toDate", toDate)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get history with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

    public  long getBalanceHistoryByUserIdCount(String id) {
        try {
            return getEntityManager().createNamedQuery("balanceHistoryEntity.count", Long.class)
                    .setParameter("userId", id)
                    .getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get history count.", e);
            throw e;
        }
    }



}