/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.helpers.SearchResultList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.RefsetDescriptorRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.WorkflowListener;

/**
 * JPA enabled implementation of {@link RefsetService}.
 */
public class RefsetServiceJpa extends ProjectServiceJpa implements RefsetService {

  /**
   * Instantiates an empty {@link RefsetServiceJpa}.
   *
   * @throws Exception the exception
   */
  public RefsetServiceJpa() throws Exception {
    super();
  }

  /**
   * Refset Services.
   *
   * @param id the id
   * @return the refset
   * @throws Exception the exception
   */

  /* see superclass */
  @Override
  public Refset getRefset(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - get refset " + id);
    return getHasLastModified(id, RefsetJpa.class);
  }

  /* see superclass */
  @Override
  public Refset getRefset(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - get refset " + terminologyId + "/" + terminology
            + "/" + version);
    return getHasLastModified(terminologyId, terminology, version,
        RefsetJpa.class);
  }

  /* see superclass */
  @Override
  public Refset addRefset(Refset refset) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - add refset " + refset);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(refset.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + refset.getTerminology());
      }
      String id = idHandler.getTerminologyId(refset);
      refset.setTerminologyId(id);
    }

    // Add component
    Refset newRefset = addHasLastModified(refset);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.refsetChanged(newRefset, WorkflowListener.Action.ADD);
      }
    }
    return newRefset;
  }

  /* see superclass */
  @Override
  public void updateRefset(Refset refset) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - update refset " + refset);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(refset.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Refset refset2 = getRefset(refset.getId());
        if (!idHandler.getTerminologyId(refset).equals(
            idHandler.getTerminologyId(refset2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set refset id on update
        refset.setTerminologyId(idHandler.getTerminologyId(refset));
      }
    }
    // update component
    this.updateHasLastModified(refset);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.refsetChanged(refset, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  @Override
  public void removeRefset(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - remove refset " + id);
    // Remove the component
    Refset refset = removeHasLastModified(id, RefsetJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.refsetChanged(refset, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  @Override
  public SearchResultList findRefsetsForQuery(String terminology,
    String version, String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Refset Service - find refsets " + terminology + "/" + version + "/"
            + query);
    return getQueryResults(terminology, version, query, RefsetJpa.class,
        RefsetJpa.class, pfs);
  }


  /**
   * RefsetDescriptorRefsetMember Services.
   *
   * @param id the id
   * @return the refset descriptor ref set member
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public RefsetDescriptorRefsetMember getRefsetDescriptorRefsetMember(Long id)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - get refsetDescriptorRefsetMember " + id);
    return getHasLastModified(id, RefsetDescriptorRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public RefsetDescriptorRefsetMember getRefsetDescriptorRefsetMember(
    String terminologyId, String terminology, String version, String branch)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - get refsetDescriptorRefsetMember " + terminologyId
            + "/" + terminology + "/" + version + "/" + branch);
    return getHasLastModified(terminologyId, terminology, version,
        RefsetDescriptorRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public RefsetDescriptorRefsetMember addRefsetDescriptorRefsetMember(
    RefsetDescriptorRefsetMember refsetDescriptorRefsetMember) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - add refsetDescriptorRefsetMember "
            + refsetDescriptorRefsetMember);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler =
          getIdentifierAssignmentHandler(refsetDescriptorRefsetMember
              .getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + refsetDescriptorRefsetMember.getTerminology());
      }
      String id = idHandler.getTerminologyId(refsetDescriptorRefsetMember);
      refsetDescriptorRefsetMember.setTerminologyId(id);
    }

    // Add component
    RefsetDescriptorRefsetMember newRefsetDescriptorRefsetMember =
        addHasLastModified(refsetDescriptorRefsetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.refsetDescriptorRefsetMemberChanged(
            newRefsetDescriptorRefsetMember, WorkflowListener.Action.ADD);
      }
    }
    return newRefsetDescriptorRefsetMember;
  }

  /* see superclass */
  @Override
  public void updateRefsetDescriptorRefsetMember(
    RefsetDescriptorRefsetMember refsetDescriptorRefsetMember) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - update refsetDescriptorRefsetMember "
            + refsetDescriptorRefsetMember);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(refsetDescriptorRefsetMember
            .getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        RefsetDescriptorRefsetMember refsetDescriptorRefsetMember2 =
            getRefsetDescriptorRefsetMember(refsetDescriptorRefsetMember
                .getId());
        if (!idHandler.getTerminologyId(refsetDescriptorRefsetMember).equals(
            idHandler.getTerminologyId(refsetDescriptorRefsetMember2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set refsetDescriptorRefsetMember id on update
        refsetDescriptorRefsetMember.setTerminologyId(idHandler
            .getTerminologyId(refsetDescriptorRefsetMember));
      }
    }
    // update component
    this.updateHasLastModified(refsetDescriptorRefsetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.refsetDescriptorRefsetMemberChanged(
            refsetDescriptorRefsetMember, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  @Override
  public void removeRefsetDescriptorRefsetMember(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - remove refsetDescriptorRefsetMember " + id);
    // Remove the component
    RefsetDescriptorRefsetMember refsetDescriptorRefsetMember =
        removeHasLastModified(id, RefsetDescriptorRefsetMemberJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.refsetDescriptorRefsetMemberChanged(
            refsetDescriptorRefsetMember, WorkflowListener.Action.REMOVE);
      }
    }
  }

  @Override
  public ConceptRefsetMemberList findMembersForRefset(Long refsetId,
    String query, PfsParameter pfs) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }


  /* see superclass */
  @Override
  public ReleaseInfo getCurrentReleaseInfoForRefset(Long refsetId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get current release info for refset" + refsetId);
    List<ReleaseInfo> results =
        getReleaseHistoryForRefset(refsetId).getObjects();
    // get max release that is published and not planned
    for (int i = results.size() - 1; i >= 0; i--) {
      if (results.get(i).isPublished() && !results.get(i).isPlanned()
          && results.get(i).getTerminology().equals(refsetId)) {
        return results.get(i);
      }
    }
    return null;
  }


  /* see superclass */
  @Override
  public ReleaseInfo getPreviousReleaseInfoForRefset(Long refsetId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get previous release info for refset" + refsetId);
    List<ReleaseInfo> results =
        getReleaseHistoryForRefset(refsetId).getObjects();
    // get one before the max release that is published
    for (int i = results.size() - 1; i >= 0; i--) {
      if (results.get(i).isPublished() && !results.get(i).isPlanned()
          && results.get(i).getTerminology().equals(refsetId)) {
        if (i > 0) {
          return results.get(i - 1);
        } else {
          return null;
        }
      }
    }
    return null;
  }

  /* see superclass */
  @Override
  public ReleaseInfo getPlannedReleaseInfoForRefset(Long refsetId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get planned release info for refset" + refsetId);
    List<ReleaseInfo> results =
        getReleaseHistoryForRefset(refsetId).getObjects();
    // get one before the max release that is published
    for (int i = results.size() - 1; i >= 0; i--) {
      if (!results.get(i).isPublished() && results.get(i).isPlanned()
          && results.get(i).getTerminology().equals(refsetId)) {
        return results.get(i);
      }
    }
    return null;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ReleaseInfoList getReleaseHistoryForRefset(Long refsetId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get refset history " + refsetId);
    javax.persistence.Query query =
        manager.createQuery("select a from ReleaseInfoJpa a, "
            + " RefsetJpa b where b.id = :refsetId and "
            + "a.refset = b order by a.effectiveTime");
    /*
     * Try to retrieve the single expected result If zero or more than one
     * result are returned, log error and set result to null
     */
    try {
      query.setParameter("refsetId", refsetId);
      List<ReleaseInfo> releaseInfos = query.getResultList();
      ReleaseInfoList releaseInfoList = new ReleaseInfoListJpa();
      releaseInfoList.setObjects(releaseInfos);
      return releaseInfoList;
    } catch (NoResultException e) {
      return null;
    }
  }


  /* see superclass */
  @Override
  public ReleaseInfo addReleaseInfo(ReleaseInfo releaseInfo) throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - add release info " + releaseInfo.getName());
    if (lastModifiedFlag) {
      releaseInfo.setLastModified(new Date());
    }
    try {
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(releaseInfo);
        tx.commit();
      } else {
        manager.persist(releaseInfo);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

    return releaseInfo;
  }

  /* see superclass */
  @Override
  public void removeReleaseInfo(Long id) {
    Logger.getLogger(getClass()).debug(
        "History  Service - remove release info " + id);
    tx = manager.getTransaction();
    // retrieve this release info
    ReleaseInfo releaseInfo = manager.find(ReleaseInfoJpa.class, id);
    try {
      if (getTransactionPerOperation()) {
        // remove description
        tx.begin();
        if (manager.contains(releaseInfo)) {
          manager.remove(releaseInfo);
        } else {
          manager.remove(manager.merge(releaseInfo));
        }
        tx.commit();
      } else {
        if (manager.contains(releaseInfo)) {
          manager.remove(releaseInfo);
        } else {
          manager.remove(manager.merge(releaseInfo));
        }
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /* see superclass */
  @Override
  public void updateReleaseInfo(ReleaseInfo releaseInfo) {
    Logger.getLogger(getClass()).debug(
        "History Service - update release info " + releaseInfo.getName());
    if (lastModifiedFlag) {
      releaseInfo.setLastModified(new Date());
    }
    try {
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.merge(releaseInfo);
        tx.commit();
      } else {
        manager.merge(releaseInfo);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public Refset getRefsetRevision(Long refsetId, Date date) throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get refset revision for date :"
            + ConfigUtility.DATE_FORMAT.format(date));
    // make envers call for date = lastModifiedDate
    AuditReader reader = AuditReaderFactory.get(manager);
    List<Refset> revisions = reader.createQuery()

    // all revisions, returned as objects, not finding deleted entries
        .forRevisionsOfEntity(RefsetJpa.class, true, false)

        .addProjection(AuditEntity.revisionNumber())

        // search by id
        .add(AuditEntity.id().eq(refsetId))

        // must preceed parameter date
        .add(AuditEntity.revisionProperty("timestamp").le(date))

        // order by descending timestamp
        .addOrder(AuditEntity.property("timestamp").desc())

        // execute query
        .getResultList();

    // get the most recent of the revisions that preceed the date parameter
    Refset refset = revisions.get(0);
    handleRefsetLazyInitialization(refset);
    return refset;
  }

  /**
   * Handle refset lazy initialization.
   *
   * @param refset the refset
   */
  @SuppressWarnings("static-method")
  private void handleRefsetLazyInitialization(Refset refset) {
    // handle all lazy initializations
    refset.getProject().getName();
    refset.getRefsetDescriptor().getRefsetId();
    for (Translation translation : refset.getTranslations()) {
      translation.getDescriptionTypes().size();
      translation.getWorkflowStatus().name();
    }
  }

  /* see superclass */
  @Override
  public ConceptRefsetMemberList findMembersForRefsetRevision(Long refsetId,
    Date date, PfsParameter pfs) {
    // TODO Auto-generated method stub
    return null;
  }

}
