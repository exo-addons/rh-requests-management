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

import org.exoplatform.rhmanagement.dao.VacationRequestDAO;
import org.exoplatform.rhmanagement.dto.VacationRequestDTO;
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
public class VacationRequestService {
  private  final Logger LOG = LoggerFactory.getLogger(VacationRequestService.class);


  private VacationRequestDAO      vacationRequestDAO;
 // private ListenerService listenerService = CommonsUtils.getService(ListenerService.class);


  public VacationRequestService() {
    this.vacationRequestDAO = new VacationRequestDAO();
  }

  public VacationRequestDTO save(VacationRequestDTO entity, boolean newRequest) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' is null");
    }

    VacationRequestEntity  vacationRequestEntity = null;
   if (newRequest) {
      entity.setCreationDate(new Date());
      /*     entity.setCreationDate(new Date());
     Calendar cal=Calendar.getInstance();
     cal.setTime(entity.getFromDate());
     cal.set(Calendar.HOUR,8);
     entity.setFromDate(cal.getTime());
     cal.setTime(entity.getToDate());
     cal.set(Calendar.HOUR,18);
     entity.setToDate(cal.getTime());*/
      vacationRequestEntity = vacationRequestDAO.create(convert(entity));
    } else {
      vacationRequestEntity = vacationRequestDAO.update(convert(entity));
    }
    return convert(vacationRequestEntity);
  }

  public void remove(VacationRequestDTO entity) {
   if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' = + "+entity+ " or 'entity.id' is null");
    }
    vacationRequestDAO.delete(convert(entity));
  }

  public List<VacationRequestDTO> getVacationRequests(int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getVacationRequests - Parameter 'offset' must be positive");
    }
    List<VacationRequestEntity> entities = vacationRequestDAO.getVacationRequests(offset, limit);
    List<VacationRequestDTO> dtos = new ArrayList<VacationRequestDTO>();
    for (VacationRequestEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }

  public List<VacationRequestDTO> getActivVacationRequests(int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getVacationRequests - Parameter 'offset' must be positive");
    }
    List<VacationRequestEntity> entities = vacationRequestDAO.getActiveVacationRequests(offset, limit);
    List<VacationRequestDTO> dtos = new ArrayList<VacationRequestDTO>();
    for (VacationRequestEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }

  public List<VacationRequestDTO> getWaitingVacationRequests(int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getVacationRequests - Parameter 'offset' must be positive");
    }
    List<VacationRequestEntity> entities = vacationRequestDAO.getWaitingVacationRequests(offset, limit);
    List<VacationRequestDTO> dtos = new ArrayList<VacationRequestDTO>();
    for (VacationRequestEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }

  public List<VacationRequestDTO> getVacationRequestsByType(String type, int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getVacationRequests - Parameter 'offset' must be positive");
    }
    List<VacationRequestEntity> entities = vacationRequestDAO.getVacationRequestsByType(type, offset, limit);
    List<VacationRequestDTO> dtos = new ArrayList<VacationRequestDTO>();
    for (VacationRequestEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }

  public VacationRequestDTO getVacationRequest(long id) {
    List<VacationRequestEntity> entities=vacationRequestDAO.getVacationRequestbyId(id);
    if (entities.size()!=0){
      return convert(entities.get(0));
    }
    return null;
  }


  public List<VacationRequestDTO> getVacationRequestsByUserId(String userId,  int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getVacationRequests - Parameter 'offset' must be positive");
    }
    List<VacationRequestEntity> entities = vacationRequestDAO.getVacationRequestsByUserId(userId,offset, limit);
    List<VacationRequestDTO> dtos = new ArrayList<VacationRequestDTO>();
    for (VacationRequestEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }


  public List<VacationRequestDTO> getVacationRequestsByUserIdAndStatus(String userId,String status, int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getVacationRequests - Parameter 'offset' must be positive");
    }
    List<VacationRequestEntity> entities = vacationRequestDAO.getVacationRequestsByUserIdAndStatus(userId,status,offset, limit);
    List<VacationRequestDTO> dtos = new ArrayList<VacationRequestDTO>();
    for (VacationRequestEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }

  public List<VacationRequestDTO> getActiveVacationRequestsByUserId(String userId, int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getVacationRequests - Parameter 'offset' must be positive");
    }
    List<VacationRequestEntity> entities = vacationRequestDAO.getActiveVacationRequestsByUserId(userId,offset, limit);
    List<VacationRequestDTO> dtos = new ArrayList<VacationRequestDTO>();
    for (VacationRequestEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }


  public List<VacationRequestDTO> getVacationRequestsByValidator(String userId,  int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getVacationRequests - Parameter 'offset' must be positive");
    }
    List<VacationRequestEntity> entities = vacationRequestDAO.getVacationRequestsByValidator(userId,offset, limit);
    List<VacationRequestDTO> dtos = new ArrayList<VacationRequestDTO>();
    for (VacationRequestEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }


  public List<VacationRequestDTO> getVacationRequestsByValidatorAndStatus(String userId,String status, int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getVacationRequests - Parameter 'offset' must be positive");
    }
    List<VacationRequestEntity> entities = vacationRequestDAO.getVacationRequestsByValidatorAndStatus(userId,status,offset, limit);
    List<VacationRequestDTO> dtos = new ArrayList<VacationRequestDTO>();
    for (VacationRequestEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }

  public List<VacationRequestDTO> getActiveVacationRequestsByValidator(String userId, int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getVacationRequests - Parameter 'offset' must be positive");
    }
    List<VacationRequestEntity> entities = vacationRequestDAO.getActiveVacationRequestsByValidator(userId,offset, limit);
    List<VacationRequestDTO> dtos = new ArrayList<VacationRequestDTO>();
    for (VacationRequestEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }


  public long getVacationRequestesCount() {
    return vacationRequestDAO.getVacationRequestesCount();
  }


  public long getActiveVacationRequestsCount() {
    return vacationRequestDAO.getActiveVacationRequestsCount();
  }


  public long getVacationRequestsByTypeCount(String type) {
    return vacationRequestDAO.getVacationRequestsByTypeCount(type);
  }



  public long getWaitingVacationRequestsCount() {
    return vacationRequestDAO.getWaitingVacationRequestsCount();
  }

  public List<VacationRequestDTO> getVacationRequestByDate(Date date) {

    List<VacationRequestEntity> entities = vacationRequestDAO.getVacationRequestsbyDate(date);
    List<VacationRequestDTO> dtos = new ArrayList<VacationRequestDTO>();
    for (VacationRequestEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }


    public List<VacationRequestDTO> getVacationRequestByManager(String userId, List<String> listSubs, int offset, int limit ) {

    List<VacationRequestEntity> entities = vacationRequestDAO.getActiveVacationRequestsByManager(userId,listSubs,offset, limit);
    List<VacationRequestDTO> dtos = new ArrayList<VacationRequestDTO>();
    for (VacationRequestEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }


  private VacationRequestEntity convert(VacationRequestDTO dto) {
    VacationRequestEntity entity = new VacationRequestEntity();
    entity.setId(dto.getId());
    entity.setUserId(dto.getUserId());
    entity.setUserFullName(dto.getUserFullName());
    entity.setDaysNumber(dto.getDaysNumber());
    entity.setFromDate(dto.getFromDate());
    entity.setToDate(dto.getToDate());
    entity.setReason(dto.getReason());
    entity.setVacationType(dto.getVacationType());
    entity.setSubstitute(dto.getSubstitute());
    entity.setType(dto.getType());
    entity.setStatus(dto.getStatus());
    entity.setCreationDate(dto.getCreationDate());
    return entity;
  }

  private VacationRequestDTO convert(VacationRequestEntity entity) {
    VacationRequestDTO dto = new VacationRequestDTO();

    dto.setId(entity.getId());
    dto.setUserFullName(entity.getUserFullName());
    dto.setUserId(entity.getUserId());
    dto.setDaysNumber(entity.getDaysNumber());
    dto.setFromDate(entity.getFromDate());
    dto.setToDate(entity.getToDate());
    dto.setReason(entity.getReason());
    dto.setVacationType(entity.getVacationType());
    dto.setSubstitute(entity.getSubstitute());
    dto.setType(entity.getType());
    dto.setStatus(entity.getStatus());
    dto.setCreationDate(entity.getCreationDate());

    return dto;
  }

}
