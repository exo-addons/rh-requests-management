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

@Data
  @AllArgsConstructor
  @NoArgsConstructor
  public class EmployeesDTO {

    public EmployeesDTO(String id, String value) {
      this.id = id;
      this.value = value;
    }

    String id;

    String name;

    String avatar;

    String type;

    String value;

    public String computeId() {
      if (!StringUtils.isBlank(id)) {
        id = id.replace("@", "");
      }
      return id;
    }


}
