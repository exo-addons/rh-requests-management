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
package org.exoplatform.rhmanagement.entity;

import lombok.Data;
import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by The eXo Platform SAS
 */
@Entity(name = "HRUserRHDataEntity")
@ExoEntity
@Table(name = "HR_USER_DATA")
@NamedQueries({
        @NamedQuery(name = "userRHDataEntity.findByUserId", query = "SELECT a FROM HRUserRHDataEntity a where a.userId = :userId"),
        @NamedQuery(name = "userRHDataEntity.findSubordonateByUserId", query = "SELECT a FROM HRUserRHDataEntity a where a.functionalManager = :userId OR a.hierarchicalManager = :userId"),
        @NamedQuery(name = "userRHDataEntity.findByStatus", query = "SELECT a FROM HRUserRHDataEntity a where a.active = :active"),
        @NamedQuery(name = "userRHDataEntity.findAll", query = "SELECT a FROM HRUserRHDataEntity a")
})
@Data
public class UserRHDataEntity {

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "SEQ_RH_USER_DATA_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_RH_USER_DATA_ID")
  private long id;

  @Column(name = "USER_ID", nullable = false)
  private String userId;

  @Column(name = "CIN")
  private String   cin;

  @Column(name = "HOLIDAYS_BALANCE")
  private float   holidaysBalance;

  @Column(name = "SICK_DAYS_BALANCE")
  private float   sickdaysBalance;

  @Column(name = "SOC_NUMBER")
  private String   socialSecNumber;

  @Column(name = "HR_ID")
  private String hrId;

  @Column(name = "B_DAY")
  private Date birthDay;

  @Column(name = "SITUATION")
  private String situation;

  @Column(name = "NB_CHILDREN")
  private int nbChildren;

  @Column(name = "TEAM")
  private String team;

  @Column(name = "ADDRESS")
  private String address;;

  @Column(name = "BANK_ID")
  private String bankId;

  @Column(name = "START_DATE")
  private Date startDate;

  @Column(name = "LEAVE_DATE")
  private Date leaveDate;

  @Column(name = "CONTRACT")
  private String contract;

  @Column(name = "CONTRACT_START_DATE")
  private Date contractStartDate;

  @Column(name = "CONTRACT_END_DATE")
  private Date contractEndDate;

  @Column(name = "MY_EXO_URL")
  private String myeXoUrl;

  @Column(name = "INSURANCE_ID")
  private String insuranceId;

  @Column(name = "OTHERS")
  private String others;

  @Column(name = "CREATION_DATE")
  private Date creationDate;

  @Column(name = "ACTIVE")
  private Boolean active;

  @Column(name = "HIERARCHICAL_MANAGER")
  private String hierarchicalManager;

  @Column(name = "FUNCTIONAL_MANAGER")
  private String functionalManager;

}