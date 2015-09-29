/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import java.util.Date;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.SearchResultList;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;

/**
 * Generically represents a service for accessing {@link Refset} information.
 */
public interface RefsetService extends ProjectService {

  /**
   * Returns the refset.
   *
   * @param id the id
   * @return the refset
   * @throws Exception the exception
   */
  public Refset getRefset(Long id) throws Exception;

  /**
   * Returns the refset.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the refset
   * @throws Exception the exception
   */
  public Refset getRefset(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Adds the refset.
   *
   * @param refset the refset
   * @return the refset
   * @throws Exception the exception
   */
  public Refset addRefset(Refset refset) throws Exception;

  /**
   * Update refset.
   *
   * @param refset the refset
   * @throws Exception the exception
   */
  public void updateRefset(Refset refset) throws Exception;

  /**
   * Removes the refset.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeRefset(Long id) throws Exception;

  /**
   * Removes the refset descriptor ref set member.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeRefsetDescriptorRefsetMember(Long id) throws Exception;

  /**
   * Update refset descriptor ref set member.
   *
   * @param refsetDescriptorRefsetMember the refset descriptor ref set member
   * @throws Exception the exception
   */
  public void updateRefsetDescriptorRefsetMember(
    RefsetDescriptorRefsetMember refsetDescriptorRefsetMember) throws Exception;

  /**
   * Adds the refset descriptor ref set member.
   *
   * @param refsetDescriptorRefsetMember the refset descriptor ref set member
   * @return the refset descriptor ref set member
   * @throws Exception the exception
   */
  public RefsetDescriptorRefsetMember addRefsetDescriptorRefsetMember(
    RefsetDescriptorRefsetMember refsetDescriptorRefsetMember) throws Exception;

  /**
   * Returns the refset descriptor ref set member.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the refset descriptor ref set member
   * @throws Exception the exception
   */
  public RefsetDescriptorRefsetMember getRefsetDescriptorRefsetMember(
    String terminologyId, String terminology, String version, String branch)
    throws Exception;

  /**
   * Returns the refset descriptor ref set member.
   *
   * @param id the id
   * @return the refset descriptor ref set member
   * @throws Exception the exception
   */
  public RefsetDescriptorRefsetMember getRefsetDescriptorRefsetMember(Long id)
    throws Exception;

  /**
   * Find refsets for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findRefsetsForQuery(String terminology,
    String version, String query, PfsParameter pfs) throws Exception;

  /**
   * Find members for refset.
   *
   * @param refsetId the refset id
   * @param query the query
   * @param pfs the pfs
   * @return the simple ref set member list
   * @throws Exception the exception
   */
  public ConceptRefsetMemberList findMembersForRefset(Long refsetId,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Returns the refset revision.
   *
   * @param refsetId the refset id
   * @param date the date
   * @return the refset revision
   * @throws Exception
   */
  public Refset getRefsetRevision(Long refsetId, Date date) throws Exception;

  /**
   * Find members for refset revision.
   *
   * @param refsetId the refset id
   * @param date the date
   * @param pfs the pfs
   * @return the simple ref set member list
   */
  public ConceptRefsetMemberList findMembersForRefsetRevision(Long refsetId,
    Date date, PfsParameter pfs);

}