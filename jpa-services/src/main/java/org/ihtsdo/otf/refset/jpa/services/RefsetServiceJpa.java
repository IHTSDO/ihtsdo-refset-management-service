/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.SearchResultList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.RefsetDescriptorRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.WorkflowListener;

/**
 * JPA enabled implementation of {@link RefsetService}.
 */
public class RefsetServiceJpa extends ProjectServiceJpa implements
    RefsetService {

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
    Logger.getLogger(getClass()).debug("Refset Service - add refset " + refset);
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
  @SuppressWarnings("unchecked")
  @Override
  public RefsetList findRefsetsForQuery(String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Refset Service - find refsets " + query);
    int[] totalCt = new int[1];
    List<Refset> list = (List<Refset>)getQueryResults(query == null || query.isEmpty()
        ? "id:[* TO *]" : query,  RefsetJpa.class,
        RefsetJpa.class, pfs, totalCt);
    RefsetList result = new RefsetListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
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
  @SuppressWarnings("unchecked")
  @Override
  public Refset getRefsetRevision(Long refsetId, Date date) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - get refset revision for date :"
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

  @Override
  public SearchResultList findRefsetReleaseRevisions(Long refsetId)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
