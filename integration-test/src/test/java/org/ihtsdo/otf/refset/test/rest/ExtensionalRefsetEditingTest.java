/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;
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
  public void testExtensionalRefset() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(2L, adminAuthToken);

    
    UserJpa reviewer1 =
        (UserJpa) securityService.getUser("reviewer1", adminAuthToken);
    projectService.assignUserToProject(project.getId(),
        reviewer1.getUserName(), UserRole.REVIEWER.toString(), adminAuthToken);

    // Create refset (EXTENSIONAL)
    Refset currentRefset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, null,
            false);

    assertTrue(currentRefset != null);

    // Workflow - Assign the refset
    currentRefset =
        getNewRefsetInRefsetList(workflowService.findAvailableEditingRefsets(
            currentRefset.getProject().getId(), testUser,
            new PfsParameterJpa(), adminAuthToken), currentRefset);
    assertTrue(currentRefset != null);

    // ASSIGN the refset to testUser
    TrackingRecord record =
        workflowService.performWorkflowAction(currentRefset.getProject()
            .getId(), currentRefset.getId(), testUser, "AUTHOR", "ASSIGN",
            adminAuthToken);

    // Find the assigned refset
    currentRefset =
        getNewRefsetInTrackingRecordList(
            workflowService.findAssignedEditingRefsets(currentRefset
                .getProject().getId(), testUser, new PfsParameterJpa(),
                adminAuthToken), record.getRefset());
    assertTrue(currentRefset != null);

    // Add 5 members to refset
    ConceptRefsetMemberJpa member1 =
        makeConceptRefsetMember("member1", "123", currentRefset);
    member1 = (ConceptRefsetMemberJpa) refsetService.addRefsetMember(member1, adminAuthToken);
    ConceptRefsetMemberJpa member2 =
        makeConceptRefsetMember("member2", "12344", currentRefset);
    member2 = (ConceptRefsetMemberJpa) refsetService.addRefsetMember(member2, adminAuthToken);
    ConceptRefsetMemberJpa member3 =
        makeConceptRefsetMember("member3", "123333", currentRefset);
    member3 = (ConceptRefsetMemberJpa) refsetService.addRefsetMember(member3, adminAuthToken);
    ConceptRefsetMemberJpa member4 =
        makeConceptRefsetMember("member4", "123223", currentRefset);
    member4 = (ConceptRefsetMemberJpa) refsetService.addRefsetMember(member4, adminAuthToken);
    ConceptRefsetMemberJpa member5 =
        makeConceptRefsetMember("member5", "1234545", currentRefset);
    member5 = (ConceptRefsetMemberJpa) refsetService.addRefsetMember(member5, adminAuthToken);

    List<ConceptRefsetMember> addedMembers =
        refsetService.findRefsetMembersForQuery(currentRefset.getId(), "",
            new PfsParameterJpa(), adminAuthToken).getObjects();
    assertEquals(5, addedMembers.size());

    // Remove 2 members
    refsetService.removeRefsetMember(member5.getId(), adminAuthToken);
    refsetService.removeRefsetMember(member4.getId(), adminAuthToken);

    List<ConceptRefsetMember> currentMembers =
        refsetService.findRefsetMembersForQuery(currentRefset.getId(), "",
            new PfsParameterJpa(), adminAuthToken).getObjects();
    assertEquals(3, currentMembers.size());

    // Change Refset Definition
    currentRefset.setDefinitionClauses(null);

    // Use workflowService to SAVE the refset
    record =
        workflowService.performWorkflowAction(currentRefset.getProject()
            .getId(), currentRefset.getId(), testUser, "AUTHOR", "SAVE",
            adminAuthToken);

    // Validate the refset
    ValidationResult result =
        validationService.validateRefset((RefsetJpa) record.getRefset(),
            project.getId(), adminAuthToken);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception(
          "Refset does not pass validation after workflow SAVE.");
    }

    currentRefset = record.getRefset();

    // Workflow - FINISH
    record =
        workflowService.performWorkflowAction(currentRefset.getProject()
            .getId(), currentRefset.getId(), testUser, "AUTHOR", "FINISH",
            adminAuthToken);

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
        workflowService.performWorkflowAction(currentRefset.getProject()
            .getId(), currentRefset.getId(), reviewer1.getUserName(), "REVIEWER", "ASSIGN",
            adminAuthToken);

    // Find assigned review refset
    currentRefset =
        getNewRefsetInTrackingRecordList(
            workflowService.findAssignedReviewRefsets(currentRefset
                .getProject().getId(), reviewer1.getUserName(), new PfsParameterJpa(),
                adminAuthToken), record.getRefset());

    assertTrue(currentRefset != null);

    // Use workflowService to SAVE the refset
    record =
        workflowService.performWorkflowAction(currentRefset.getProject()
            .getId(), currentRefset.getId(), reviewer1.getUserName(), "REVIEWER", "SAVE",
            adminAuthToken);

    // Validate the refset
    result =
        validationService.validateRefset((RefsetJpa) record.getRefset(),
            project.getId(), adminAuthToken);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception(
          "Refset does not pass validation after workflow SAVE.");
    }
    currentRefset = record.getRefset();

    // FINISH - status
    record =
        workflowService.performWorkflowAction(currentRefset.getProject()
            .getId(), currentRefset.getId(), reviewer1.getUserName(), "REVIEWER", "FINISH",
            adminAuthToken);

    // remove refset
    workflowService.performWorkflowAction(project.getId(), currentRefset.getId(),
        reviewer1.getUserName(), "REVIEWER", "UNASSIGN", adminAuthToken);
    workflowService.performWorkflowAction(project.getId(), currentRefset.getId(),
        testUser, "AUTHOR", "UNASSIGN", adminAuthToken);

    verifyRefsetLookupCompleted(currentRefset.getId());
    refsetService.removeRefset(currentRefset.getId(), true, adminAuthToken);
  }

  /**
   * Test EXTERNAL refset - creation, refset
   *
   * @throws Exception the exception
   */
  @Test
  public void testExternalRefset() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    // Create refset (EXTERNAL)
    RefsetJpa currentRefset =
        makeRefset("refset998", null, Refset.Type.EXTERNAL, project2, "998",
            true);

    assertTrue(currentRefset != null);

    // remove refset
    verifyRefsetLookupCompleted(currentRefset.getId());
    refsetService.removeRefset(currentRefset.getId(), true, adminAuthToken);

    Refset pulledRefset = null;

    pulledRefset =
        refsetService.getRefset(currentRefset.getId(), adminAuthToken);

    assertTrue(pulledRefset == null);
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

  /**
   * Returns the new refset in tracking record list.
   *
   * @param list the list
   * @param oldRefset the old refset
   * @return the new refset in tracking record list
   */
  @SuppressWarnings("static-method")
  private Refset getNewRefsetInTrackingRecordList(TrackingRecordList list,
    Refset oldRefset) {
    for (TrackingRecord record : list.getObjects()) {
      if (record.getRefset().equals(oldRefset)) {
        return record.getRefset();
      }
    }
    return null;
  }

}
