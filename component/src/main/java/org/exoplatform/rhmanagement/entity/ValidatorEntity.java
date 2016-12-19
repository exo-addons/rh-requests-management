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
 *
 * @author boubaker.khanfir@exoplatform.com
 * @since Apr 27, 2016
 */
@Entity(name = "HRValidatorEntity")
@ExoEntity
@Table(name = "VALIDATOR")
@NamedQueries({
        @NamedQuery(name = "validatorEntity.findAllByRequestId", query = "SELECT a FROM HRValidatorEntity a where a.requestId = :requestId"),
        @NamedQuery(name = "validatorEntity.count", query = "SELECT count(a.id) FROM HRValidatorEntity a  where a.requestId = :requestId"),
        @NamedQuery(name = "validatorEntity.findAllByValidatorUserId", query = "SELECT a FROM HRValidatorEntity a where a.validatorUserId = :validatorUserId"),
        @NamedQuery(name = "validatorEntity.finByValidatorUserIdandRequestId", query = "SELECT a FROM HRValidatorEntity a where a.validatorUserId = :validatorUserId and a.requestId = :requestId")
})

@Data
public class ValidatorEntity {

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "SEQ_RH_VACATION_REQUEST_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_RH_VACATION_REQUEST_ID")
  private long Id;

  @Column(name = "REQUEST_ID", nullable = false)
  private long requestId;

  @Column(name = "USER_ID")
  private String userId;

  @Column(name = "VALIDATOR_USER_ID")
  private String   validatorUserId;

  @Column(name = "VALIDATOR_EMAIL")
  private String   validatorMail;

  @Column(name = "REPLY")
  private String   reply;
}