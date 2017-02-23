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
@Entity(name = "HRCommentEntity")
@ExoEntity
@Table(name = "COMMENT")
@NamedQueries({
        @NamedQuery(name = "commentEntity.findByRequestId", query = "SELECT a FROM HRCommentEntity a where a.requestId = :requestId"),
        @NamedQuery(name = "commentEntity.count", query = "SELECT count(a.id) FROM HRCommentEntity a  where a.requestId = :requestId") })
@Data
public class CommentEntity {

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "SEQ_RH_COMMENT_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_RH_COMMENT_ID")
  private long Id;

  @Column(name = "POSTER_ID", nullable = false)
  private String posterId;

  @Column(name = "REQUEST_ID")
  private long requestId;

  @Column(name = "POSTED_TIME")
  private Date   postedTime;

  @Column(name = "COMMENT_TYPE")
  private String   commentType;

  @Column(name = "COMMENT_TEXT")
  private String   commentText;

}