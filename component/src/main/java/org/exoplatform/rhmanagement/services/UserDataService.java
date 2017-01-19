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

import org.exoplatform.rhmanagement.dao.UserRHDataDAO;
import org.exoplatform.rhmanagement.dto.EmployeesDTO;
import org.exoplatform.rhmanagement.dto.UserRHDataDTO;
import org.exoplatform.rhmanagement.entity.UserRHDataEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
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
public class UserDataService {
  private  final Logger LOG = LoggerFactory.getLogger(UserDataService.class);

  private UserRHDataDAO userRHDataDAO;

  private IdentityManager identityManager;
  public UserDataService(IdentityManager identityManager) {
    this.userRHDataDAO = new UserRHDataDAO();
    this.identityManager = identityManager;
  }

  public UserRHDataDTO save(UserRHDataDTO entity) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' is null");
    }

    UserRHDataEntity userRHDataEntity = null;
    if (entity == null) {
      userRHDataEntity = userRHDataDAO.create(convert(entity));
    } else {
      userRHDataEntity = userRHDataDAO.update(convert(entity));
    }
    return convert(userRHDataEntity);
  }

  public void remove(UserRHDataDTO entity) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' = + "+entity+ " or 'entity.id' is null");
    }
    userRHDataDAO.delete(convert(entity));
  }

  public UserRHDataDTO getUserRHDataByUserId(String id) {


    return convert(userRHDataDAO.getUserRHDataDAOByUserId(id));
  }


  public List<EmployeesDTO> getAllUsersRhData(int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getAllUsersRhData - Parameter 'offset' must be positive");
    }
    List<UserRHDataEntity> entities = userRHDataDAO.getAllUsersRhData(offset, limit);
    List<EmployeesDTO> dtos = new ArrayList<EmployeesDTO>();
    for (UserRHDataEntity entity : entities) {
      Identity id=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, entity.getUserId(), false);
      if(id!=null){
        EmployeesDTO employeesDTO=new EmployeesDTO();
        employeesDTO.setId(entity.getId());
        employeesDTO.setUserId(entity.getUserId());
        employeesDTO.setName(id.getProfile().getFullName());
        employeesDTO.setCin(entity.getCin());
        employeesDTO.setNbrHolidays(entity.getNbrHolidays());
        employeesDTO.setNbrSickdays(entity.getNbrSickdays());
        employeesDTO.setSocialSecNumber(entity.getSocialSecNumber());
        dtos.add(employeesDTO);
      }
    }
    return dtos;
  }


  private UserRHDataEntity convert(UserRHDataDTO dto) {
    UserRHDataEntity entity = new UserRHDataEntity();
    entity.setId(dto.getId());
    entity.setUserId(dto.getUserId());
    entity.setCin(dto.getCin());
    entity.setNbrHolidays(dto.getNbrHolidays());
    entity.setNbrSickdays(dto.getNbrSickdays());
    entity.setSocialSecNumber(dto.getSocialSecNumber());
    return entity;
  }

  private UserRHDataDTO convert(UserRHDataEntity entity) {
    UserRHDataDTO dto = new UserRHDataDTO();
    dto.setId(entity.getId());
    dto.setUserId(entity.getUserId());
    dto.setCin(entity.getCin());
    dto.setNbrHolidays(entity.getNbrHolidays());
    dto.setNbrSickdays(entity.getNbrSickdays());
    dto.setSocialSecNumber(entity.getSocialSecNumber());
    return dto;
  }

}
