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

import org.exoplatform.rhmanagement.dao.ConventionalVacationDAO;
import org.exoplatform.rhmanagement.dto.ConventionalVacationDTO;
import org.exoplatform.rhmanagement.entity.ConventionalVacationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by The eXo Platform SAS
 */
public class ConventionalVacationService {
  private  final Logger LOG = LoggerFactory.getLogger(ConventionalVacationService.class);

  private ConventionalVacationDAO conventionalVacationDAO;


  public ConventionalVacationService() {
    this.conventionalVacationDAO = new ConventionalVacationDAO();
  }

  public ConventionalVacationDTO save(ConventionalVacationDTO entity, boolean newCv) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' is null");
    }

    ConventionalVacationEntity conventionalVacationEntity = null;
    if (newCv) {
      conventionalVacationEntity = conventionalVacationDAO.create(convert(entity));
    } else {
      conventionalVacationEntity = conventionalVacationDAO.update(convert(entity));
    }
    return convert(conventionalVacationEntity);
  }

  public void remove(ConventionalVacationDTO entity) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' = + "+entity+ " or 'entity.id' is null");
    }
    conventionalVacationDAO.delete(convert(entity));
  }

  public List<ConventionalVacationDTO> getConventionalVacations(int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getCommentsByRequestID - Parameter 'offset' must be positive");
    }
    List<ConventionalVacationEntity> entities = conventionalVacationDAO.getConventionalVacations(offset, limit);
    List<ConventionalVacationDTO> dtos = new ArrayList<ConventionalVacationDTO>();
    for (ConventionalVacationEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }
  public long getConventionalVacationsCount() {
    return conventionalVacationDAO.getConventionalVacationsCount();
  }

  public ConventionalVacationDTO getConventionalVacationsById(long id) {

    return convert(conventionalVacationDAO.getConventionalVacationById(id));
  }


  private ConventionalVacationEntity convert(ConventionalVacationDTO dto) {
    ConventionalVacationEntity entity = new ConventionalVacationEntity();
    entity.setId(dto.getId());
    entity.setLabel(dto.getLabel());
    entity.setDaysNumber(dto.getDaysNumber());
    entity.setDescription(dto.getDescription());
    return entity;
  }

  private ConventionalVacationDTO convert(ConventionalVacationEntity entity) {
    ConventionalVacationDTO dto = new ConventionalVacationDTO();
    dto.setId(entity.getId());
    dto.setLabel(entity.getLabel());
    dto.setDaysNumber(entity.getDaysNumber());
    dto.setDescription(entity.getDescription());
    return dto;
  }


}
