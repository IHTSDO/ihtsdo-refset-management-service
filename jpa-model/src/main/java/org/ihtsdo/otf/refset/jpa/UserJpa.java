/*
 * Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyClass;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory;
import org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory;
import org.apache.lucene.analysis.ngram.NGramFilterFactory;
import org.apache.lucene.analysis.pattern.PatternReplaceFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.AnalyzerDefs;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.hibernate.search.bridge.builtin.EnumBridge;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserPreferences;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.jpa.helpers.MapIdBridge;
import org.ihtsdo.otf.refset.jpa.helpers.ProjectRoleBridge;
import org.ihtsdo.otf.refset.jpa.helpers.ProjectRoleMapAdapter;

/**
 * JPA enabled implementation of {@link User}.
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {
  "userName"
}))
@Audited
@Indexed
@AnalyzerDefs({
  @AnalyzerDef(name = "userNoStopWord", tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class), filters = {
      @TokenFilterDef(factory = StandardFilterFactory.class),
      @TokenFilterDef(factory = LowerCaseFilterFactory.class)
  }),
  @AnalyzerDef(name = "userAutocompleteEdgeAnalyzer",
      // Split input into tokens according to tokenizer
      tokenizer = @TokenizerDef(factory = KeywordTokenizerFactory.class), filters = {
          // Normalize token text to lowercase, as the user is unlikely to
          // care about casing when searching for matches
          @TokenFilterDef(factory = PatternReplaceFilterFactory.class, params = {
              @Parameter(name = "pattern", value = "([^a-zA-Z0-9\\.])"),
              @Parameter(name = "replacement", value = " "),
              @Parameter(name = "replace", value = "all")
          }), @TokenFilterDef(factory = LowerCaseFilterFactory.class), @TokenFilterDef(factory = StopFilterFactory.class),
          // Index partial words starting at the front, so we can provide
          // Autocomplete functionality
          @TokenFilterDef(factory = EdgeNGramFilterFactory.class, params = {
              @Parameter(name = "minGramSize", value = "3"),
              @Parameter(name = "maxGramSize", value = "255")
          })
      }), @AnalyzerDef(name = "userAutocompleteNGramAnalyzer",
          // Split input into tokens according to tokenizer
          tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class), filters = {
              // Normalize token text to lowercase, as the user is unlikely to
              // care about casing when searching for matches
              @TokenFilterDef(factory = WordDelimiterFilterFactory.class),
              @TokenFilterDef(factory = LowerCaseFilterFactory.class),
              @TokenFilterDef(factory = NGramFilterFactory.class, params = {
                  @Parameter(name = "minGramSize", value = "3"),
                  @Parameter(name = "maxGramSize", value = "5")
              }), @TokenFilterDef(factory = PatternReplaceFilterFactory.class, params = {
                  @Parameter(name = "pattern", value = "([^a-zA-Z0-9\\.])"),
                  @Parameter(name = "replacement", value = " "),
                  @Parameter(name = "replace", value = "all")
              })
          })
})
@XmlRootElement(name = "user")
public class UserJpa implements User {

  /** The id. Set initial value to 50 to bypass entries in import.sql */
  @TableGenerator(name = "EntityIdGenUser", table = "table_generator_users", pkColumnValue = "Entity", initialValue = 50)
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGenUser")
  private Long id;

  /** The user name. */
  @Column(nullable = false, unique = true, length = 250)
  private String userName;

  /** The name. */
  @Column(nullable = false, length = 250)
  private String name;

  /** The email. */
  @Column(nullable = false)
  private String email;

  /** The application role. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole applicationRole;

  /** The auth token. */
  @Transient
  private String authToken;

  /** The user preferences. */
  @OneToOne(mappedBy = "user", targetEntity = UserPreferencesJpa.class, optional = true)
  private UserPreferences userPreferences;

  /** The project role map. */
  @ElementCollection
  @MapKeyClass(value = ProjectJpa.class)
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "user_project_role_map")
  @MapKeyJoinColumn(name = "project_id")
  @Column(name = "role")
  private Map<Project, UserRole> projectRoleMap;

  /**
   * The default constructor.
   */
  public UserJpa() {
    // n/a
  }

  /**
   * Instantiates a new user jpa.
   *
   * @param user the user
   */
  public UserJpa(User user) {
    super();
    id = user.getId();
    userName = user.getUserName();
    name = user.getName();
    email = user.getEmail();
    applicationRole = user.getApplicationRole();
    authToken = user.getAuthToken();
    userPreferences = user.getUserPreferences();
    projectRoleMap = new HashMap<>(user.getProjectRoleMap());
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @SortableField
  public String getUserName() {
    return userName;
  }

  /* see superclass */
  @Override
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /* see superclass */
  @Override
  @Fields({
      @Field(name = "name", index = Index.YES, store = Store.NO, analyze = Analyze.YES, analyzer = @Analyzer(definition = "userNoStopWord")),
      @Field(name = "nameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO),
      @Field(name = "nameEdgeNGram", index = Index.YES, store = Store.NO, analyze = Analyze.YES, analyzer = @Analyzer(definition = "userAutocompleteEdgeAnalyzer")),
      @Field(name = "nameNGram", index = Index.YES, store = Store.NO, analyze = Analyze.YES, analyzer = @Analyzer(definition = "userAutocompleteNGramAnalyzer"))
  })
  @SortableField(forField = "nameSort")
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @SortableField
  public String getEmail() {
    return email;
  }

  /* see superclass */
  @Override
  public void setEmail(String email) {
    this.email = email;
  }

  /* see superclass */
  @Override
  @Field(bridge = @FieldBridge(impl = EnumBridge.class), index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @SortableField
  public UserRole getApplicationRole() {
    return applicationRole;
  }

  /* see superclass */
  @Override
  public void setApplicationRole(UserRole role) {
    this.applicationRole = role;
  }

  /* see superclass */
  @Override
  public String getAuthToken() {
    return authToken;
  }

  /* see superclass */
  @Override
  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  /*
   * <pre> This supports searching both for a particular role on a particular
   * project or to determine if this user is assigned to any project. For
   * example:
   * 
   * "projectRoleMap:10ADMIN" -> finds where the user has an ADMIN role on
   * project 10 "projectAnyRole:10" -> finds where the user has any role on
   * project 10 </pre>
   */
  @XmlJavaTypeAdapter(ProjectRoleMapAdapter.class)
  @Fields({
      @Field(bridge = @FieldBridge(impl = ProjectRoleBridge.class), analyzer = @Analyzer(impl = WhitespaceAnalyzer.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "projectAnyRole", bridge = @FieldBridge(impl = MapIdBridge.class), analyzer = @Analyzer(impl = WhitespaceAnalyzer.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  })
  @Override
  public Map<Project, UserRole> getProjectRoleMap() {
    if (projectRoleMap == null) {
      projectRoleMap = new HashMap<>();
    }
    return projectRoleMap;
  }

  /* see superclass */
  @Override
  public void setProjectRoleMap(Map<Project, UserRole> projectRoleMap) {
    this.projectRoleMap = projectRoleMap;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((applicationRole == null) ? 0 : applicationRole.hashCode());
    result = prime * result + ((authToken == null) ? 0 : authToken.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    UserJpa other = (UserJpa) obj;
    if (applicationRole != other.applicationRole)
      return false;
    if (authToken == null) {
      if (other.authToken != null)
        return false;
    } else if (!authToken.equals(other.authToken))
      return false;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (userName == null) {
      if (other.userName != null)
        return false;
    } else if (!userName.equals(other.userName))
      return false;
    return true;
  }

  /* see superclass */
  @XmlElement(type = UserPreferencesJpa.class)
  @Override
  public UserPreferences getUserPreferences() {
    return userPreferences;
  }

  /* see superclass */
  @Override
  public void setUserPreferences(UserPreferences preferences) {
    this.userPreferences = preferences;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "UserJpa [id=" + id + ", userName=" + userName + ", name=" + name
        + ", email=" + email + ", applicationRole=" + applicationRole
        + ", authToken=" + authToken + "]";
  }

}
