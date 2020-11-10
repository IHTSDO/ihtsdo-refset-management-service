/*
 * Copyright 2019 West Coast Informatics, LLC
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
import javax.persistence.JoinColumn;
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
 * The Class UserPreferencesJpa.
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

  /** The user. */
  @OneToOne(targetEntity = UserJpa.class)
  private User user;

  /** The language description types. */
  @OneToMany(cascade = CascadeType.ALL, targetEntity = LanguageDescriptionTypeJpa.class, orphanRemoval = true)
  @CollectionTable(name = "user_pref_language_desc_types", joinColumns = @JoinColumn(name="user_preferences_id"))
  private List<LanguageDescriptionType> languageDescriptionTypes =
      new ArrayList<>();

  /** The last tab. */
  @Column(nullable = true)
  private String lastTab;

  /** The last refset accordion. */
  @Column(nullable = true)
  private String lastRefsetAccordion;

  /** The spelling enabled. */
  @Column(nullable = false)
  private boolean spellingEnabled = true;

  /** The memory enabled. */
  @Column(nullable = false)
  private boolean memoryEnabled = true;

  /** The last translation accordion. */
  private String lastTranslationAccordion;

  /** The last directory accordion. */
  private String lastDirectoryAccordion;

  /** The last project role. */
  private UserRole lastProjectRole;

  /** The last project id. */
  private Long lastProjectId;

  /** The last refset id. */
  private Long lastRefsetId;

  /** The last translation id. */
  private Long lastTranslationId;
  
  /** The module id. */
  @Column(nullable = true)
  private String moduleId;

  /** The feedback email. */
  @Column(nullable = true)
  private String feedbackEmail;

  /** The namespace. */
  @Column(nullable = true)
  private String namespace;

  /** The organization. */
  @Column(nullable = true)
  private String organization;

  /** The exclusion clause. */
  @Column(nullable = true)
  private String exclusionClause;

  /**
   * Instantiates an empty {@link UserPreferencesJpa}.
   */
  public UserPreferencesJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link UserPreferencesJpa} from the specified parameters.
   *
   * @param prefs the prefs
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
    lastRefsetId = prefs.getLastRefsetId();
    lastTranslationId = prefs.getLastTranslationId();
    moduleId = prefs.getModuleId();
    namespace = prefs.getNamespace();
    organization = prefs.getOrganization();
    exclusionClause = prefs.getExclusionClause();
    feedbackEmail = prefs.getFeedbackEmail();
  }

  /* see superclass */
  @Override
  public Long getId() {
    return id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public User getUser() {
    return user;
  }

  /* see superclass */
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
  public String getLastTab() {
    return lastTab;
  }

  /* see superclass */
  @Override
  public void setLastTab(String lastTab) {
    this.lastTab = lastTab;
  }

  /* see superclass */
  @Override
  public String getLastRefsetAccordion() {
    return lastRefsetAccordion;
  }

  /* see superclass */
  @Override
  public void setLastRefsetAccordion(String lastRefsetAccordion) {
    this.lastRefsetAccordion = lastRefsetAccordion;
  }

  /* see superclass */
  @Override
  public String getLastTranslationAccordion() {
    return lastTranslationAccordion;
  }

  /* see superclass */
  @Override
  public void setLastTranslationAccordion(String lastTranslationAccordion) {
    this.lastTranslationAccordion = lastTranslationAccordion;
  }

  /* see superclass */
  @Override
  public String getLastDirectoryAccordion() {
    return lastDirectoryAccordion;
  }

  /* see superclass */
  @Override
  public void setLastDirectoryAccordion(String lastDirectoryAccordion) {
    this.lastDirectoryAccordion = lastDirectoryAccordion;
  }

  /* see superclass */
  @Override
  public UserRole getLastProjectRole() {
    return lastProjectRole;
  }

  /* see superclass */
  @Override
  public void setLastProjectRole(UserRole lastProjectRole) {
    this.lastProjectRole = lastProjectRole;
  }

  /* see superclass */
  @Override
  public Long getLastProjectId() {
    return lastProjectId;
  }

  /* see superclass */
  @Override
  public void setLastProjectId(Long lastProjectId) {
    this.lastProjectId = lastProjectId;
  }

  /* see superclass */
  @Override
  public Long getLastRefsetId() {
    return lastRefsetId;
  }

  /* see superclass */
  @Override
  public void setLastRefsetId(Long lastRefsetId) {
    this.lastRefsetId = lastRefsetId;
  }

  /* see superclass */
  @Override
  public Long getLastTranslationId() {
    return lastTranslationId;
  }

  /* see superclass */
  @Override
  public void setLastTranslationId(Long lastTranslationId) {
    this.lastTranslationId = lastTranslationId;
  }

  /* see superclass */
  @XmlElement(type = LanguageDescriptionTypeJpa.class)
  @Override
  public List<LanguageDescriptionType> getLanguageDescriptionTypes() {
    if (languageDescriptionTypes == null) {
      languageDescriptionTypes = new ArrayList<>();
    }
    return languageDescriptionTypes;
  }

  /* see superclass */
  @Override
  public void setLanguageDescriptionTypes(
    List<LanguageDescriptionType> languageDescriptionTypes) {
    this.languageDescriptionTypes = languageDescriptionTypes;
  }

  /* see superclass */
  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.refset.UserPreferences#isSpellingEnabled()
   */
  @Override
  public boolean isSpellingEnabled() {
    return spellingEnabled;
  }

  /* see superclass */
  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.refset.UserPreferences#setSpellingEnabled(boolean)
   */
  @Override
  public void setSpellingEnabled(boolean spellingEnabled) {
    this.spellingEnabled = spellingEnabled;
  }

  /* see superclass */
  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.refset.UserPreferences#isMemoryEnabled()
   */
  @Override
  public boolean isMemoryEnabled() {
    return memoryEnabled;
  }

  /* see superclass */
  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.refset.UserPreferences#setMemoryEnabled(boolean)
   */
  @Override
  public void setMemoryEnabled(boolean memoryEnabled) {
    this.memoryEnabled = memoryEnabled;
  }

  /* see superclass */
  @Override
  public String getModuleId() {
    return moduleId;
  }

  /* see superclass */
  @Override
  public void setModuleId(String moduleId) {
    this.moduleId = moduleId;
  }

  /* see superclass */
  @Override
  public String getFeedbackEmail() {
    return feedbackEmail;
  }

  /* see superclass */
  @Override
  public void setFeedbackEmail(String feedbackEmail) {
    this.feedbackEmail = feedbackEmail;
  }

  /* see superclass */
  @Override
  public String getNamespace() {
    return namespace;
  }

  /* see superclass */
  @Override
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /* see superclass */
  @Override
  public String getOrganization() {
    return organization;
  }

  /* see superclass */
  @Override
  public void setOrganization(String organization) {
    this.organization = organization;
  }

  /* see superclass */
  @Override
  public String getExclusionClause() {
    return exclusionClause;
  }

  /* see superclass */
  @Override
  public void setExclusionClause(String exclusionClause) {
    this.exclusionClause = exclusionClause;
  }
  /* see superclass */

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((languageDescriptionTypes == null) ? 0
        : languageDescriptionTypes.hashCode());
    result = prime * result + ((lastDirectoryAccordion == null) ? 0
        : lastDirectoryAccordion.hashCode());
    result = prime * result
        + ((lastProjectId == null) ? 0 : lastProjectId.hashCode());
    result =
        prime * result + ((lastRefsetId == null) ? 0 : lastRefsetId.hashCode());
    result = prime * result
        + ((lastTranslationId == null) ? 0 : lastTranslationId.hashCode());
    result = prime * result
        + ((lastProjectRole == null) ? 0 : lastProjectRole.hashCode());
    result = prime * result
        + ((lastRefsetAccordion == null) ? 0 : lastRefsetAccordion.hashCode());
    result = prime * result + ((lastTab == null) ? 0 : lastTab.hashCode());
    result = prime * result + ((moduleId == null) ? 0 : moduleId.hashCode());
    result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
    result =
        prime * result + ((organization == null) ? 0 : organization.hashCode());
    result = prime * result
        + ((exclusionClause == null) ? 0 : exclusionClause.hashCode());
    result = prime * result
        + ((feedbackEmail == null) ? 0 : feedbackEmail.hashCode());
    result = prime * result + ((lastTranslationAccordion == null) ? 0
        : lastTranslationAccordion.hashCode());
    result = prime * result + (memoryEnabled ? 1231 : 1237);
    result = prime * result + (spellingEnabled ? 1231 : 1237);
    final String userName =
        user == null || user.getUserName() == null ? null : user.getUserName();
    result = prime * result + ((userName == null) ? 0 : userName.hashCode());

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
    if (lastRefsetId == null) {
      if (other.lastRefsetId != null)
        return false;
    } else if (!lastRefsetId.equals(other.lastRefsetId))
      return false;
    if (lastTranslationId == null) {
      if (other.lastTranslationId != null)
        return false;
    } else if (!lastTranslationId.equals(other.lastTranslationId))
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

  /* see superclass */
  @Override
  public String toString() {
    return "UserPreferencesJpa [id=" + id + ", user=" + user
        + ", languageDescriptionTypes=" + languageDescriptionTypes
        + ", lastTab=" + lastTab + ", lastRefsetAccordion="
        + lastRefsetAccordion + ", spellingEnabled=" + spellingEnabled
        + ", memoryEnabled=" + memoryEnabled + ", lastTranslationAccordion="
        + lastTranslationAccordion + ", lastDirectoryAccordion="
        + lastDirectoryAccordion + ", lastProjectRole=" + lastProjectRole
        + ", lastProjectId=" + lastProjectId + ", moduleId=" + moduleId
        + ", namespace=" + namespace + ", organization=" + organization
        + ", exclusionClause=" + exclusionClause + ", feedbackEmail="
        + feedbackEmail + ", lastRefsetId=" + lastRefsetId
        + ", lastTranslationId=" + lastTranslationId + "]";
  }

}