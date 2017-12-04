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
@Entity(name = "HROfficialVacationEntity")
@ExoEntity
@Table(name = "HR_OFFICIAL_VACATION")
@NamedQueries({
        @NamedQuery(name = "officialVacationEntity.findAll", query = "SELECT a FROM HROfficialVacationEntity a"),
        @NamedQuery(name = "officialVacationEntity.count", query = "SELECT count(a.id) FROM HROfficialVacationEntity a"),
        @NamedQuery(name = "officialVacationEntity.findOfficialVacationbyId", query = "SELECT a FROM HROfficialVacationEntity a where a.id = :id"),
})

@Data
public class OfficialVacationEntity {

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "SEQ_HR_OFFICIAL_VACATION_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_HR_OFFICIAL_VACATION_ID")
  private long id;

  @Column(name = "LABEL", nullable = false)
  private String label;

  @Column(name = "DAYS_NUMBER")
  private int daysNumber;

  @Column(name = "DESCRIPTION")
  private String   description;

  @Column(name = "BEGIN_DATE")
  private Date beginDate;

  @Column(name = "END_DATE")
  private Date endDate;

  @Column(name = "RECURRENT")
  private Boolean   recurrent;

}