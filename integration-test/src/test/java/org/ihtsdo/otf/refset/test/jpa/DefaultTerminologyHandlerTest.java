/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.handlers.DefaultTerminologyHandler;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Some initial testing for {@link DefaultTerminologyHandler}. Assumes stock dev
 * load.
 */
public class DefaultTerminologyHandlerTest extends JpaSupport {

  /** The project. */
  public static Project project;

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {
    project = new ProjectJpa();
    project.setTerminologyHandlerKey("TERMSERVER");
    project.setTerminologyHandlerUrl(
        "https://uat-authoring.ihtsdotools.org/snowowl/snomed-ct/v2/MAIN");
  }

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    // n/a
  }

  /**
   * Test getting concepts from Snow Owl.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetConcept() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();

    Concept concept = service.getTerminologyHandler(project)
        .getConcept("126880001", "en-edition", "20150131");
    assertTrue(concept.getName().startsWith("Neoplasm of kidney"));
    service.close();
  }

  /**
   * Test resolve expression.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResolveExpression() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();
    PfsParameter pfs = new PfsParameterJpa();
    pfs.setMaxResults(25);
    pfs.setStartIndex(5);
    ConceptList conceptList = service.getTerminologyHandler(project)
        .resolveExpression("<<284009009|Route of administration|", "en-edition",
            "20150131", pfs);
    assertEquals(143, conceptList.getTotalCount());
    service.close();
  }

  /**
   * Test getting concepts with descriptions from Snow Owl.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetFullConcept() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();

    Concept concept = service.getTerminologyHandler(project)
        .getFullConcept("126880001", "en-edition", "20150131");
    assertTrue(concept.getName().startsWith("Neoplasm of kidney"));
    assertEquals(6, concept.getDescriptions().size());
    assertEquals(2, concept.getRelationships().size());
    service.close();
  }

  /**
   * Test getting the potential current concepts for retired concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void getPotentialCurrentConceptsForRetiredConcept() throws Exception {

    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();

    ConceptList concepts = service.getTerminologyHandler(project)
        .getReplacementConcepts("150606004", "en-edition", "20150131");
    assertEquals(2, concepts.getObjects().size());
    service.close();
  }

  /**
   * Test get concept parents.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetConceptParents() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();

    ConceptList concepts = service.getTerminologyHandler(project)
        .getConceptParents("108369006", "en-edition", "20150131");
    assertEquals(1, concepts.getObjects().size());
    assertEquals(concepts.getObjects().get(0).getName(),
        "Neoplasm and/or hamartoma (morphologic abnormality)");

    service.close();
  }

  /**
   * Test get concept children.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetConceptChildren() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();

    ConceptList concepts = service.getTerminologyHandler(project)
        .getConceptChildren("108369006", "en-edition", "20150131");
    assertEquals(40, concepts.getObjects().size());

    service.close();
  }

  /**
   * Test getting concepts with descriptions from Snow Owl.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptsForQuery() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();

    PfsParameter pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(50);
    ConceptList concepts = service.getTerminologyHandler(project)
        .findConceptsForQuery("tumor", "en-edition", "20150131", pfs);
    assertEquals(50, concepts.getObjects().size());
    assertEquals(8889, concepts.getTotalCount());

    // check someing with no results
    concepts = service.getTerminologyHandler(project)
        .findConceptsForQuery("tmuor", "en-edition", "20150131", pfs);
    assertEquals(0, concepts.getObjects().size());
    service.close();
  }

  /**
   * Test edge cases.
   *
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();

    // Bogus ids, term server produces failures
    try {
      Concept c = service.getTerminologyHandler(project).getConcept("12345",
          "abc", "20150131");
      assertNull(c);
    } catch (Exception e) {
      fail("unexpected exception");
    }

    ConceptList list = service.getTerminologyHandler(project)
        .getConceptChildren("12345", "abc", "20150131");
    assertEquals(0, list.getCount());

    list = service.getTerminologyHandler(project).getConceptParents("12345",
        "abc", "20150131");
    assertEquals(0, list.getCount());

    try {
      List<String> ids = new ArrayList<>();
      ids.add("1234");
      ids.add("5679");
      list = service.getTerminologyHandler(project).getConcepts(ids, "abc",
          "20150131");
      fail("Exception expected.");
    } catch (Exception e) {
      // n/a, expected result
    }

  }

  /**
   * Test degenerate use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    ProjectService service = new ProjectServiceJpa();
    try {
      Concept c =
          service.getTerminologyHandler(project).getConcept(null, "abc", "def");
      assertNull(c);
    } catch (Exception e) {
      fail("Unexpected failure");
    }

    try {
      Concept c =
          service.getTerminologyHandler(project).getConcept("abc", null, "def");
      assertNull(c);
    } catch (Exception e) {
      fail("Unexpected failure");
    }

    try {
      service.getTerminologyHandler(project).getConcept("abcabc", "abc", null);
    } catch (Exception e) {
      fail("Exception expected.");
    }

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // n/a
  }

  /**
   * Teardown class.
   *
   * @throws Exception the exception
   */
  @AfterClass
  public static void teardownClass() throws Exception {
    // do nothing
  }

}
