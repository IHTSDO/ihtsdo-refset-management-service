/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.test.rest;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.junit.Test;

/**
 * Integration test for extensional refset editing.
 */
public class ExtensionalRefsetEditingTest extends RefsetTest {

  /**
   * Test EXTENSIONAL refset - creation, addition, adding members, removing
   * members, changing desc, remove refset
   *
   * @throws Exception the exception
   */
  @Test
  public void testRefset001() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testRefset001");

    Project project2 = projectService.getProject(2L, adminAuthToken);
    // Create refset (EXTENSIONAL)
    RefsetJpa newRefset =
        makeRefset("refset99", null, Refset.Type.EXTENSIONAL, project2, null);

    // Validate refset
    ValidationResult result =
        validationService.validateRefset(newRefset, adminAuthToken);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("Refset does not pass validation.");
    }

    // Add refset
    Refset currentRefset = refsetService.addRefset(newRefset, adminAuthToken);
    // TODO: this doesn't work, different id
    if (!currentRefset.equals(newRefset)) {
      throw new Exception("Refset does not pass equality test.");
    }

    // Workflow - Assign the refset
    currentRefset =
        getNewRefsetInRefsetList(workflowService.findAvailableEditingRefsets(
            currentRefset.getProject().getId(), testUser,
            new PfsParameterJpa(), adminAuthToken), currentRefset);

    if (currentRefset == null) {
      throw new Exception(
          "Refset not found by workflowService.findAvailableEditingRefset.");
    }

    // ASSIGN the refset to testUser
    TrackingRecord record =
        workflowService
            .performWorkflowAction(currentRefset.getProject().getId(),
                currentRefset.getId(), testUser, "ASSIGN", adminAuthToken);

    // Find the assigned refset
    currentRefset =
        getNewRefsetInRefsetList(workflowService.findAssignedEditingRefsets(
            currentRefset.getProject().getId(), testUser,
            new PfsParameterJpa(), adminAuthToken), record.getRefset());

    if (currentRefset == null) {
      throw new Exception(
          "Refset not found by workflowService.findAssignedEditingRefsets.");
    }

    // Add 5 members to refset
    ConceptRefsetMemberJpa member1 =
        makeConceptRefsetMember("member1", "123", currentRefset);
    refsetService.addRefsetMember(member1, adminAuthToken);
    ConceptRefsetMemberJpa member2 =
        makeConceptRefsetMember("member2", "12344", currentRefset);
    refsetService.addRefsetMember(member2, adminAuthToken);
    ConceptRefsetMemberJpa member3 =
        makeConceptRefsetMember("member3", "123333", currentRefset);
    refsetService.addRefsetMember(member3, adminAuthToken);
    ConceptRefsetMemberJpa member4 =
        makeConceptRefsetMember("member4", "123223", currentRefset);
    refsetService.addRefsetMember(member4, adminAuthToken);
    ConceptRefsetMemberJpa member5 =
        makeConceptRefsetMember("member5", "1234545", currentRefset);
    refsetService.addRefsetMember(member5, adminAuthToken);

    if (refsetService
        .findRefsetMembersForQuery(currentRefset.getId(), "",
            new PfsParameterJpa(), adminAuthToken).getObjects().size() != 5) {
      throw new Exception("Refset did not pass the add refset members test.");
    }

    // Remove 2 members
    refsetService.removeRefsetMember(member5.getId(), adminAuthToken);
    refsetService.removeRefsetMember(member4.getId(), adminAuthToken);

    if (refsetService
        .findRefsetMembersForQuery(currentRefset.getId(), "",
            new PfsParameterJpa(), adminAuthToken).getObjects().size() != 3) {
      throw new Exception("Refset did not pass the remove refset members test.");
    }

    // TODO: What happens to the currentRefset here as the members were removed
    // but the refset is not refreshed
    // Check it out when I step through the code

    // Change Refset Definition
    currentRefset.setDefinition("new definition");

    // Use workflowService to SAVE the refset
    record =
        workflowService.performWorkflowAction(currentRefset.getProject()
            .getId(), currentRefset.getId(), testUser, "SAVE", adminAuthToken);

    // Validate the refset
    result =
        validationService.validateRefset((RefsetJpa) record.getRefset(),
            adminAuthToken);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception(
          "Refset does not pass validation after workflow SAVE.");
    }

    currentRefset = record.getRefset();

    // Workflow - FINISH
    record =
        workflowService
            .performWorkflowAction(currentRefset.getProject().getId(),
                currentRefset.getId(), testUser, "FINISH", adminAuthToken);

    // Find available review refset
    currentRefset =
        getNewRefsetInRefsetList(workflowService.findAvailableReviewRefsets(
            currentRefset.getProject().getId(), testUser,
            new PfsParameterJpa(), adminAuthToken), record.getRefset());

    if (currentRefset == null) {
      throw new Exception(
          "Refset not found by workflowService.findAvailableReviewRefsets.");
    }

    // Assign refset for review
    record =
        workflowService
            .performWorkflowAction(currentRefset.getProject().getId(),
                currentRefset.getId(), testUser, "ASSIGN", adminAuthToken);

    // Find assigned review refset
    currentRefset =
        getNewRefsetInRefsetList(workflowService.findAssignedReviewRefsets(
            currentRefset.getProject().getId(), testUser,
            new PfsParameterJpa(), adminAuthToken), record.getRefset());

    if (currentRefset == null) {
      throw new Exception(
          "Refset not found by workflowService.findAssignedReviewRefsets.");
    }

    // Use workflowService to SAVE the refset
    record =
        workflowService.performWorkflowAction(currentRefset.getProject()
            .getId(), currentRefset.getId(), testUser, "SAVE", adminAuthToken);

    // Validate the refset
    result =
        validationService.validateRefset((RefsetJpa) record.getRefset(),
            adminAuthToken);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception(
          "Refset does not pass validation after workflow SAVE.");
    }
    currentRefset = record.getRefset();

    // FINISH - status
    record =
        workflowService
            .performWorkflowAction(currentRefset.getProject().getId(),
                currentRefset.getId(), testUser, "FINISH", adminAuthToken);

    record.getRefset().getWorkflowStatus();

    // remove refset
    refsetService.removeRefset(currentRefset.getId(), true, adminAuthToken);

    if (refsetService.getRefset(currentRefset.getId(), adminAuthToken) != null) {
      throw new Exception("Refset did not pass the remove refset test.");
    }
  }

  /**
   * Test EXTERNAL refset - creation, refset
   *
   * @throws Exception the exception
   */
  @Test
  public void testRefset002() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testRefset002");

    Project project2 = projectService.getProject(2L, adminAuthToken);
    // Create refset (EXTERNAL)
    RefsetJpa newRefset =
        makeRefset("refset98", null, Refset.Type.EXTERNAL, project2, null);

    // Validate refset
    ValidationResult result =
        validationService.validateRefset(newRefset, adminAuthToken);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("Refset does not pass validation.");
    }

    // Add refset
    RefsetJpa currentRefset =
        (RefsetJpa) refsetService.addRefset(newRefset, adminAuthToken);
    if (!currentRefset.equals(newRefset)) {
      throw new Exception("Refset does not pass equality test.");
    }
    // remove refset
    refsetService.removeRefset(currentRefset.getId(), true, adminAuthToken);

    if (refsetService.getRefset(currentRefset.getId(), adminAuthToken) != null) {
      throw new Exception("Refset did not pass the remove refset test.");
    }
  }

  /**
   * Returns the new refset in refset list.
   *
   * @param list the list
   * @param oldRefset the old refset
   * @return the new refset in refset list
   */
  @SuppressWarnings("static-method")
  private Refset getNewRefsetInRefsetList(RefsetList list, Refset oldRefset) {
    for (Refset ref : list.getObjects()) {
      if (ref.equals(oldRefset)) {
        return ref;
      }
    }
    return null;
  }

}
