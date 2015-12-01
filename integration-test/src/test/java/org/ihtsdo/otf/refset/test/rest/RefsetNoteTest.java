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

import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.rest.client.RefsetClientRest;
import org.ihtsdo.otf.refset.rest.client.SecurityClientRest;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for refset.
 */
public class RefsetNoteTest extends RestIntegrationSupport {

  /** The auth token. */
  private static String authToken;

  /** The refset service. */
  protected static RefsetClientRest refsetService;

  /** The security service. */
  protected static SecurityClientRest securityService;

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

    // Get refsets
    Logger.getLogger(getClass()).debug("Get refsets for project 1");
    RefsetList list =
        refsetService.findRefsetsForQuery("projectId:1", null, authToken);
    assertFalse(list.getObjects().isEmpty());

    // Get first member of the first refset and add a note
    Refset refset = list.getObjects().get(0);
    Logger.getLogger(getClass()).debug("  refset = " + refset.getId());
    ConceptRefsetMemberList list2 =
        refsetService.findRefsetMembersForQuery(refset.getId(), "", null,
            authToken);
    assertFalse(list2.getObjects().isEmpty());
    ConceptRefsetMember member = list2.getObjects().get(0);
    Logger.getLogger(getClass()).debug("  member = " + member.getId());
    Note note =
        refsetService.addRefsetMemberNote(refset.getId(), member.getId(),
            "TEST NOTE", authToken);
    assertEquals("TEST NOTE", note.getValue());
    Logger.getLogger(getClass()).debug("  note = " + note);

    // Check that the get call returns the note
    member =
        refsetService
            .findRefsetMembersForQuery(refset.getId(), "", null, authToken)
            .getObjects().get(0);
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
            .findRefsetMembersForQuery(refset.getId(), "", null, authToken)
            .getObjects().get(0);
    assertEquals(2, member.getNotes().size());

    // Remove the notes
    for (Note note3 : member.getNotes()) {
      refsetService.removeRefsetMemberNote(refset.getId(), note3.getId(),
          authToken);
    }
    member =
        refsetService
            .findRefsetMembersForQuery(refset.getId(), "", null, authToken)
            .getObjects().get(0);
    assertTrue(refset.getNotes().isEmpty());

  }
}
