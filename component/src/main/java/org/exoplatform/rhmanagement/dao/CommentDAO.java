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
import org.exoplatform.rhmanagement.entity.CommentEntity;
import org.exoplatform.rhmanagement.entity.VacationRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 *
 * @author boubaker.khanfir@exoplatform.com
 * @since Apr 27, 2016
 */
public class CommentDAO extends GenericDAOJPAImpl<CommentEntity, String> {
    private static final Logger LOG = LoggerFactory.getLogger(CommentDAO.class);

    public List<CommentEntity> getCommentsByRequestId(long id, String commentType, int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("commentEntity.findByRequestId", CommentEntity.class)
                        .setParameter("requestId", id)
                        .setParameter("commentType", commentType)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("commentEntity.findByRequestId", CommentEntity.class)
                        .setParameter("requestId", id)
                        .setParameter("commentType", commentType)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get comments with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

    public  long getCommentsByRequestIdCount(long id, String commentType) {
        try {
            return getEntityManager().createNamedQuery("commentEntity.count", Long.class)
                    .setParameter("requesId", id)
                    .setParameter("commentType", commentType)
                    .getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get comments count.", e);
            throw e;
        }
    }



}