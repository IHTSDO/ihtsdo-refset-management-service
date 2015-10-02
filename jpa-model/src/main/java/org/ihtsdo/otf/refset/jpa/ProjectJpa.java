/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.XmlGenericMapAdapter;
import org.ihtsdo.otf.refset.jpa.helpers.UserRoleBridge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JPA enabled implementation of {@link Project}.
 */
@Entity
@Table(name = "projects", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "description"
}))
@Audited
@Indexed
@XmlRootElement(name = "project")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectJpa implements Project {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified = new Date();

  /** The last modified. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The namespace. */
  @Column(nullable = true)
  private String namespace;

  /** The description. */
  @Column(nullable = false)
  private String description;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The version. */
  @Column(nullable = false)
  private String version;

  /** The role map. */
  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyClass(value = UserJpa.class)
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "project_user_role_map", joinColumns = @JoinColumn(name = "user_id"))
  private Map<User, UserRole> userRoleMap;

  /** The refsets. */
  @OneToMany(mappedBy = "project", orphanRemoval = true, targetEntity = RefsetJpa.class)
  // @IndexedEmbedded - n/a
  private List<Refset> refsets = null;

  /**
   * Instantiates an empty {@link ProjectJpa}.
   */
  public ProjectJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ProjectJpa} from the specified parameters.
   *
   * @param project the project
   */
  public ProjectJpa(Project project) {
    super();
    id = project.getId();
    lastModified = project.getLastModified();
    lastModifiedBy = project.getLastModifiedBy();
    name = project.getName();
    namespace = project.getNamespace();
    description = project.getDescription();
    terminology = project.getTerminology();
    version = project.getVersion();
    userRoleMap = new HashMap<>(project.getUserRoleMap());
    refsets = new ArrayList<Refset>();
    for (Refset refset : project.getRefsets()) {
      refsets.add(new RefsetJpa(refset));
    }
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public Long getId() {
    return this.id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Returns the object id. For JAXB.
   *
   * @return the object id
   */
  public String getObjectId() {
    return id == null ? "" : id.toString();
  }

  /**
   * Sets the object id. For JAXB.
   *
   * @param id the object id
   */
  public void setObjectId(String id) {
    this.id = Long.parseLong(id);
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public Date getLastModified() {
    return lastModified;
  }

  /* see superclass */
  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  /* see superclass */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getVersion() {
    return version;
  }

  /* see superclass */
  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  /* see superclass */
  @Override
  @Fields({
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "nameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
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
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "descriptionSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getDescription() {
    return description;
  }

  /* see superclass */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return "ProjectJpa [name=" + name + ", namespace=" + namespace
        + ", description=" + description + ", terminology=" + terminology
        + ", version=" + version + "]";
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public String getTerminologyId() {
    return id.toString();
  }

  /* see superclass */
  @Override
  public void setTerminologyId(String terminologyId) {
    // n/a
  }

  /* see superclass */
  @XmlJavaTypeAdapter(XmlGenericMapAdapter.class)
  @Field(bridge = @FieldBridge(impl = UserRoleBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  @Override
  public Map<User, UserRole> getUserRoleMap() {
    if (userRoleMap == null) {
      userRoleMap = new HashMap<>();
    }
    return userRoleMap;
  }

  /* see superclass */
  @Override
  public void setUserRoleMap(Map<User, UserRole> userRoleMap) {
    this.userRoleMap = userRoleMap;
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
    ProjectJpa other = (ProjectJpa) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (namespace == null) {
      if (other.namespace != null)
        return false;
    } else if (!namespace.equals(other.namespace))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public List<Refset> getRefsets() {
    if (refsets == null) {
      refsets = new ArrayList<Refset>();
    }
    return refsets;
  }

  /* see superclass */
  @Override
  public void setRefsets(List<Refset> refsets) {
    this.refsets = refsets;
  }

  /* see superclass */
  @Override
  public void addRefset(Refset refset) {
    if (refsets == null) {
      refsets = new ArrayList<Refset>();
    }
    refsets.add(refset);
  }

  /* see superclass */
  @Override
  public void removeRefset(Refset refset) {
    if (refsets != null) {
      refsets.remove(refset);
    }

  }

}