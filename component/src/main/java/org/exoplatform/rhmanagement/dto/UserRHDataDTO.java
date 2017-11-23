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
import lombok.Data;

import java.util.Date;


@Data

  public class UserRHDataDTO {


    private long  Id;

    private String userId;

    private String cin;

    private float holidaysBalance;

    private float sickdaysBalance;

    private String socialSecNumber;

    private String hrId;

    private Date birthDay;

    private String situation;

    private int nbChildren;

    private String team;

    private String address;

    private String bankId;

    private Date startDate;

    private Date leaveDate;

    private String contract;

    private Date contractStartDate;

    private Date contractEndDate;

    private String myeXoUrl;

    private String insuranceId;

    private String others;

    private Date creationDate;

    private Boolean active;

}
