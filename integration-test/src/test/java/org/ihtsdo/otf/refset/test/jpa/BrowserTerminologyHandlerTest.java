/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Terminology;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.handlers.BrowserTerminologyHandler;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Some initial testing for {@link BrowserTerminologyHandler}. Assumes stock dev
 * load.
 */
public class BrowserTerminologyHandlerTest extends JpaSupport {

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
    project.setTerminologyHandlerKey("BROWSER");
    project.setTerminologyHandlerUrl("https://sct-rest.ihtsdotools.org/api");
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
   * Test getting concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetConcept() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();

    Concept concept = service.getTerminologyHandler(project)
        .getConcept("126880001", "en-edition", "20160131");
    assertEquals(concept.getName(), "Neoplasm of kidney (disorder)");
    service.close();
  }

  /**
   * Test get replacement concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetReplacementConcepts() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();

    ConceptList list = service.getTerminologyHandler(project)
        .getReplacementConcepts("136709000", "en-edition", "20160131");
    assertEquals(list.getTotalCount(), 1);
    list = service.getTerminologyHandler(project)
        .getReplacementConcepts("150606004", "en-edition", "20160131");
    assertEquals(list.getTotalCount(), 2);

    service.close();
  }

  /**
   * Test getting concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetConcepts() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();

    List<String> concepts = new ArrayList<>();
    concepts.add("126880001");
    concepts.add("72938002");
    ConceptList conceptList = service.getTerminologyHandler(project)
        .getConcepts(concepts, "en-edition", "20160131");
    assertEquals(conceptList.getCount(), 2);
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
            "20160131", pfs);
    assertEquals(148, conceptList.getTotalCount());
    assertEquals(25, conceptList.getCount());

    // Test variations of expressions - just verify no exceptions
    // <<195967001
    // <<195967001 MINUS <<304527002
    // <<195967001 OR <<304527002
    // (<<195967001 OR <<304527002) MINUS <<370218001
    // (<<195967001 OR <<304527002) MINUS (<<370218001 OR <<389145006)
    // <<195967001 MINUS (<<370218001 OR <<389145006)
    // (<<195967001 OR <<304527002 OR <<370218001) MINUS (<<370218001 OR
    // <<389145006 OR <<195967001)

    service.getTerminologyHandler(project).resolveExpression("<<195967001",
        "en-edition", "20160131", pfs);
    service.getTerminologyHandler(project).resolveExpression(
        "<<195967001 MINUS <<304527002", "en-edition", "20160131", pfs);
    service.getTerminologyHandler(project).resolveExpression(
        "<<195967001 OR <<304527002", "en-edition", "20160131", pfs);
    service.getTerminologyHandler(project).resolveExpression(
        "(<<195967001 OR <<304527002) MINUS <<370218001", "en-edition",
        "20160131", pfs);
    service.getTerminologyHandler(project).resolveExpression(
        "(<<195967001 OR <<304527002) MINUS (<<370218001 OR <<389145006)",
        "en-edition", "20160131", pfs);
    service.getTerminologyHandler(project).resolveExpression(
        "<<195967001 MINUS (<<370218001 OR <<389145006)", "en-edition",
        "20160131", pfs);
    service.getTerminologyHandler(project).resolveExpression(
        "(<<195967001 OR <<304527002 OR <<370218001) MINUS (<<370218001 OR <<389145006 OR <<195967001)",
        "en-edition", "20160131", pfs);

    service.getTerminologyHandler(project).resolveExpression(
        "< 19829001 |disorder of lung|: 116676008 |associated morphology| = 79654002 |edema|",
        "en-edition", "20160131", pfs);

    service.getTerminologyHandler(project).resolveExpression(
        "(< 19829001 |disorder of lung|: 116676008 |associated morphology| = 79654002 |edema|) "
            + "OR <<409623005 | Respiratory insufficiency (disorder) |",
        "en-edition", "20160131", pfs);

    service.getTerminologyHandler(project).resolveExpression(
        "(< 19829001 |disorder of lung|: 116676008 |associated morphology| = 79654002 |edema|) MINUS <<304527002",
        "en-edition", "20160131", pfs);

    service.getTerminologyHandler(project).resolveExpression(
        "((< 19829001 |disorder of lung|: 116676008 |associated morphology| = 79654002 |edema|) "
            + "OR <<409623005 | Respiratory insufficiency (disorder) |) MINUS <<304527002",
        "en-edition", "20160131", pfs);

    // Test more complicated cases
    // < 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive
    // system (procedure)| OR < 233604007|Pneumonia (disorder)| MINUS <<
    // 64667001|Interstitial pneumonia (disorder)|
    service.getTerminologyHandler(project).resolveExpression(
        "(< 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive system (procedure)|) "
            + "OR (< 233604007|Pneumonia (disorder)| MINUS << 64667001|Interstitial pneumonia (disorder)|)",
        "en-edition", "20160131", pfs);

    // Complex
    String a =
        "< 65801008|Excision (procedure)| AND < 118673008|Procedure on digestive system (procedure)|";
    String b =
        "< 233604007|Pneumonia (disorder)| MINUS << 64667001|Interstitial pneumonia (disorder)|";
    service.getTerminologyHandler(project).resolveExpression(
        "((" + a + ") OR (" + a + ")) MINUS ((" + b + ") OR (" + b + "))",
        "en-edition", "20160131", pfs);

    service.getTerminologyHandler(project).resolveExpression(
        "(65801008 OR (" + a + ")) MINUS ((" + b + ") OR (" + b + "))",
        "en-edition", "20160131", pfs);

    service.getTerminologyHandler(project).resolveExpression(
        "(65801008 OR (" + a + ")) MINUS ((" + b + ") OR 233604007)",
        "en-edition", "20160131", pfs);

    service.getTerminologyHandler(project).resolveExpression(
        "65801008 OR (" + a + ") OR (" + b + ") OR 233604007", "en-edition",
        "20160131", pfs);

    service.close();
  }

  /**
   * Test count expression.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCountExpression() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();
    int ct = service.getTerminologyHandler(project).countExpression(
        "<<284009009|Route of administration|", "en-edition", "20160131");
    assertEquals(148, ct);
    service.close();
  }

  /**
   * Test get terminology versions.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTerminologyVersions() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();
    List<Terminology> terminologyList = service.getTerminologyHandler(project)
        .getTerminologyVersions("en-edition");
    assertEquals(3, terminologyList.size());
    service.close();
  }

  /**
   * Test get terminology editions.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTerminologyEditions() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();
    service.getTerminologyHandler(project).getTerminologyEditions();
    // Just making sure it doesn't fail.
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
        .getFullConcept("126880001", "en-edition", "20160131");
    assertEquals(concept.getName(), "Neoplasm of kidney (disorder)");
    assertEquals(6, concept.getDescriptions().size());
    assertEquals(2, concept.getRelationships().size());
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
        .getConceptParents("108369006", "en-edition", "20160131");
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
        .getConceptChildren("108369006", "en-edition", "20160131");
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
        .findConceptsForQuery("tumor", "en-edition", "20160131", pfs);
    assertEquals(50, concepts.getObjects().size());
    assertEquals(8985, concepts.getTotalCount());

    /*
     * concepts =
     * service.getTerminologyHandler(project).findConceptsForQuery("tumor*",
     * "en-edition", "20160131", pfs); assertEquals(50,
     * concepts.getObjects().size()); assertEquals(6897,
     * concepts.getTotalCount());
     */

    // check someing with no results
    concepts = service.getTerminologyHandler(project)
        .findConceptsForQuery("tmuor", "en-edition", "20160131", pfs);
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
      fail("Exception not expected.");
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
