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

/**
 * Created by The eXo Platform SAS
 */
@Entity(name = "HRBalanceHistoryEntity")
@ExoEntity
@Table(name = "HR_BALANCE_HISTORY")
@NamedQueries({
        @NamedQuery(name = "balanceHistoryEntity.findByUserId", query = "SELECT a FROM HRBalanceHistoryEntity a where a.userId = :userId or a.userId = :name and a.updateDate > :fromDate and a.updateDate < :toDate  order by a.Id desc"),
        @NamedQuery(name = "balanceHistoryEntity.findByDate", query = "SELECT a FROM HRBalanceHistoryEntity a where  a.updateDate > :fromDate and a.updateDate < :toDate  order by a.Id desc"),
        @NamedQuery(name = "balanceHistoryEntity.count", query = "SELECT count(a.Id) FROM HRBalanceHistoryEntity a  where a.userId = :userId") })
@Data
public class BalanceHistoryEntity {

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "SEQ_RH_COMMENT_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_RH_COMMENT_ID")
  private long Id;

  @Column(name = "USER_ID", nullable = false)
  private String userId;

  @Column(name = "INITIAL_HOLIDAYS_BALANCE")
  private float intialHolidaysBalance;

  @Column(name = "INITIAL_SICK_DAYS_BALANCE")
  private float   intialSickBalance;

  @Column(name = "HOLIDAYS_BALANCE")
  private float holidaysBalance;

  @Column(name = "SICK_DAYS_BALANCE")
  private float   sickBalance;

  @Column(name = "UPDATE_DATE")
  private long   updateDate;

  @Column(name = "VACATION_TYPE")
  private String   vacationType;

  @Column(name = "VACATION_ID")
  private long   vacationId;

  @Column(name = "DAYS_NUMBER")
  private float   daysNumber;

  @Column(name = "UPDATE_TYPE")
  private String   updateType;

  @Column(name = "UPDATERID")
  private String   updaterId;

}