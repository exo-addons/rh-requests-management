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
@Entity(name = "HRVacatioRequestEntity")
@ExoEntity
@Table(name = "VACATION_REQUEST")
@NamedQueries({
        @NamedQuery(name = "vacatioRequestEntity.findAllOrderBy", query = "SELECT a FROM HRVacatioRequestEntity a order by a.id desc"),
        @NamedQuery(name = "vacatioRequestEntity.count", query = "SELECT count(a.id) FROM HRVacatioRequestEntity a"),
        @NamedQuery(name = "vacatioRequestEntity.findByUserId", query = "SELECT a FROM HRVacatioRequestEntity a where a.userId = :userId order by a.id desc"),
        @NamedQuery(name = "vacatioRequestEntity.findByUserIdAndStatus", query = "SELECT a FROM HRVacatioRequestEntity a where a.userId = :userId and a.status = :status  order by a.id desc"),
        @NamedQuery(name = "vacatioRequestEntity.findById", query = "SELECT a FROM HRVacatioRequestEntity a where a.id = :id") })
@Data
public class VacationRequestEntity {

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "SEQ_RH_VACATION_REQUEST_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_RH_VACATION_REQUEST_ID")
  private long id;

  @Column(name = "USER_ID", nullable = false)
  private String userId;

  @Column(name = "USER_FULL_NAME")
  private String userFullName;

  @Column(name = "DAYS_NUMBER")
  private int   daysNumber;

  @Column(name = "FROM_DATE")
  private Date fromDate;

  @Column(name = "TO_DATE")
  private Date toDate;

  @Column(name = "REASON")
  private String   reason;

  @Column(name = "ANNUAL")
  private boolean annual;

  @Column(name = "SUBSTITUTE")
  private String   substitute;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "TYPE")
  private String   type;

}