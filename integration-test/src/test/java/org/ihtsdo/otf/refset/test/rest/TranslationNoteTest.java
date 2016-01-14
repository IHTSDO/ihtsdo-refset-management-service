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
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.rest.client.SecurityClientRest;
import org.ihtsdo.otf.refset.rest.client.TranslationClientRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for translation.
 */
public class TranslationNoteTest extends RestSupport {

  /** The auth token. */
  private static String authToken;

  /** The translation service. */
  protected static TranslationClientRest translationService;

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
    translationService = new TranslationClientRest(properties);

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
   * Test translation note.
   *
   * @throws Exception the exception
   */
  @Test
  public void testTranslationNote() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Get translations
    Logger.getLogger(getClass()).debug("Get translations for project 3");
    TranslationList list =
        translationService.findTranslationsForQuery("projectId:3", null,
            authToken);
    assertFalse(list.getObjects().isEmpty());

    // Get first translation and add a note
    Translation translation = list.getObjects().get(0);
    Logger.getLogger(getClass())
        .debug("  translation = " + translation.getId());
    Note note =
        translationService.addTranslationNote(translation.getId(), "TEST NOTE",
            authToken);
    assertEquals("TEST NOTE", note.getValue());
    Logger.getLogger(getClass()).debug("  note = " + note);

    // Check that the get call returns the note
    translation =
        translationService.getTranslation(translation.getId(), authToken);
    assertEquals(1, translation.getNotes().size());
    assertEquals("TEST NOTE", translation.getNotes().get(0).getValue());

    // Add another note
    Note note2 =
        translationService.addTranslationNote(translation.getId(),
            "TEST NOTE2", authToken);
    assertEquals("TEST NOTE2", note2.getValue());
    Logger.getLogger(getClass()).debug("  note2 = " + note2);
    translation =
        translationService.getTranslation(translation.getId(), authToken);
    assertEquals(2, translation.getNotes().size());

    // Remove the notes
    for (Note note3 : translation.getNotes()) {
      translationService.removeTranslationNote(translation.getId(),
          note3.getId(), authToken);
    }
    translation =
        translationService.getTranslation(translation.getId(), authToken);
    assertTrue(translation.getNotes().isEmpty());

  }

  /**
   * Test translation concept note.
   *
   * @throws Exception the exception
   */
  @Test
  public void testTranslationConceptNote() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Get translations
    Logger.getLogger(getClass()).debug("Get translations for project 3");
    TranslationList list =
        translationService.findTranslationsForQuery("projectId:3", null,
            authToken);
    assertFalse(list.getObjects().isEmpty());

    // Get first concept of the first translation and add a note
    Translation translation = list.getObjects().get(0);
    Logger.getLogger(getClass())
        .debug("  translation = " + translation.getId());
    ConceptList list2 =
        translationService.findTranslationConceptsForQuery(translation.getId(),
            "", null, authToken);
    assertFalse(list2.getObjects().isEmpty());

    // Get concept to work on during test
    Concept concept = list2.getObjects().get(0);
    Logger.getLogger(getClass()).debug("  concept = " + concept.getId());
    Note note =
        translationService.addTranslationConceptNote(translation.getId(),
            concept.getId(), "TEST NOTE", authToken);
    assertEquals("TEST NOTE", note.getValue());
    Logger.getLogger(getClass()).debug("  note = " + note);

    translationService = new TranslationClientRest(properties);

    // Check that the get call returns the note
    concept =
        translationService
            .findTranslationConceptsForQuery(translation.getId(),
                "terminologyId:" + concept.getTerminologyId(), null, authToken)
            .getObjects().get(0);
    assertEquals(1, concept.getNotes().size());
    assertEquals("TEST NOTE", concept.getNotes().get(0).getValue());

    // Add another note to concept
    Note note2 =
        translationService.addTranslationConceptNote(translation.getId(),
            concept.getId(), "TEST NOTE2", authToken);
    assertEquals("TEST NOTE2", note2.getValue());
    Logger.getLogger(getClass()).debug("  note2 = " + note2);
    concept =
        translationService
            .findTranslationConceptsForQuery(translation.getId(),
                "terminologyId:" + concept.getTerminologyId(), null, authToken)
            .getObjects().get(0);
    assertEquals(2, concept.getNotes().size());

    // Remove all notes from concept
    for (Note note3 : concept.getNotes()) {
      translationService.removeTranslationConceptNote(concept.getId(),
          note3.getId(), authToken);
    }

    // Verify notes removed from concept
    concept =
        translationService
            .findTranslationConceptsForQuery(translation.getId(),
                "terminologyId:" + concept.getTerminologyId(), null, authToken)
            .getObjects().get(0);
    assertTrue(translation.getNotes().isEmpty());
  }
}
