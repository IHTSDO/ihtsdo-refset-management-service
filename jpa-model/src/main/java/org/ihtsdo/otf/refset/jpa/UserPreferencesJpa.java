/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserPreferences;

/**
 * JPA enabled implementation of {@link UserPreferences}.
 */
@Entity
@Table(name = "user_preferences")
@Audited
@XmlRootElement(name = "prefs")
public class UserPreferencesJpa implements UserPreferences {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The user name. */
  @OneToOne(targetEntity = UserJpa.class)
  private User user;

  /** The lastTab. */
  private String lastTab;

  /** The lastAccordion. */
  private String lastAccordion;

  /**
   * The default constructor.
   */
  public UserPreferencesJpa() {
    // n/a
  }

  /**
   * Instantiates a new user jpa.
   *
   * @param userPreferences the user preferences
   */
  public UserPreferencesJpa(UserPreferences userPreferences) {
    super();
    id = userPreferences.getId();
    user = userPreferences.getUser();
    lastTab = userPreferences.getLastTab();
    lastAccordion = userPreferences.getLastAccordion();
  }

  /**
   * Returns the id.
   *
   * @return the id
   */
  @Override
  public Long getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the id
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Returns the user.
   *
   * @return the user
   */
  @XmlTransient
  @Override
  public User getUser() {
    return user;
  }

  /**
   * Sets the user.
   *
   * @param user the user
   */
  @Override
  public void setUser(User user) {
    this.user = user;
  }

  /**
   * Returns the user id.
   *
   * @return the user id
   */
  @XmlElement
  public Long getUserId() {
    return user == null ? 0L : user.getId();
  }

  /**
   * Sets the user id.
   *
   * @param id the user id
   */
  public void setUserId(Long id) {
    if (user == null) {
      user = new UserJpa();
    }
    user.setId(id);
  }

  /**
   * Returns the user name.
   *
   * @return the user name
   */
  public String getUserName() {
    return user == null ? "" : user.getUserName();
  }

  /**
   * Sets the user name.
   *
   * @param userName the user name
   */
  public void setUserName(String userName) {
    if (user == null) {
      user = new UserJpa();
    }
    user.setUserName(userName);
  }

  /**
   * Returns the last tab accessed.
   *
   * @return the lastTab
   */
  @Override
  public String getLastTab() {
    return lastTab;
  }

  /**
   * Sets the last tab accessed.
   *
   * @param lastTab the last tab accessed
   */
  @Override
  public void setLastTab(String lastTab) {
    this.lastTab = lastTab;
  }

  /**
   * Sets the last accordion accessed.
   *
   * @param lastAccordion the last accordion accessed
   */
  @Override
  public void setLastAccordion(String lastAccordion) {
    this.lastAccordion = lastAccordion;
  }

  /**
   * Returns the last accordion accessed.
   *
   * @return the lastAccordion
   */
  @Override
  public String getLastAccordion() {
    return lastAccordion;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result =
        prime * result
            + ((lastAccordion == null) ? 0 : lastAccordion.hashCode());
    result = prime * result + ((lastTab == null) ? 0 : lastTab.hashCode());
    result = prime * result + ((user == null) ? 0 : user.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UserPreferencesJpa other = (UserPreferencesJpa) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (lastAccordion == null) {
      if (other.lastAccordion != null)
        return false;
    } else if (!lastAccordion.equals(other.lastAccordion))
      return false;
    if (lastTab == null) {
      if (other.lastTab != null)
        return false;
    } else if (!lastTab.equals(other.lastTab))
      return false;
    if (user == null) {
      if (other.user != null)
        return false;
    } else if (!user.equals(other.user))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "UserPreferencesJpa [id=" + id + ", user=" + user + ", lastTab="
        + lastTab + ", lastAccordion=" + lastAccordion + "]";
  }

}