/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.MemberDiffReport;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.jpa.DefinitionClauseJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rest.client.RefsetClientRest;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.junit.Test;

/**
 * Test case for refset.
 */
public class RefsetTest extends RefsetTestSupport {

  /**
   * Test getting a specific member from a refset.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetMember() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(3L, adminAuthToken);

    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), true);

    refsetService = new RefsetClientRest(properties);

    // Create a new Member and add to refset
    ConceptRefsetMemberJpa createdMember =
        makeConceptRefsetMember("TestMember", "1234567", refset);
    createdMember =
        (ConceptRefsetMemberJpa) refsetService.addRefsetMember(createdMember,
            adminAuthToken);

    // With new member's Id, pull member via getMember()
    Long memberId = createdMember.getId();
    ConceptRefsetMemberJpa pulledMember =
        (ConceptRefsetMemberJpa) refsetService.getMember(memberId,
            adminAuthToken);

    assertEquals(createdMember, pulledMember);

    // clean up
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test adding a member to a refset via an expression
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddRefsetMembersForExpression() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(3L, adminAuthToken);

    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), true);

    refsetService = new RefsetClientRest(properties);
    refset =
        (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);

    // Verify number of members to begin with
    List<ConceptRefsetMember> foundMembers =
        refsetService.findRefsetMembersForQuery(refset.getId(), "", null, false, false,
            new PfsParameterJpa(), adminAuthToken).getObjects();

    assertEquals(21, foundMembers.size());

    // Add Concept Id expression
    String expression = "284009009";
    ConceptRefsetMemberList updateMembers =
        refsetService.addRefsetMembersForExpression(refset.getId(), expression,
            adminAuthToken);

    // Verify new member created from concept specified
    assertEquals(1, updateMembers.getCount());
    ConceptRefsetMember updateMember = updateMembers.getObjects().get(0);
    assertEquals("284009009", updateMember.getConceptId());
    assertEquals("Route of administration value (qualifier value)",
        updateMember.getConceptName());

    // Verify number of members in refset has increased due to new member
    foundMembers =
        refsetService.findRefsetMembersForQuery(refset.getId(), "", null, false, false,
            new PfsParameterJpa(), adminAuthToken).getObjects();
    assertEquals(22, foundMembers.size());

    // clean up
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test removing a refset exclusion from a refset
   *
   * @throws Exception the exception
   */
  @Test
  public void testRemoveRefsetExclusion() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(3L, adminAuthToken);

    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.INTENSIONAL, project, null, true);

    // Get all members
    ConceptRefsetMemberList originalMemberList =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", null, false, false, new PfsParameterJpa(), adminAuthToken);
    assertEquals(143, originalMemberList.getCount());

    // Identify member to exclude
    ConceptRefsetMemberList memberToRemove =
        refsetService.findRefsetMembersForQuery(refset.getId(), "429817007",null, false, false,
            new PfsParameterJpa(), adminAuthToken);

    // Add exclusion and verify refset members have decreased
    refsetService.addRefsetExclusion(refset.getId(), "429817007", false,
        adminAuthToken);
    ConceptRefsetMemberList removedMemberList =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", null, false, false, new PfsParameterJpa(), adminAuthToken);
    assertEquals(142, removedMemberList.getCount());

    // Remove exclusion and verify refset members have increased
    ConceptRefsetMember exclusionRemovedMember =
        refsetService.removeRefsetExclusion(memberToRemove.getObjects().get(0)
            .getId(), adminAuthToken);
    assertEquals(exclusionRemovedMember.getConceptId(), "429817007");
    assertEquals("Interstitial route (qualifier value)",
        exclusionRemovedMember.getConceptName());
    ConceptRefsetMemberList finalMemberList =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", null, false, false, new PfsParameterJpa(), adminAuthToken);
    assertEquals(143, finalMemberList.getObjects().size());

    // clean up
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test optomizing a refset definition with a redundant subsumption clauses
   *
   * @throws Exception the exception
   */
  @Test
  public void testOptimizeDefinition() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(3L, adminAuthToken);
    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.INTENSIONAL, project, null,
            false);

    // Populate contents with clause
    DefinitionClause clause = new DefinitionClauseJpa();
    clause.setValue("<<284009009|Route of administration|");
    clause.setNegated(false);
    refset.getDefinitionClauses().add(clause);
    refsetService.updateRefset(refset, adminAuthToken);
    refset =
        (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);
    ConceptRefsetMemberList members =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", null, false, false, new PfsParameterJpa(), adminAuthToken);
    assertEquals(143, members.getCount());

    // Add 2nd clause that is based on concept that is child of original
    // concept's clause
    clause = new DefinitionClauseJpa();
    clause.setValue("<<6064005 | Topical route (qualifier value) |");
    clause.setNegated(false);
    refset.getDefinitionClauses().add(clause);
    refsetService.updateRefset(refset, adminAuthToken);
    refset =
        (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);
    members =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", null, false, false, new PfsParameterJpa(), adminAuthToken);
    assertEquals(143, members.getCount());
    assertEquals(2, refset.getDefinitionClauses().size());

    // Add a negated clause
    clause = new DefinitionClauseJpa();
    clause.setValue("<<372457001 | Gingival route (qualifier value) |");
    clause.setNegated(true);
    refset.getDefinitionClauses().add(clause);
    refsetService.updateRefset(refset, adminAuthToken);
    refset =
        (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);
    members =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", null, false, false, new PfsParameterJpa(), adminAuthToken);
    assertEquals(141, members.getCount());
    assertEquals(3, refset.getDefinitionClauses().size());

    // Add 2nd negated clause that is based on concept that is child of original
    // concept's clause
    clause = new DefinitionClauseJpa();
    clause.setValue("<<419601003 | Subgingival route (qualifier value) |");
    clause.setNegated(true);
    refset.getDefinitionClauses().add(clause);
    refsetService.updateRefset(refset, adminAuthToken);
    refset =
        (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);
    members =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", null, false, false, new PfsParameterJpa(), adminAuthToken);
    assertEquals(141, members.getCount());
    assertEquals(4, refset.getDefinitionClauses().size());

    // After Optimize, should turn into 2 clauses (one positive & one negated)
    refsetService.optimizeDefinition(refset.getId(), adminAuthToken);
    Refset optomizedRefset =
        refsetService.getRefset(refset.getId(), adminAuthToken);
    assertEquals(2, optomizedRefset.getDefinitionClauses().size());
    members =
        refsetService.findRefsetMembersForQuery(optomizedRefset.getId(),
            "memberType:MEMBER", null, false, false, new PfsParameterJpa(), adminAuthToken);
    assertEquals(141, members.getCount());

    int posClauses = 0;
    int negClauses = 0;
    for (DefinitionClause optimizedClause : optomizedRefset
        .getDefinitionClauses()) {
      if (optimizedClause.isNegated()) {
        negClauses++;
      } else {
        posClauses++;
      }
    }

    assertEquals(1, negClauses);
    assertEquals(1, posClauses);

    // clean up
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test refset lookup for the case that no members are added nor looked-up.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRefsetLookupNoMembers() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(3L, adminAuthToken);

    // Create refset (extensional) and do not import definition
    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), false);

    refsetService = new RefsetClientRest(properties);

    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test getting both old and new regular members.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetOldNewRegularMembers() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(3L, adminAuthToken);

    // Create refset (extensional) and import definition
    RefsetJpa janRefset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), true);

    refsetService = new RefsetClientRest(properties);

    // Begin migration
    Refset julyStagedRefset =
        refsetService.beginMigration(janRefset.getId(), "en-edition",
            "20150731", adminAuthToken);

    // Create Report with identical content
    // Thus Old & New the same size
    String reportToken =
        refsetService.compareRefsets(janRefset.getId(),
            julyStagedRefset.getId(), adminAuthToken);

    ConceptRefsetMemberList oldRegularMembers =
        refsetService.getOldRegularMembers(reportToken, "", null, null,
            adminAuthToken);
    ConceptRefsetMemberList newRegularMembers =
        refsetService.getNewRegularMembers(reportToken, "", null, null,
            adminAuthToken);

    assertEquals(0, oldRegularMembers.getCount());
    assertEquals(0, newRegularMembers.getCount());

    // Add member to July refset and regenerate report
    // Thus New has an extra member
    ConceptRefsetMemberJpa createdMember =
        makeConceptRefsetMember("TestMember", "1234567", julyStagedRefset);
    createdMember =
        (ConceptRefsetMemberJpa) refsetService.addRefsetMember(createdMember,
            adminAuthToken);
    refsetService.releaseReportToken(reportToken, adminAuthToken);

    reportToken =
        refsetService.compareRefsets(janRefset.getId(),
            julyStagedRefset.getId(), adminAuthToken);

    oldRegularMembers =
        refsetService.getOldRegularMembers(reportToken, "", null, null,
            adminAuthToken);
    newRegularMembers =
        refsetService.getNewRegularMembers(reportToken, "", null, null,
            adminAuthToken);

    assertEquals(0, oldRegularMembers.getCount());
    assertEquals(1, newRegularMembers.getCount());
    assertEquals(createdMember.getConceptId(), newRegularMembers.getObjects()
        .get(0).getConceptId());
    assertEquals("TestMember", newRegularMembers
        .getObjects().get(0).getConceptName());

    // Add identical member to Jan refset and regenerate report
    // Thus Old & New again the same size
    ConceptRefsetMemberJpa createdMember2 =
        makeConceptRefsetMember("TestMember", "1234567", janRefset);
    // ConceptRefsetMember addIdenticalMember =
    refsetService.addRefsetMember(createdMember2, adminAuthToken);
    refsetService.releaseReportToken(reportToken, adminAuthToken);

    reportToken =
        refsetService.compareRefsets(janRefset.getId(),
            julyStagedRefset.getId(), adminAuthToken);

    oldRegularMembers =
        refsetService.getOldRegularMembers(reportToken, "", null, null,
            adminAuthToken);
    newRegularMembers =
        refsetService.getNewRegularMembers(reportToken, "", null, null,
            adminAuthToken);

    assertEquals(0, oldRegularMembers.getCount());
    assertEquals(0, newRegularMembers.getCount());
    refsetService.releaseReportToken(reportToken, adminAuthToken);

    // Add another but unique member to Jan refset and regenerate report
    // Thus Old has an extra member
    ConceptRefsetMemberJpa createdMember3 =
        makeConceptRefsetMember("TestMember3", "12345673", janRefset);
    createdMember =
        (ConceptRefsetMemberJpa) refsetService.addRefsetMember(createdMember3,
            adminAuthToken);
    refsetService.releaseReportToken(reportToken, adminAuthToken);

    reportToken =
        refsetService.compareRefsets(janRefset.getId(),
            julyStagedRefset.getId(), adminAuthToken);

    oldRegularMembers =
        refsetService.getOldRegularMembers(reportToken, "", null, null,
            adminAuthToken);
    newRegularMembers =
        refsetService.getNewRegularMembers(reportToken, "", null, null,
            adminAuthToken);
    refsetService.releaseReportToken(reportToken, adminAuthToken);

    assertEquals(1, oldRegularMembers.getCount());
    assertEquals(0, newRegularMembers.getCount());
    assertEquals(createdMember3.getConceptId(), oldRegularMembers.getObjects()
        .get(0).getConceptId());
    assertEquals("TestMember3", oldRegularMembers
        .getObjects().get(0).getConceptName());
    refsetService.releaseReportToken(reportToken, adminAuthToken);

    // cleanup
    // refsetService.finishMigration(janRefset.getId(), adminAuthToken);
    refsetService.cancelMigration(janRefset.getId(), adminAuthToken);
    refsetService.removeRefset(janRefset.getId(), true, adminAuthToken);
  }

  /**
   * Test adding a member to a refset via an expression
   *
   * @throws Exception the exception
   */
  /*
   * @Test public void testRecoveryRefset() throws Exception {
   * Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
   * 
   * Project project = projectService.getProject(3L, adminAuthToken);
   * 
   * User admin = securityService.authenticate(adminUser, adminPassword);
   * 
   * RefsetJpa refset = makeRefset("refset", null, Refset.Type.EXTENSIONAL,
   * project, UUID .randomUUID().toString(), true);
   * 
   * refsetService = new RefsetClientRest(properties); refset = (RefsetJpa)
   * refsetService.getRefset(refset.getId(), adminAuthToken);
   * 
   * // Verify number of members to begin with List<ConceptRefsetMember>
   * foundMembers = refsetService.findRefsetMembersForQuery(refset.getId(), "",
   * new PfsParameterJpa(), adminAuthToken).getObjects();
   * 
   * assertEquals(21, foundMembers.size());
   * 
   * // Create translation TranslationJpa translation =
   * makeTranslation("translation", refset, project, admin);
   * 
   * translationService.removeTranslation(translation.getId(), true,
   * adminAuthToken);
   * 
   * refsetService.removeRefset(refset.getId(), true, adminAuthToken);
   * 
   * Refset recoveryRefset = refsetService.recoverRefset(refset.getProjectId(),
   * refset.getId(), adminAuthToken);
   * 
   * // Verify number of members recovered foundMembers =
   * refsetService.findRefsetMembersForQuery(recoveryRefset.getId(), "", new
   * PfsParameterJpa(), adminAuthToken).getObjects();
   * 
   * assertEquals(21, foundMembers.size()); }
   */

  /**
   * Test obtaining nonexistent refset returns null gracefully
   *
   * @throws Exception the exception
   */
  @Test
  public void testNonexistentRefsetAccess() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Refset refset =
        refsetService.getRefset(123456789123456789L, adminAuthToken);
    assertNull(refset);
  }

  /**
   * Test obtaining nonexistent member
   *
   * @throws Exception the exception
   */
  @Test
  public void testNonexistentRefsetMemberAccess() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    ConceptRefsetMember member =
        refsetService.getMember(1234567890L, adminAuthToken);
    assertNull(member);
  }

  /**
   * Test full migration of intensional refset and all reportToken-based methods
   *
   * @throws Exception the exception
   */
  @Test
  public void testFullMigration() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(3L, adminAuthToken);

    // Create refset (intensional) and add definition
    Logger.getLogger(getClass()).debug("  create refset");
    Refset janRefset =
        makeRefset("refset", null, Refset.Type.INTENSIONAL, project, null, true);

    DefinitionClause clause = new DefinitionClauseJpa();
    clause.setValue("<<70759006 | Pyoderma (disorder) |");
    clause.setNegated(false);
    janRefset.getDefinitionClauses().add(clause);
    Logger.getLogger(getClass()).debug("  change definition");
    refsetService.updateRefset((RefsetJpa) janRefset, adminAuthToken);
    refsetService = new RefsetClientRest(properties);

    janRefset = refsetService.getRefset(janRefset.getId(), adminAuthToken);

    // Begin migration
    Logger.getLogger(getClass()).debug("  begin migration");
    Refset julyStagedRefset =
        refsetService.beginMigration(janRefset.getId(), "en-edition",
            "20150731", adminAuthToken);

    // Obtain reportToken via compareRefsets
    Logger.getLogger(getClass()).debug("  compare refsets");
    String reportToken =
        refsetService.compareRefsets(janRefset.getId(),
            julyStagedRefset.getId(), adminAuthToken);

    // Verify common members as expected
    Logger.getLogger(getClass()).debug("  get members in common");
    ConceptRefsetMemberList commonMembers =
        refsetService.findMembersInCommon(reportToken, "", null, null,
            adminAuthToken);
    assertEquals(254, commonMembers.getCount());

    // Verify diffReport as expected
    Logger.getLogger(getClass()).debug("  get diff report");
    MemberDiffReport diffReport =
        refsetService.getDiffReport(reportToken, adminAuthToken);
    assertEquals(7, diffReport.getOldNotNew().size());
    assertEquals(56, diffReport.getNewNotOld().size());

    // Verify oldNewRegular Member Calls as expected
    ConceptRefsetMemberList oldRegularMembers =
        refsetService.getOldRegularMembers(reportToken, "", null, null,
            adminAuthToken);
    ConceptRefsetMemberList newRegularMembers =
        refsetService.getNewRegularMembers(reportToken, "", null, null,
            adminAuthToken);

    assertEquals(diffReport.getOldNotNew().size(), oldRegularMembers.getCount());
    assertEquals(diffReport.getNewNotOld().size(), newRegularMembers.getCount());

    // Complete process
    Logger.getLogger(getClass()).debug("  release token and finish migration");
    refsetService.releaseReportToken(reportToken, adminAuthToken);

    // cleanup
    refsetService.finishMigration(janRefset.getId(), adminAuthToken);

    refsetService.removeRefset(janRefset.getId(), true, adminAuthToken);
  }

}
