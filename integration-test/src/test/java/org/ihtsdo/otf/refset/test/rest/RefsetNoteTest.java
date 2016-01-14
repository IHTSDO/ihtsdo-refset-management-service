/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.DefinitionClauseJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.rest.client.ProjectClientRest;
import org.ihtsdo.otf.refset.rest.client.RefsetClientRest;
import org.ihtsdo.otf.refset.rest.client.SecurityClientRest;
import org.ihtsdo.otf.refset.rest.client.ValidationClientRest;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for refset.
 */
public class RefsetNoteTest extends RestSupport {

  /** The auth token. */
  private static String authToken;

  /** The refset service. */
  protected static RefsetClientRest refsetService;

  /** The security service. */
  protected static SecurityClientRest securityService;

  /** The validation service. */
  protected static ValidationClientRest validationService;

  /** The project service. */
  protected static ProjectClientRest projectService;

  /** The properties. */
  protected static Properties properties;

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {

    // instantiate properties
    properties = ConfigUtility.getConfigProperties();

    // instantiate required services
    securityService = new SecurityClientRest(properties);
    refsetService = new RefsetClientRest(properties);
    validationService = new ValidationClientRest(properties);
    projectService = new ProjectClientRest(properties);
  }

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @Before
  public void setup() throws Exception {

    // authentication
    authToken =
        securityService.authenticate("author1", "author1").getAuthToken();

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @After
  public void teardown() throws Exception {

    // logout
    securityService.logout(authToken);
  }

  /**
   * Make basic refset.
   *
   * @param name the name
   * @param definition the definition
   * @param type the type
   * @param project the project
   * @param refsetId the refset id
   * @param importMembers if to import members during refset creation
   * @return the refset jpa
   * @throws Exception the exception
   */
  protected RefsetJpa makeRefset(String name, String definition,
    Refset.Type type, Project project, String refsetId, boolean importMembers)
    throws Exception {
    RefsetJpa refset = new RefsetJpa();
    refset.setActive(true);
    refset.setType(type);
    refset.setName(name);
    refset.setDescription("Description of refset " + name);
    if (type == Refset.Type.INTENSIONAL) {
      List<DefinitionClause> definitionClauses =
          new ArrayList<DefinitionClause>();
      DefinitionClause clause = new DefinitionClauseJpa();
      clause.setValue(definition);
      clause.setNegated(false);
      definitionClauses.add(clause);
      refset.setDefinitionClauses(definitionClauses);
    } else {
      refset.setDefinitionClauses(null);
    }
    refset.setExternalUrl(null);
    refset.setFeedbackEmail("***REMOVED***");
    refset.getEnabledFeedbackEvents().add(FeedbackEvent.MEMBER_ADD);
    refset.getEnabledFeedbackEvents().add(FeedbackEvent.MEMBER_REMOVE);
    refset.setForTranslation(false);
    refset.setLastModified(new Date());
    refset.setLookupInProgress(false);
    refset.setModuleId("900000000000445007");
    refset.setProject(project);
    refset.setPublishable(true);
    refset.setPublished(false);
    refset.setTerminology("SNOMEDCT");
    refset.setTerminologyId(refsetId);
    refset.setVersion("2015-01-31");
    refset.setWorkflowPath("DEFAULT");
    refset.setWorkflowStatus(WorkflowStatus.NEW);

    if (type == Refset.Type.INTENSIONAL && definition == null) {
      refset.setDefinitionClauses(new ArrayList<DefinitionClause>());
    } else if (type == Refset.Type.EXTERNAL) {
      refset.setExternalUrl("http://www.example.com/some/other/refset.txt");
    }

    // Validate refset
    ValidationResult result =
        validationService.validateRefset(refset, project.getId(), authToken);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("Refset does not pass validation.");
    }
    // Add refset
    refsetService = new RefsetClientRest(properties);

    refset = (RefsetJpa) refsetService.addRefset(refset, authToken);
    refsetService = new RefsetClientRest(properties);

    if (importMembers) {
      refset = (RefsetJpa) refsetService.getRefset(refset.getId(), authToken);

      if (type == Refset.Type.EXTENSIONAL) {
        // EXTENSIONAL Import members (from file)
        ValidationResult vr =
            refsetService.beginImportMembers(refset.getId(), "DEFAULT",
                authToken);
        if (!vr.isValid()) {
          throw new Exception("import staging is invalid - " + vr);
        }

        refsetService = new RefsetClientRest(properties);
        refset = (RefsetJpa) refsetService.getRefset(refset.getId(), authToken);

        InputStream in =
            new FileInputStream(
                new File(
                    "../config/src/main/resources/data/refset/der2_Refset_SimpleSnapshot_INT_20140731.txt"));
        refsetService.finishImportMembers(null, in, refset.getId(), "DEFAULT",
            authToken);
        in.close();
      } else if (type == Refset.Type.INTENSIONAL) {
        // Import definition (from file)
        InputStream in =
            new FileInputStream(
                new File(
                    "../config/src/main/resources/data/refset/der2_Refset_DefinitionSnapshot_INT_20140731.txt"));
        refsetService.importDefinition(null, in, refset.getId(), "DEFAULT",
            authToken);
        in.close();
      }
    }

    return (RefsetJpa) refsetService.getRefset(refset.getId(), authToken);
  }

