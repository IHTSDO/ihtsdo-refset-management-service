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

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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
    if (user == null) {
      if (other.user != null)
        return false;
    } else if (!user.getUserName().equals(other.user.getUserName()))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "UserPreferencesJpa [id=" + id + ", user=" + user + "]";
  }

}
