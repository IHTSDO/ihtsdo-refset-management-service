/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserPreferences;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageDescriptionTypeJpa;

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

  /** The language Refset members. */
  @OneToMany(cascade = CascadeType.ALL, targetEntity = LanguageDescriptionTypeJpa.class, orphanRemoval = true)
  @CollectionTable(name = "user_pref_language_desc_types")
  private List<LanguageDescriptionType> languageDescriptionTypes =
      new ArrayList<>();

  /** The lastTab. */
  @Column(nullable = true)
  private String lastTab;

  /** The lastRefsetAccordion. */
  @Column(nullable = true)
  private String lastRefsetAccordion;

  /** The spelling enabled. */
  @Column(nullable = false)
  private boolean spellingEnabled = true;

  /** The memory enabled. */
  @Column(nullable = false)
  private boolean memoryEnabled = true;

  /** The lastTranslationAccordion. */
  private String lastTranslationAccordion;

  /** The lastDirectoryAccordion. */
  private String lastDirectoryAccordion;

  /** The lastProjectRole. */
  private UserRole lastProjectRole;

  /** The lastProjectId. */
  private Long lastProjectId;
  
  /**  The module id. */
  @Column(nullable = true)
  private String moduleId;
  
  /**  The module id. */
  @Column(nullable = true)
  private String feedbackEmail;
  
  /**  The namespace. */
  @Column(nullable = true)
  private String namespace;
  
  /**  The organization. */
  @Column(nullable = true)
  private String organization;
  
  /**  The exclusion clause. */
  @Column(nullable = true)
  private String exclusionClause;
  

  /**
   * The default constructor.
   */
  public UserPreferencesJpa() {
    // n/a
  }

  /**
   * Instantiates a new user jpa.
   *
   * @param prefs the user preferences
   */
  public UserPreferencesJpa(UserPreferences prefs) {
    super();
    id = prefs.getId();
    user = prefs.getUser();
    lastTab = prefs.getLastTab();
    languageDescriptionTypes = prefs.getLanguageDescriptionTypes();
    spellingEnabled = prefs.isSpellingEnabled();
    memoryEnabled = prefs.isMemoryEnabled();
    languageDescriptionTypes = prefs.getLanguageDescriptionTypes();
    lastRefsetAccordion = prefs.getLastRefsetAccordion();
    lastTranslationAccordion = prefs.getLastTranslationAccordion();
    lastDirectoryAccordion = prefs.getLastDirectoryAccordion();
    lastProjectRole = prefs.getLastProjectRole();
    lastProjectId = prefs.getLastProjectId();
    moduleId = prefs.getModuleId();
    namespace = prefs.getNamespace();
    organization = prefs.getOrganization();
    exclusionClause = prefs.getExclusionClause();
    feedbackEmail = prefs.getFeedbackEmail();
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
   * Returns the last refset accordion accessed.
   *
   * @return the lastRefsetAccordion
   */
  @Override
  public String getLastRefsetAccordion() {
    return lastRefsetAccordion;
  }

  /**
   * Sets the last refset accordion accessed.
   *
   * @param lastRefsetAccordion the last refset accordion accessed
   */
  @Override
  public void setLastRefsetAccordion(String lastRefsetAccordion) {
    this.lastRefsetAccordion = lastRefsetAccordion;
  }

  /**
   * Returns the last translation accordion accessed.
   *
   * @return the lastTranslationAccordion
   */
  @Override
  public String getLastTranslationAccordion() {
    return lastTranslationAccordion;
  }

  /**
   * Sets the last translation accordion accessed.
   *
   * @param lastTranslationAccordion the last translation accordion accessed
   */
  @Override
  public void setLastTranslationAccordion(String lastTranslationAccordion) {
    this.lastTranslationAccordion = lastTranslationAccordion;
  }

  /**
   * Returns the last directory accordion accessed.
   *
   * @return the lastDirectoryAccordion
   */
  @Override
  public String getLastDirectoryAccordion() {
    return lastDirectoryAccordion;
  }

  /**
   * Sets the last directory accordion accessed.
   *
   * @param lastDirectoryAccordion the last directory accordion accessed
   */
  @Override
  public void setLastDirectoryAccordion(String lastDirectoryAccordion) {
    this.lastDirectoryAccordion = lastDirectoryAccordion;
  }

  /**
   * Returns the last project role accessed.
   *
   * @return the lastProjectRole
   */
  @Override
  public UserRole getLastProjectRole() {
    return lastProjectRole;
  }

  /**
   * Sets the last project role accessed.
   *
   * @param lastProjectRole the last project role accessed
   */
  @Override
  public void setLastProjectRole(UserRole lastProjectRole) {
    this.lastProjectRole = lastProjectRole;
  }

  /**
   * Returns the last project ID accessed.
   *
   * @return the lastProjectId
   */
  @Override
  public Long getLastProjectId() {
    return lastProjectId;
  }

  /**
   * Sets the last project accessed.
   *
   * @param lastProjectId the last project id
   */
  @Override
  public void setLastProjectId(Long lastProjectId) {
    this.lastProjectId = lastProjectId;
  }

  /* see superclass */
  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#getLanguageDescriptionTypes()
   */
  @XmlElement(type = LanguageDescriptionTypeJpa.class)
  @Override
  public List<LanguageDescriptionType> getLanguageDescriptionTypes() {
    if (languageDescriptionTypes == null) {
      languageDescriptionTypes = new ArrayList<>();
    }
    return languageDescriptionTypes;
  }

  /* see superclass */
  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#setLanguageDescriptionTypes(java.util.List)
   */
  @Override
  public void setLanguageDescriptionTypes(
    List<LanguageDescriptionType> languageDescriptionTypes) {
    this.languageDescriptionTypes = languageDescriptionTypes;
  }

  /* see superclass */
  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#isSpellingEnabled()
   */
  @Override
  public boolean isSpellingEnabled() {
    return spellingEnabled;
  }

  /* see superclass */
  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#setSpellingEnabled(boolean)
   */
  @Override
  public void setSpellingEnabled(boolean spellingEnabled) {
    this.spellingEnabled = spellingEnabled;
  }

  /* see superclass */
  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#isMemoryEnabled()
   */
  @Override
  public boolean isMemoryEnabled() {
    return memoryEnabled;
  }

  /* see superclass */
  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#setMemoryEnabled(boolean)
   */
  @Override
  public void setMemoryEnabled(boolean memoryEnabled) {
    this.memoryEnabled = memoryEnabled;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime
            * result
            + ((languageDescriptionTypes == null) ? 0
                : languageDescriptionTypes.hashCode());
    result =
        prime
            * result
            + ((lastDirectoryAccordion == null) ? 0 : lastDirectoryAccordion
                .hashCode());
    result =
        prime * result
            + ((lastProjectId == null) ? 0 : lastProjectId.hashCode());
    result =
        prime * result
            + ((lastProjectRole == null) ? 0 : lastProjectRole.hashCode());
    result =
        prime
            * result
            + ((lastRefsetAccordion == null) ? 0 : lastRefsetAccordion
                .hashCode());
    result = prime * result + ((lastTab == null) ? 0 : lastTab.hashCode());
    result = prime * result + ((moduleId == null) ? 0 : moduleId.hashCode());
    result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
    result = prime * result + ((organization == null) ? 0 : organization.hashCode());
    result = prime * result + ((exclusionClause == null) ? 0 : exclusionClause.hashCode());
    result = prime * result + ((feedbackEmail == null) ? 0 : feedbackEmail.hashCode());
    result =
        prime
            * result
            + ((lastTranslationAccordion == null) ? 0
                : lastTranslationAccordion.hashCode());
    result = prime * result + (memoryEnabled ? 1231 : 1237);
    result = prime * result + (spellingEnabled ? 1231 : 1237);
    final String userName =
        user == null || user.getUserName() == null ? null : user.getUserName();
    result = prime * result + ((userName == null) ? 0 : userName.hashCode());

    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UserPreferencesJpa other = (UserPreferencesJpa) obj;
    if (languageDescriptionTypes == null) {
      if (other.languageDescriptionTypes != null)
        return false;
    } else if (!languageDescriptionTypes.equals(other.languageDescriptionTypes))
      return false;
    if (lastDirectoryAccordion == null) {
      if (other.lastDirectoryAccordion != null)
        return false;
    } else if (!lastDirectoryAccordion.equals(other.lastDirectoryAccordion))
      return false;
    if (lastProjectId == null) {
      if (other.lastProjectId != null)
        return false;
    } else if (!lastProjectId.equals(other.lastProjectId))
      return false;
    if (lastProjectRole != other.lastProjectRole)
      return false;
    if (lastRefsetAccordion == null) {
      if (other.lastRefsetAccordion != null)
        return false;
    } else if (!lastRefsetAccordion.equals(other.lastRefsetAccordion))
      return false;
    if (lastTab == null) {
      if (other.lastTab != null)
        return false;
    } else if (!lastTab.equals(other.lastTab))
      return false;
    if (lastTranslationAccordion == null) {
      if (other.lastTranslationAccordion != null)
        return false;
    } else if (!lastTranslationAccordion.equals(other.lastTranslationAccordion))
      return false;
    if (memoryEnabled != other.memoryEnabled)
      return false;
    if (spellingEnabled != other.spellingEnabled)
      return false;
    final String userName =
        user == null || user.getUserName() == null ? null : user.getUserName();
    final String otherUserName =
        other.user == null || other.user.getUserName() == null ? null
            : other.user.getUserName();
    if (userName == null) {
      if (otherUserName != null)
        return false;
    } else if (!userName.equals(otherUserName))
      return false;
    if (userName == null) {
      if (otherUserName != null)
        return false;
    } else if (!userName.equals(otherUserName))
      return false;
    if (userName == null) {
      if (otherUserName != null)
        return false;
    } else if (!userName.equals(otherUserName))
      return false;
    if (moduleId == null) {
      if (other.moduleId != null)
        return false;
    } else if (!moduleId.equals(other.moduleId))
      return false;
    if (namespace == null) {
      if (other.namespace != null)
        return false;
    } else if (!namespace.equals(other.namespace))
      return false;
    if (organization == null) {
      if (other.organization != null)
        return false;
    } else if (!organization.equals(other.organization))
      return false;
    if (exclusionClause == null) {
      if (other.exclusionClause != null)
        return false;
    } else if (!exclusionClause.equals(other.exclusionClause))
      return false;
    if (feedbackEmail == null) {
      if (other.feedbackEmail != null)
        return false;
    } else if (!feedbackEmail.equals(other.feedbackEmail))
      return false;
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "UserPreferencesJpa [id=" + id + ", user=" + user
        + ", languageDescriptionTypes=" + languageDescriptionTypes
        + ", lastTab=" + lastTab + ", lastRefsetAccordion="
        + lastRefsetAccordion + ", spellingEnabled=" + spellingEnabled
        + ", memoryEnabled=" + memoryEnabled + ", lastTranslationAccordion="
        + lastTranslationAccordion + ", lastDirectoryAccordion="
        + lastDirectoryAccordion + ", lastProjectRole=" + lastProjectRole
        + ", lastProjectId=" + lastProjectId 
        + ", moduleId=" + moduleId
        + ", namespace=" + namespace
        + ", organization=" + organization
        + ", exclusionClause=" + exclusionClause
        + ", feedbackEmail=" + feedbackEmail
        + "]";
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#getModuleId()
   */
  @Override
  public String getModuleId() {
    return moduleId;
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#setModuleId(java.lang.String)
   */
  @Override
  public void setModuleId(String moduleId) {
    this.moduleId = moduleId;
  }
  
  @Override
  public String getFeedbackEmail() {
    return feedbackEmail;
  }
  
  @Override
  public void setFeedbackEmail(String feedbackEmail) {
    this.feedbackEmail = feedbackEmail;
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#getNamespace()
   */
  @Override
  public String getNamespace() {
    return namespace;
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#setNamespace(java.lang.String)
   */
  @Override
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#getOrganization()
   */
  @Override
  public String getOrganization() {
    return organization;
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#setOrganization(java.lang.String)
   */
  @Override
  public void setOrganization(String organization) {
    this.organization = organization;
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#getExclusionClause()
   */
  @Override
  public String getExclusionClause() {
    return exclusionClause;
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.refset.UserPreferences#setExclusionClause(java.lang.String)
   */
  @Override
  public void setExclusionClause(String exclusionClause) {
    this.exclusionClause = exclusionClause;
  }

}