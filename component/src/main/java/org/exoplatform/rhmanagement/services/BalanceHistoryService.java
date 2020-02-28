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

import org.exoplatform.rhmanagement.dao.BalanceHistoryDAO;
import org.exoplatform.rhmanagement.dto.BalanceHistoryDTO;
import org.exoplatform.rhmanagement.entity.BalanceHistoryEntity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by The eXo Platform SAS
 */
public class BalanceHistoryService {
  private  final Logger LOG = LoggerFactory.getLogger(BalanceHistoryService.class);

  private BalanceHistoryDAO balanceHistoryDAO;
  private IdentityManager identityManager;


  public BalanceHistoryService(IdentityManager identityManager) {
    this.balanceHistoryDAO = new BalanceHistoryDAO();
    this.identityManager = identityManager;
  }

  public BalanceHistoryDTO save(BalanceHistoryDTO entity) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' is null");
    }

    BalanceHistoryEntity balanceHistoryEntity = null;
    /*if (entity == null) {*/
      entity.setUpdateDate(System.currentTimeMillis());
    balanceHistoryEntity = balanceHistoryDAO.create(convert(entity));
/*    } else {
      commentEntity = commentDAO.update(convert(entity));
    }*/
    return convert(balanceHistoryEntity);
  }

  public void remove(BalanceHistoryDTO entity) {
    if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' = + "+entity+ " or 'entity.id' is null");
    }
    balanceHistoryDAO.delete(convert(entity));
  }

  public List<BalanceHistoryDTO> getBalanceHistoryByUserId(String id, long fromDate, long toDate, int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getCommentsByRequestID - Parameter 'offset' must be positive");
    }
    Profile userProfile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, false).getProfile();
    List<BalanceHistoryEntity> entities = balanceHistoryDAO.getBalanceHistoryByUserId(id, userProfile.getFullName(), fromDate, toDate, offset, limit);
    List<BalanceHistoryDTO> dtos = new ArrayList<BalanceHistoryDTO>();
    for (BalanceHistoryEntity entity : entities) {
      Profile profile ;
     if(entity.getUpdaterId()!=null){
        try {
          profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, entity.getUpdaterId()).getProfile();
          entity.setUpdaterId(profile.getFullName());
        } catch (Exception e) {
          LOG.error("cannot get profile of {}"+ entity.getUpdaterId());
        }
      }
      if(entity.getUserId()!=null) {
        try {
          profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, entity.getUserId(), false).getProfile();
        } catch (Exception e) {
          LOG.error("cannot get profile of"+ entity.getUserId());
        }
      }
      dtos.add(convert(entity));
    }
    return dtos;
  }

  public List<BalanceHistoryDTO> getBalanceHistoryByDate(long fromDate, long toDate, int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getCommentsByRequestID - Parameter 'offset' must be positive");
    }
    List<BalanceHistoryEntity> entities = balanceHistoryDAO.getBalanceHistoryByDate(fromDate, toDate, offset, limit);
    List<BalanceHistoryDTO> dtos = new ArrayList<BalanceHistoryDTO>();
    for (BalanceHistoryEntity entity : entities) {
      if(entity.getUpdaterId()!=null){
        try {
          Profile profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, entity.getUpdaterId(), false).getProfile();
          entity.setUpdaterId(profile.getFullName());
        } catch (Exception e) {
          LOG.error("cannot get profile of"+ entity.getUpdaterId());
        }
      }
      if(entity.getUserId()!=null) {
        try {
          Profile profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, entity.getUserId(), false).getProfile();
          entity.setUserId(profile.getFullName());
        } catch (Exception e) {
          LOG.error("cannot get profile of"+ entity.getUserId());
        }
      }
      dtos.add(convert(entity));
    }
    return dtos;
  }


  public long getBalanceHistoryByUserrIdCount(String id) {
    return balanceHistoryDAO.getBalanceHistoryByUserIdCount(id);
  }

  private BalanceHistoryEntity convert(BalanceHistoryDTO dto) {
    BalanceHistoryEntity entity = new BalanceHistoryEntity();
    entity.setId(dto.getId());
    entity.setUserId(dto.getUserId());
    entity.setIntialHolidaysBalance(dto.getIntialHolidaysBalance());
    entity.setIntialSickBalance(dto.getIntialSickBalance());
    entity.setHolidaysBalance(dto.getHolidaysBalance());
    entity.setSickBalance(dto.getSickBalance());
    entity.setUpdateDate(dto.getUpdateDate());
    entity.setVacationType(dto.getVacationType());
    entity.setVacationId(dto.getVacationId());
    entity.setDaysNumber(dto.getDaysNumber());
    entity.setUpdateType(dto.getUpdateType());
    entity.setUpdaterId(dto.getUpdaterId());

    return entity;
  }

  private BalanceHistoryDTO convert(BalanceHistoryEntity entity) {
    BalanceHistoryDTO dto = new BalanceHistoryDTO();
    dto.setId(entity.getId());
    dto.setUserId(entity.getUserId());
    dto.setIntialHolidaysBalance(entity.getIntialHolidaysBalance());
    dto.setIntialSickBalance(entity.getIntialSickBalance());
    dto.setHolidaysBalance(entity.getHolidaysBalance());
    dto.setSickBalance(entity.getSickBalance());
    dto.setUpdateDate(entity.getUpdateDate());
    dto.setVacationType(entity.getVacationType());
    dto.setVacationId(entity.getVacationId());
    dto.setDaysNumber(entity.getDaysNumber());
    dto.setUpdateType(entity.getUpdateType());
    dto.setUpdaterId(entity.getUpdaterId());
    return dto;
  }

}

