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

import java.util.Date;
import java.util.List;

public class ContextDTO {

  private String currentUser;

  private String currentUserAvatar;

  private String currentUserName;

  private String employeesSpace;

  private float holidaysBalance;

  private String hrId;

  private String insuranceId;

  private float sickBalance;

  private String socialSecNumber;

  private List<VacationRequestDTO> vacationRequestsToValidate;

  private List<VacationRequestDTO> myVacationRequests;

  private VacationRequestDTO vacationRequestsToShow;

  private List<ConventionalVacationDTO>  conventionalVacations;

  private List<OfficialVacationDTO> officialVacations;

  private List<Date> officialDays;

  public String getCurrentUser() {
    return currentUser;
  }

  public void setCurrentUser(String currentUser) {
    this.currentUser = currentUser;
  }

  public String getCurrentUserAvatar() {
    return currentUserAvatar;
  }

  public void setCurrentUserAvatar(String currentUserAvatar) {
    this.currentUserAvatar = currentUserAvatar;
  }

  public String getCurrentUserName() {
    return currentUserName;
  }

  public void setCurrentUserName(String currentUserName) {
    this.currentUserName = currentUserName;
  }

  public String getEmployeesSpace() {
    return employeesSpace;
  }

  public void setEmployeesSpace(String employeesSpace) {
    this.employeesSpace = employeesSpace;
  }

  public float getHolidaysBalance() {
    return holidaysBalance;
  }

  public void setHolidaysBalance(float holidaysBalance) {
    this.holidaysBalance = holidaysBalance;
  }

  public String getHrId() {
    return hrId;
  }

  public void setHrId(String hrId) {
    this.hrId = hrId;
  }

  public String getInsuranceId() {
    return insuranceId;
  }

  public void setInsuranceId(String insuranceId) {
    this.insuranceId = insuranceId;
  }

  public float getSickBalance() {
    return sickBalance;
  }

  public void setSickBalance(float sickBalance) {
    this.sickBalance = sickBalance;
  }

  public String getSocialSecNumber() {
    return socialSecNumber;
  }

  public void setSocialSecNumber(String socialSecNumber) {
    this.socialSecNumber = socialSecNumber;
  }

  public List<VacationRequestDTO> getVacationRequestsToValidate() {
    return vacationRequestsToValidate;
  }

  public void setVacationRequestsToValidate(List<VacationRequestDTO> vacationRequestsToValidate) {
    this.vacationRequestsToValidate = vacationRequestsToValidate;
  }

  public List<VacationRequestDTO> getMyVacationRequests() {
    return myVacationRequests;
  }

  public void setMyVacationRequests(List<VacationRequestDTO> myVacationRequests) {
    this.myVacationRequests = myVacationRequests;
  }

  public VacationRequestDTO getVacationRequestsToShow() {
    return vacationRequestsToShow;
  }

  public void setVacationRequestsToShow(VacationRequestDTO vacationRequestsToShow) {
    this.vacationRequestsToShow = vacationRequestsToShow;
  }


  public List<ConventionalVacationDTO> getConventionalVacations() {
    return conventionalVacations;
  }

  public void setConventionalVacations(List<ConventionalVacationDTO> conventionalVacations) {
    this.conventionalVacations = conventionalVacations;
  }

  public List<Date> getOfficialDays() {
    return officialDays;
  }

  public void setOfficialDays(List<Date> officialDays) {
    this.officialDays = officialDays;
  }

  public List<OfficialVacationDTO> getOfficialVacations() {
    return officialVacations;
  }

  public void setOfficialVacations(List<OfficialVacationDTO> officialVacations) {
    this.officialVacations = officialVacations;
  }
}