  /**
   * Test refset note.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRefsetNote() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Get refsets
    Logger.getLogger(getClass()).debug("Get refsets for project 1");
    RefsetList list =
        refsetService.findRefsetsForQuery("projectId:1", null, authToken);
    assertFalse(list.getObjects().isEmpty());

    // Get first refset and add a note
    Refset refset = list.getObjects().get(0);
    Logger.getLogger(getClass()).debug("  refset = " + refset.getId());
    Note note =
        refsetService.addRefsetNote(refset.getId(), "TEST NOTE", authToken);
    assertEquals("TEST NOTE", note.getValue());
    Logger.getLogger(getClass()).debug("  note = " + note);

    // Check that the get call returns the note
    refset = refsetService.getRefset(refset.getId(), authToken);
    assertEquals(1, refset.getNotes().size());
    assertEquals("TEST NOTE", refset.getNotes().get(0).getValue());

    // Add another note
    Note note2 =
        refsetService.addRefsetNote(refset.getId(), "TEST NOTE2", authToken);
    assertEquals("TEST NOTE2", note2.getValue());
    Logger.getLogger(getClass()).debug("  note2 = " + note2);
    refset = refsetService.getRefset(refset.getId(), authToken);
    assertEquals(2, refset.getNotes().size());

    // Remove the notes
    for (Note note3 : refset.getNotes()) {
      refsetService.removeRefsetNote(refset.getId(), note3.getId(), authToken);
    }
    refset = refsetService.getRefset(refset.getId(), authToken);
    assertTrue(refset.getNotes().isEmpty());

  }

  /**
   * Test refset member note.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRefsetMemberNote() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Project project = projectService.getProject(1L, authToken);

    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), true);

    // Get members
    ConceptRefsetMemberList list =
        refsetService.findRefsetMembersForQuery(refset.getId(), "", null,
            authToken);
    assertFalse(list.getObjects().isEmpty());

    // Get random member
    ConceptRefsetMember member = list.getObjects().get(0);
    String memberConceptId = member.getConceptId();
    Logger.getLogger(getClass()).debug("  member = " + member.getId());

    // Create Note
    Note note =
        refsetService.addRefsetMemberNote(refset.getId(), member.getId(),
            "TEST NOTE", authToken);
    assertEquals("TEST NOTE", note.getValue());
    Logger.getLogger(getClass()).debug("  note = " + note);

    // Check that the get call returns the note
    member =
        refsetService
            .findRefsetMembersForQuery(refset.getId(),
                "conceptId:" + memberConceptId, null, authToken).getObjects()
            .get(0);
    assertEquals(1, member.getNotes().size());
    assertEquals("TEST NOTE", member.getNotes().get(0).getValue());

    // Add another note
    Note note2 =
        refsetService.addRefsetMemberNote(refset.getId(), member.getId(),
            "TEST NOTE2", authToken);
    assertEquals("TEST NOTE2", note2.getValue());
    Logger.getLogger(getClass()).debug("  note2 = " + note2);
    member =
        refsetService
            .findRefsetMembersForQuery(refset.getId(),
                "conceptId:" + memberConceptId, null, authToken).getObjects()
            .get(0);
    assertEquals(2, member.getNotes().size());

    // Remove all notes
    for (Note note3 : member.getNotes()) {
      refsetService.removeRefsetMemberNote(member.getId(), note3.getId(),
          authToken);
    }

    // Ensure notes is now empty
    member =
        refsetService
            .findRefsetMembersForQuery(refset.getId(),
                "conceptId:" + memberConceptId, null, authToken).getObjects()
            .get(0);
    assertTrue(refset.getNotes().isEmpty());
  }
}
