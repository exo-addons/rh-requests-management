/*
 *
 *  * Copyright (C) 2003-2016 eXo Platform SAS.
 *  *
 *  * This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 */
package org.exoplatform.rhmanagement.services;

import org.exoplatform.rhmanagement.dao.CommentDAO;
import org.exoplatform.rhmanagement.dto.CommentDTO;
import org.exoplatform.rhmanagement.entity.CommentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by The eXo Platform SAS
 */
public class CommentService {
  private  final Logger LOG = LoggerFactory.getLogger(CommentService.class);

  private CommentDAO commentDAO;


  public CommentService() {
    this.commentDAO = new CommentDAO();
  }

  public CommentDTO save(CommentDTO entity) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' is null");
    }

    CommentEntity commentEntity = null;
    if (entity == null) {
      entity.setPostedTime(System.currentTimeMillis());
      commentEntity = commentDAO.create(convert(entity));
    } else {
      commentEntity = commentDAO.update(convert(entity));
    }
    return convert(commentEntity);
  }

  public void remove(CommentDTO entity) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' = + "+entity+ " or 'entity.id' is null");
    }
    commentDAO.delete(convert(entity));
  }

  public List<CommentDTO> getCommentsByRequestId(long id, String commentType, int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getCommentsByRequestID - Parameter 'offset' must be positive");
    }
    List<CommentEntity> entities = commentDAO.getCommentsByRequestId(id,commentType, offset, limit);
    List<CommentDTO> dtos = new ArrayList<CommentDTO>();
    for (CommentEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }


  public long getCommentsByRequestIdCount(Long id, String commentType) {
    return commentDAO.getCommentsByRequestIdCount(id, commentType);
  }

  private CommentEntity convert(CommentDTO dto) {
    CommentEntity entity = new CommentEntity();
    entity.setId(dto.getId());
    entity.setPosterId(dto.getPosterId());
    entity.setPostedTime(dto.getPostedTime());
    entity.setRequestId(dto.getRequestId());
    entity.setCommentText(dto.getCommentText());
    entity.setCommentType(dto.getCommentType());
    return entity;
  }

  private CommentDTO convert(CommentEntity entity) {
    CommentDTO dto = new CommentDTO();
    dto.setId(entity.getId());
    dto.setPosterId(entity.getPosterId());
    dto.setPostedTime(entity.getPostedTime());
    dto.setRequestId(entity.getRequestId());
    dto.setCommentText(entity.getCommentText());
    dto.setCommentType(entity.getCommentType());
    return dto;
  }

}
