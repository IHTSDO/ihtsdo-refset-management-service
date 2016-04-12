/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Implementation of the "Project Service REST Concept Queries" Test Cases.
 */
public class ProjectConceptQueryTest extends ProjectTestSupport {

  /** The viewer auth token. */
  private static String viewerAuthToken;

  /** The admin auth token. */
  private static String adminAuthToken;

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // authentication
    viewerAuthToken =
        securityService.authenticate(testUser, testPassword).getAuthToken();
    adminAuthToken =
        securityService.authenticate(adminUser, adminPassword).getAuthToken();
  }

  /**
   * Test retrieving concepts, their children, and their parents
   *
   * @throws Exception the exception
   */
  @Test
  public void testConceptAccess() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // verify able to retrieve expected concept via direct call
    Concept concept =
        projectService.getFullConcept("406473004", "en-edition", "20150131",
            null, adminAuthToken);
    assertNotNull(concept);
    assert(concept.getName().startsWith("Contact allergen"));

    // verify able to retrieve expected concept via query
    ConceptList conceptList =
        projectService.findConceptsForQuery("406473004", "en-edition",
            "20150131", null, adminAuthToken);
    assertEquals(1, conceptList.getCount());
    assertTrue(concept.getName().startsWith("Contact allergen"));

    // verify able to retrieve expected children
    conceptList =
        projectService.getConceptChildren("406473004", "en-edition",
            "20150131", null, null, adminAuthToken);
    assertTrue(conceptList.getCount() > 0);

    boolean foundExpectedChild = false;
    for (Concept con : conceptList.getObjects()) {
      if (con.getTerminologyId().equals("53034005")
          && con.getName().startsWith("Coal tar")) {
        foundExpectedChild = true;
        break;
      }
    }
    assertTrue(foundExpectedChild);

    // verify able to retrieve expected children
    conceptList =
        projectService.getConceptParents("406473004", "en-edition", "20150131",
            null, adminAuthToken);
    assertTrue(conceptList.getCount() > 0);

    boolean foundExpectedParent = false;
    for (Concept con : conceptList.getObjects()) {
      if (con.getTerminologyId().equals("406455002")
          && con.getName().startsWith("Allergen class")) {
        foundExpectedParent = true;
        break;
      }
    }
    assertTrue(foundExpectedParent);
  }

  /**
   * Test retrieving nonexistent concepts, their children, and their parents
   * 
   * @throws Exception the exception
   */
  @Test
  public void testNonexistentProjectConceptAccess() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Concept doesn't exist thus should return null
    Concept concept =
        projectService.getFullConcept("1234567890", "en-edition", "20150131",
            null, adminAuthToken);
    assertNull(concept);

    // Concept doesn't exist thus should return empty list
    ConceptList conceptList =
        projectService.findConceptsForQuery("1234567890", "en-edition",
            "20150131", null, adminAuthToken);
    assertEquals(0, conceptList.getCount());

    // Concept doesn't exist thus should return empty list
    conceptList =
        projectService.getConceptChildren("1234567890", "en-edition",
            "20150131", null, null, adminAuthToken);
    assertEquals(0, conceptList.getCount());

    // Concept doesn't exist thus should return empty list
    conceptList =
        projectService.getConceptParents("1234567890", "en-edition",
            "20150131", null, adminAuthToken);
    assertEquals(0, conceptList.getCount());
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // logout
    securityService.logout(viewerAuthToken);
    securityService.logout(adminAuthToken);
  }

}
