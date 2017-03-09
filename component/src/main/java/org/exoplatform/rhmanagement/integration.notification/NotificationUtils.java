/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
  
package org.exoplatform.rhmanagement.integration.notification;

import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.rhmanagement.dto.CommentDTO;
import org.exoplatform.rhmanagement.dto.VacationRequestDTO;

import java.util.Set;

public class NotificationUtils {
  public final static ArgumentLiteral<VacationRequestDTO> VACATION_REQUEST = new ArgumentLiteral<VacationRequestDTO>(VacationRequestDTO.class, "vacationRequest");
  public final static ArgumentLiteral<CommentDTO> COMMENT = new ArgumentLiteral<CommentDTO>(CommentDTO.class, "comment");
  public final static String CREATOR = "creator";
  public final static String OWNER = "owner";
  public static final ArgumentLiteral<Set> RECEIVERS = new ArgumentLiteral<Set>(Set.class, "receivers");
  public static final String VACATION_URL = "vacationUrl";
  public final static String COMMENT_TEXT = "commentText";
  public final static String FROM_DATE = "from";
  public final static String TO_DATE = "to";
  public final static String BIRTHDAY_DATE = "birthdayDate";
  public final static String CONTRACT_ANNIV_DATE = "birthdayDate";
  public final static String ACTIVITY_ID = "activityId";

}
