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
import org.exoplatform.rhmanagement.dao.ValidatorDAO;
import org.exoplatform.rhmanagement.dto.CommentDTO;
import org.exoplatform.rhmanagement.dto.ValidatorDTO;
import org.exoplatform.rhmanagement.entity.CommentEntity;
import org.exoplatform.rhmanagement.entity.ValidatorEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by The eXo Platform SAS
 *
 * @author boubaker.khanfir@exoplatform.com
 * @since Apr 27, 2016
 */
public class ValidatorService {
  private  final Logger LOG = LoggerFactory.getLogger(ValidatorService.class);

  private ValidatorDAO validatorDAO;


  public ValidatorService() {
    this.validatorDAO = new ValidatorDAO();
  }

  public ValidatorDTO save(ValidatorDTO entity) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' is null");
    }

    ValidatorEntity commentEntity = null;
    if (entity == null) {
      commentEntity = validatorDAO.create(convert(entity));
    } else {
      commentEntity = validatorDAO.update(convert(entity));
    }
    return convert(commentEntity);
  }

  public void remove(ValidatorDTO entity) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' = + "+entity+ " or 'entity.id' is null");
    }
    validatorDAO.delete(convert(entity));
  }

  public List<ValidatorDTO> getValidatorsByRequestId(long id, int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getCommentsByRequestID - Parameter 'offset' must be positive");
    }
    List<ValidatorEntity> entities = validatorDAO.getValidatorsByRequestId(id, offset, limit);
    List<ValidatorDTO> dtos = new ArrayList<ValidatorDTO>();
    for (ValidatorEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }
  public long getValidatorsByRequestIdCount(Long id) {
    return validatorDAO.getValidatorsByRequestIdCount(id);
  }

  public List<ValidatorDTO> getValidatorsByValidatorUserId(String userId, int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getCommentsByRequestID - Parameter 'offset' must be positive");
    }
    List<ValidatorEntity> entities = validatorDAO.getValidatorsByValidatorUserId(userId, offset, limit);
    List<ValidatorDTO> dtos = new ArrayList<ValidatorDTO>();
    for (ValidatorEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }

  public List<ValidatorDTO> getValidatorsByValidatorUserIdandRequestId(String userId,long requestId) {
    List<ValidatorEntity> entities = validatorDAO.getValidatorsByValidatorUserIdandRequestId(userId, requestId);
    List<ValidatorDTO> dtos = new ArrayList<ValidatorDTO>();
    for (ValidatorEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }


  private ValidatorEntity convert(ValidatorDTO dto) {
    ValidatorEntity entity = new ValidatorEntity();
    entity.setId(dto.getId());
    entity.setUserId(dto.getUserId());
    entity.setValidatorUserId(dto.getValidatorUserId());
    entity.setRequestId(dto.getRequestId());
    entity.setValidatorMail(dto.getValidatorMail());
    entity.setReply(dto.getReply());
    return entity;
  }

  private ValidatorDTO convert(ValidatorEntity entity) {
    ValidatorDTO dto = new ValidatorDTO();
    dto.setId(entity.getId());
    dto.setUserId(entity.getUserId());
    dto.setValidatorUserId(entity.getValidatorUserId());
    dto.setRequestId(entity.getRequestId());
    dto.setValidatorMail(entity.getValidatorMail());
    dto.setReply(entity.getReply());
    return dto;
  }



  private String              validatorMail;
  private String              reply;
}
