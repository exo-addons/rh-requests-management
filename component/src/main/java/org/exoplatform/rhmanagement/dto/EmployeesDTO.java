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
package org.exoplatform.rhmanagement.dto;

import org.apache.commons.lang.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


  public class EmployeesDTO {

  private  long id;

  private  String userId;

  private  String name;

  private  String avatar;

  private  String type;

  private  String value;

  private long cin;

  private float nbrHolidays;

  private float nbrSickdays;

  private long socialSecNumber;

    public long getId() {
      return id;
    }

    public void setId(long id) {
      this.id = id;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getAvatar() {
      return avatar;
    }

    public void setAvatar(String avatar) {
      this.avatar = avatar;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public long getCin() {
      return cin;
    }

    public void setCin(long cin) {
      this.cin = cin;
    }

    public float getNbrHolidays() {
      return nbrHolidays;
    }

    public void setNbrHolidays(float nbrHolidays) {
      this.nbrHolidays = nbrHolidays;
    }

    public float getNbrSickdays() {
      return nbrSickdays;
    }

    public void setNbrSickdays(float nbrSickdays) {
      this.nbrSickdays = nbrSickdays;
    }

    public long getSocialSecNumber() {
      return socialSecNumber;
    }

    public void setSocialSecNumber(long socialSecNumber) {
      this.socialSecNumber = socialSecNumber;
    }
  }
