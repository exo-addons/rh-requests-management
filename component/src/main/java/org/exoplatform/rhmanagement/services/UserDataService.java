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
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Created by The eXo Platform SAS
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
      try {
        UserRHDataDTO userRHDataDTO =getUserRHDataByUserId(entity.getUserId());
        if(userRHDataDTO!=null){
          userRHDataEntity = userRHDataDAO.update(convert(entity));
        }else{
          entity.setCreationDate(new Date());
          userRHDataEntity = userRHDataDAO.create(convert(entity));
        }
      } catch (Exception e) {
        entity.setCreationDate(new Date());
        userRHDataEntity = userRHDataDAO.create(convert(entity));
      }

    return convert(userRHDataEntity);
  }


  public void remove(UserRHDataDTO entity) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' = + "+entity+ " or 'entity.id' is null");
    }
    userRHDataDAO.delete(convert(entity));
  }

  public UserRHDataDTO getUserRHDataByUserId(String id){
    try {
      return convert(userRHDataDAO.getUserRHDataDAOByUserId(id));
    } catch (Exception e) {
      return null;
    }
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
        Profile profile=id.getProfile();
        EmployeesDTO employeesDTO=new EmployeesDTO();
        employeesDTO.setId(entity.getId());
        employeesDTO.setUserId(entity.getUserId());
        employeesDTO.setName(profile.getFullName());
        employeesDTO.setAvatar(profile.getAvatarUrl());
        employeesDTO.setEmail(profile.getEmail());
        employeesDTO.setJobTitle(profile.getPosition());
        employeesDTO.setGender(profile.getGender());
        if (profile.getPhones()!=null && profile.getPhones().size()>0){
          String phones="";
          for (Map<String, String> map : profile.getPhones()) {
            String phone="";
            for (Map.Entry<String, String> entry : map.entrySet()) {
              phone= entry.getValue();
              break;
            }
            phones= phones+phone+" , ";
          }
          employeesDTO.setPhone(phones);
        }else employeesDTO.setPhone("");
        employeesDTO.setHrData(convert(entity));
        dtos.add(employeesDTO);
      }
    }
    return dtos;
  }



  public List<EmployeesDTO> getUsersRhDataByStatus(boolean active, int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getAllUsersRhData - Parameter 'offset' must be positive");
    }
    List<UserRHDataEntity> entities = userRHDataDAO.getUsersRhDataByStatus(active, offset, limit);
    List<EmployeesDTO> dtos = new ArrayList<EmployeesDTO>();
    for (UserRHDataEntity entity : entities) {
      Identity id=identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, entity.getUserId(), false);
      if(id!=null){
        Profile profile=id.getProfile();
        EmployeesDTO employeesDTO=new EmployeesDTO();
        employeesDTO.setId(entity.getId());
        employeesDTO.setUserId(entity.getUserId());
        employeesDTO.setName(profile.getFullName());
        employeesDTO.setAvatar(profile.getAvatarUrl());
        employeesDTO.setEmail(profile.getEmail());
        employeesDTO.setJobTitle(profile.getPosition());
        employeesDTO.setGender(profile.getGender());
        if (profile.getPhones()!=null && profile.getPhones().size()>0){
          String phones="";
          for (Map<String, String> map : profile.getPhones()) {
            String phone="";
            for (Map.Entry<String, String> entry : map.entrySet()) {
              phone= entry.getValue();
              break;
            }
            phones= phones+phone+" , ";
          }
          employeesDTO.setPhone(phones);
        }else employeesDTO.setPhone("");
        employeesDTO.setHrData(convert(entity));
        dtos.add(employeesDTO);
      }
    }
    return dtos;
  }



  public List<UserRHDataDTO> getAllRhData(int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getAllUsersRhData - Parameter 'offset' must be positive");
    }
    List<UserRHDataDTO> dtos = new ArrayList<UserRHDataDTO>();
    for (UserRHDataEntity entity : userRHDataDAO.getAllUsersRhData(offset, limit)) {
        dtos.add(convert(entity));
      }

    return dtos;
  }

  public List<UserRHDataDTO> getRhDataByStatus( boolean active, int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getAllUsersRhData - Parameter 'offset' must be positive");
    }
    List<UserRHDataDTO> dtos = new ArrayList<UserRHDataDTO>();
    for (UserRHDataEntity entity : userRHDataDAO.getUsersRhDataByStatus(active ,offset, limit)) {
      dtos.add(convert(entity));
    }

    return dtos;
  }



  private UserRHDataEntity convert(UserRHDataDTO dto) {
    UserRHDataEntity entity = new UserRHDataEntity();
    entity.setId(dto.getId());
    entity.setUserId(dto.getUserId());
    entity.setCin(dto.getCin());
    entity.setHolidaysBalance(dto.getHolidaysBalance());
    entity.setSickdaysBalance(dto.getSickdaysBalance());
    entity.setSocialSecNumber(dto.getSocialSecNumber());
    entity.setHrId(dto.getHrId());
    entity.setBirthDay(dto.getBirthDay());
    entity.setSituation(dto.getSituation());
    entity.setNbChildren(dto.getNbChildren());
    entity.setTeam(dto.getTeam());
    entity.setBankId(dto.getBankId());
    entity.setStartDate(dto.getStartDate());
    entity.setLeaveDate(dto.getLeaveDate());
    entity.setContract(dto.getContract());
    entity.setContractStartDate(dto.getContractStartDate());
    entity.setContractEndDate(dto.getContractEndDate());
    entity.setInsuranceId(dto.getInsuranceId());
    entity.setMyeXoUrl(dto.getMyeXoUrl());
    entity.setOthers(dto.getOthers());
    entity.setAddress(dto.getAddress());
    entity.setCreationDate(dto.getCreationDate());
    entity.setActive(dto.getActive());
    return entity;
  }

  private UserRHDataDTO convert(UserRHDataEntity entity) {
    UserRHDataDTO dto = new UserRHDataDTO();
    dto.setId(entity.getId());
    dto.setUserId(entity.getUserId());
    dto.setCin(entity.getCin());
    dto.setHolidaysBalance(entity.getHolidaysBalance());
    dto.setSickdaysBalance(entity.getSickdaysBalance());
    dto.setSocialSecNumber(entity.getSocialSecNumber());
    dto.setHrId(entity.getHrId());
    dto.setBirthDay(entity.getBirthDay());
    dto.setSituation(entity.getSituation());
    dto.setNbChildren(entity.getNbChildren());
    dto.setTeam(entity.getTeam());
    dto.setBankId(entity.getBankId());
    dto.setStartDate(entity.getStartDate());
    dto.setLeaveDate(entity.getLeaveDate());
    dto.setContract(entity.getContract());
    dto.setContractStartDate(entity.getContractStartDate());
    dto.setContractEndDate(entity.getContractEndDate());
    dto.setInsuranceId(entity.getInsuranceId());
    dto.setMyeXoUrl(entity.getMyeXoUrl());
    dto.setAddress(entity.getAddress());
    dto.setOthers(entity.getOthers());
    dto.setCreationDate(entity.getCreationDate());
    dto.setActive(entity.getActive());
    return dto;
  }

}
