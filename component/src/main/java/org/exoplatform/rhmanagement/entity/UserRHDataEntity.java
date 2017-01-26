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
@Table(name = "RH_USER_DATA")
@NamedQueries({
        @NamedQuery(name = "userRHDataEntity.findByUserId", query = "SELECT a FROM HRUserRHDataEntity a where a.userId = :userId"),
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
  private long   cin;

  @Column(name = "NB_HOLIDAYS")
  private float   nbrHolidays;

  @Column(name = "NB_SICK_DAYS")
  private float   nbrSickdays;

  @Column(name = "SOC_NUMBER")
  private long   socialSecNumber;

  @Column(name = "HR_ID")
  private String hrId;

  @Column(name = "B_DAY")
  private Date bDay;

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

  @Column(name = "OTHERS")
  private String others;

}