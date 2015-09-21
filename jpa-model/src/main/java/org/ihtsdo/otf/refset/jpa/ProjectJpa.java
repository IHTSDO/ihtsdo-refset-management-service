/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.User;

/**
 * JPA enabled implementation of {@link Project}. TODO: convert all sets to
 * lists.
 */
@Entity
@Table(name = "projects", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "description"
}))
@Audited
@Indexed
@XmlRootElement(name = "project")
public class ProjectJpa implements Project {

  /** The id. */
  @Id
  @GeneratedValue
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

  /** The description. */
  @Column(nullable = false)
  private String description;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The version. */
  @Column(nullable = false)
  private String version;

  /** The leads. */
  @ManyToMany(targetEntity = UserJpa.class, fetch = FetchType.EAGER)
  @JoinTable(name = "projects_leads", joinColumns = @JoinColumn(name = "projects_id"), inverseJoinColumns = @JoinColumn(name = "users_id"))
  @IndexedEmbedded(targetElement = UserJpa.class)
  private Set<User> leads = new HashSet<>();

  /** The authors. */
  @ManyToMany(targetEntity = UserJpa.class, fetch = FetchType.EAGER)
  @JoinTable(name = "projects_authors", joinColumns = @JoinColumn(name = "projects_id"), inverseJoinColumns = @JoinColumn(name = "users_id"))
  @IndexedEmbedded(targetElement = UserJpa.class)
  private Set<User> authors = new HashSet<>();

  /** The admins. */
  @ManyToMany(targetEntity = UserJpa.class, fetch = FetchType.EAGER)
  @JoinTable(name = "projects_admins", joinColumns = @JoinColumn(name = "projects_id"), inverseJoinColumns = @JoinColumn(name = "users_id"))
  @IndexedEmbedded(targetElement = UserJpa.class)
  private Set<User> admins = new HashSet<>();

  /** The branch. */
  @Column(nullable = true)
  private String branch;

  /** The refsets. */
  @OneToMany(mappedBy = "project", orphanRemoval = true, targetEntity = RefsetJpa.class)
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
    description = project.getDescription();
    terminology = project.getTerminology();
    version = project.getVersion();
    leads = new HashSet<>(project.getLeads());
    authors = new HashSet<>(project.getAuthors());
    admins = new HashSet<>(project.getAdmins());

    branch = project.getBranch();
  }

  /* see superclass */
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
  @XmlElement(type = UserJpa.class)
  public Set<User> getLeads() {
    return leads;
  }

  /* see superclass */
  @Override
  public void setLeads(Set<User> leads) {
    this.leads = leads;
  }

  /* see superclass */
  @Override
  public void addLead(User lead) {
    leads.add(lead);
  }

  /* see superclass */
  @Override
  public void removeLead(User lead) {
    leads.remove(lead);
  }

  /* see superclass */
  @Override
  @XmlElement(type = UserJpa.class)
  public Set<User> getAuthors() {
    return authors;
  }

  /* see superclass */
  @Override
  public void setAuthors(Set<User> authors) {
    this.authors = authors;
  }

  /* see superclass */
  @Override
  public void addAuthor(User author) {
    authors.add(author);
  }

  /* see superclass */
  @Override
  public void removeAuthor(User author) {
    authors.remove(author);
  }

  /* see superclass */
  @Override
  @XmlElement(type = UserJpa.class)
  public Set<User> getAdmins() {
    return admins;
  }

  /* see superclass */
  @Override
  public void setAdmins(Set<User> admins) {
    this.admins = admins;
  }

  /* see superclass */
  @Override
  public void addAdmin(User admin) {
    admins.add(admin);
  }

  /* see superclass */
  @Override
  public void removeAdmin(User admin) {
    admins.remove(admin);
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
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getDescription() {
    return description;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /* see superclass */
  @Override
  public String toString() {
    return getName() + " " + getId();
  }

  /* see superclass */
  @Override
  public String getBranch() {
    return branch;
  }

  /* see superclass */
  @Override
  public void setBranch(String branch) {
    this.branch = branch;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
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
  @XmlElement(type = RefsetJpa.class)
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

  @Override
  public String getTerminologyId() {
    return id.toString();
  }

  @Override
  public void setTerminologyId(String terminologyId) {
    // n/a
  }
}