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

import org.exoplatform.rhmanagement.dao.OfficialVacationDAO;
import org.exoplatform.rhmanagement.dto.OfficialVacationDTO;
import org.exoplatform.rhmanagement.entity.OfficialVacationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by The eXo Platform SAS
 */
public class OfficialVacationService {
  private  final Logger LOG = LoggerFactory.getLogger(OfficialVacationService.class);

  private OfficialVacationDAO officialVacationDAO;
  private List<String> officialVacationDays;


  public OfficialVacationService() {
    this.officialVacationDAO = new OfficialVacationDAO();
  }


  public OfficialVacationDTO save(OfficialVacationDTO entity, boolean newCv) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' is null");
    }

    OfficialVacationEntity officialVacationEntity = null;
    if (newCv) {
      officialVacationEntity = officialVacationDAO.create(convert(entity));
    } else {
      officialVacationEntity = officialVacationDAO.update(convert(entity));
    }
    return convert(officialVacationEntity);
  }

  public void remove(OfficialVacationDTO entity) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' = + "+entity+ " or 'entity.id' is null");
    }
    officialVacationDAO.delete(convert(entity));
  }

  public List<OfficialVacationDTO> getOfficialVacations(int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getCommentsByRequestID - Parameter 'offset' must be positive");
    }
    List<OfficialVacationEntity> entities = officialVacationDAO.getOfficialVacations(offset, limit);
    List<OfficialVacationDTO> dtos = new ArrayList<OfficialVacationDTO>();
    for (OfficialVacationEntity entity : entities) {
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(entity.getBeginDate());
      calendar.set(Calendar.HOUR, 12);
      entity.setBeginDate(calendar.getTime());
      calendar.setTime(entity.getEndDate());
      calendar.set(Calendar.HOUR, 12);
      entity.setEndDate(calendar.getTime());
      dtos.add(convert(entity));
    }
    return dtos;
  }
  public long getOfficialVacationsCount() {
    return officialVacationDAO.getOfficialVacationsCount();
  }

  public OfficialVacationDTO getOfficialVacationsById(long id) {

    return convert(officialVacationDAO.getOfficialVacationById(id));
  }


  public List<Date> getOfficialVacationDays() {
    SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
    List<OfficialVacationEntity> entities = officialVacationDAO.getOfficialVacations(0, 0);
    List<Date> days = new ArrayList<Date>();
    for (OfficialVacationEntity entity : entities) {
      if(((int)entity.getDaysNumber())==1){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(entity.getBeginDate());
        calendar.set(Calendar.HOUR, 12);
        days.add(calendar.getTime());
      }else if(((int)entity.getDaysNumber())>1){
        days.addAll(Utils.getDaysBetweenDates(entity.getBeginDate(), entity.getEndDate())) ;
      }
    }
    return days;
  }


  private OfficialVacationEntity convert(OfficialVacationDTO dto) {
    OfficialVacationEntity entity = new OfficialVacationEntity();
    entity.setId(dto.getId());
    entity.setLabel(dto.getLabel());
    entity.setDaysNumber(dto.getDaysNumber());
    entity.setDescription(dto.getDescription());
    entity.setRecurrent(dto.getRecurrent());
    entity.setBeginDate(dto.getBeginDate());
    entity.setEndDate(dto.getEndDate());
    return entity;
  }

  private OfficialVacationDTO convert(OfficialVacationEntity entity) {
    OfficialVacationDTO dto = new OfficialVacationDTO();
    dto.setId(entity.getId());
    dto.setLabel(entity.getLabel());
    dto.setDaysNumber(entity.getDaysNumber());
    dto.setDescription(entity.getDescription());
    dto.setBeginDate(entity.getBeginDate());
    dto.setEndDate(entity.getEndDate());
    return dto;
  }



}
