/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import java.util.List;
import java.util.Set;

import org.ihtsdo.otf.refset.helpers.Searchable;

/**
 * Generically represents an editing project.
 */
public interface Project extends Searchable {

  /**
   * Returns the description.
   * 
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   * 
   * @param description the description
   */
  public void setDescription(String description);

  /**
   * Returns the leads.
   * 
   * @return the leads
   */
  public Set<User> getLeads();

  /**
   * Sets the leads.
   * 
   * @param leads the leads
   */
  public void setLeads(Set<User> leads);

  /**
   * Adds the lead.
   * 
   * @param lead the lead
   */
  public void addLead(User lead);

  /**
   * Removes the lead.
   * 
   * @param lead the lead
   */
  public void removeLead(User lead);

  /**
   * Returns the administrators.
   * 
   * @return the administrators
   */
  public Set<User> getAdmins();

  /**
   * Sets the administrators.
   * @param admins the administrators
   */
  public void setAdmins(Set<User> admins);

  /**
   * Adds the administrator.
   * 
   * @param admin a administrator
   */
  public void addAdmin(User admin);

  /**
   * Removes the administrator.
   * 
   * @param admin the administrator
   */
  public void removeAdmin(User admin);

  /**
   * Returns the author.
   * 
   * @return the author.
   */
  public Set<User> getAuthors();

  /**
   * Sets the authors.
   * 
   * @param authors the authors
   */
  public void setAuthors(Set<User> authors);

  /**
   * Adds the author.
   * 
   * @param author the author
   */
  public void addAuthor(User author);

  /**
   * Removes the author.
   * 
   * @param author the author
   */
  public void removeAuthor(User author);

  /**
   * Returns the branch.
   *
   * @return the branch
   */
  public String getBranch();

  /**
   * Sets the branch.
   *
   * @param branch the branch
   */
  public void setBranch(String branch);

  /**
   * Returns the refsets.
   *
   * @return the refsets
   */
  public List<Refset> getRefsets();

  /**
   * Sets the refsets.
   *
   * @param refsets the refsets
   */
  public void setRefsets(List<Refset> refsets);

  /**
   * Adds the refset.
   *
   * @param refset the refset
   */
  public void addRefset(Refset refset);

  /**
   * Removes the refset.
   *
   * @param refset the refset
   */
  public void removeRefset(Refset refset);

}