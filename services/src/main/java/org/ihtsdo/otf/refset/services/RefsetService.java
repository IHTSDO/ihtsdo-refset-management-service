/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import java.util.Date;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.SearchResultList;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportRefsetHandler;

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
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public RefsetList findRefsetsForQuery(String query, PfsParameter pfs)
    throws Exception;

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
   * @throws Exception the exception
   */
  public Refset getRefsetRevision(Long refsetId, Date date) throws Exception;

  /**
   * Find release revisions. This is the max revision number before the
   * finalization date of the release for releases that were published.
   *
   * @param refsetId the refset id
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findRefsetReleaseRevisions(Long refsetId)
    throws Exception;

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

  /**
   * Returns the import refset handler.
   *
   * @param key the key
   * @return the import refset handler
   * @throws Exception the exception
   */
  public ImportRefsetHandler getImportRefsetHandler(String key)
    throws Exception;

  /**
   * Returns the export refset handler.
   *
   * @param key the key
   * @return the export refset handler
   * @throws Exception the exception
   */
  public ExportRefsetHandler getExportRefsetHandler(String key)
    throws Exception;

  /**
   * Returns the import refset handler info.
   *
   * @return the import refset handler info
   * @throws Exception the exception
   */
  public IoHandlerInfoList getImportRefsetHandlerInfo() throws Exception;

  /**
   * Returns the export refset handler info.
   *
   * @return the export refset handler info
   * @throws Exception the exception
   */
  public IoHandlerInfoList getExportRefsetHandlerInfo() throws Exception;
}