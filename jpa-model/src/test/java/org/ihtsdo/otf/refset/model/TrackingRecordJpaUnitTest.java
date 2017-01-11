/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.model;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.UserPreferences;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.UserPreferencesJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IndexedFieldTester;
import org.ihtsdo.otf.refset.jpa.helpers.NullableFieldTester;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecordJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link TrackingRecordJpa}.
 */
public class TrackingRecordJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private TrackingRecordJpa object;

  /** the test fixture t1 */
  private Translation t1;

  /** the test fixture t2 */
  private Translation t2;

  /** the test fixture r1 */
  private Refset r1;

  /** the test fixture r2 */
  private Refset r2;

  /** the test fixture c1 */
  private Concept c1;

  /** the test fixture c2 */
  private Concept c2;

  /** the test fixture p1 */
  private Project p1;

  /** the test fixture p2 */
  private Project p2;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    object = new TrackingRecordJpa();

    ProxyTester tester = new ProxyTester(new TranslationJpa());
    t1 = (TranslationJpa) tester.createObject(1);
    t2 = (TranslationJpa) tester.createObject(2);

    tester = new ProxyTester(new RefsetJpa());
    r1 = (RefsetJpa) tester.createObject(1);
    r2 = (RefsetJpa) tester.createObject(2);
    r1.setId(1L);
    r2.setId(2L);

    tester = new ProxyTester(new ConceptJpa());
    c1 = (ConceptJpa) tester.createObject(1);
    c2 = (ConceptJpa) tester.createObject(2);

    tester = new ProxyTester(new ProjectJpa());
    p1 = (ProjectJpa) tester.createObject(1);
    p2 = (ProjectJpa) tester.createObject(2);

    r1.setProject(p1);
    r2.setProject(p2);
    t1.setProject(p1);
    t2.setProject(p2);
    t1.setRefset(r1);
    t2.setRefset(r2);
    r1.getTranslations().add(t1);
    r2.getTranslations().add(t2);
    t1.getConcepts().add(c1);
    t2.getConcepts().add(c2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("refsetId");
    tester.exclude("translationId");
    tester.exclude("conceptId");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    // EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    // TODO: get this working - havng trouble because of setRefsetId and
    // setConceptId
    // if one or the other it would be fine.
    //
    // tester.include("conceptId");
    // tester.include("refsetId");
    // tester.include("forAuthoring");
    // tester.include("forReview");
    // tester.include("revision");
    // tester.include("authors");
    // tester.include("reviewers");
    // tester.exclude("concept");
    // tester.exclude("refset");
    //
    // tester.proxy(Translation.class, 1, t1);
    // tester.proxy(Translation.class, 2, t2);
    // tester.proxy(Refset.class, 1, r1);
    // tester.proxy(Refset.class, 2, r2);
    // tester.proxy(Concept.class, 1, c1);
    // tester.proxy(Concept.class, 2, c2);
    // tester.proxy(Project.class, 1, p1);
    // tester.proxy(Project.class, 2, p2);
    // tester.proxy(List.class, 1, l1);
    // tester.proxy(List.class, 2, l2);
    // assertTrue(tester.testIdentitiyFieldEquals());
    // assertTrue(tester.testNonIdentitiyFieldEquals());
    // assertTrue(tester.testIdentityFieldNotEquals());
    // assertTrue(tester.testIdentitiyFieldHashcode());
    // assertTrue(tester.testNonIdentitiyFieldHashcode());
    //
    // assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Translation.class, 1, t1);
    tester.proxy(Translation.class, 2, t2);
    tester.proxy(Refset.class, 1, r1);
    tester.proxy(Refset.class, 2, r2);
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);
    tester.proxy(Project.class, 1, p1);
    tester.proxy(Project.class, 2, p2);

    assertTrue(tester.testCopyConstructor(TrackingRecord.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);

    Concept concept = new ConceptJpa();
    concept.setId(1L);
    Refset refset = new RefsetJpa();
    refset.setId(1L);
    refset.setRefsetDescriptorUuid("UUID");
    Translation translation = new TranslationJpa();
    translation.setId(1L);
    Project project = new ProjectJpa();
    project.setId(1L);
    UserPreferences prefs = new UserPreferencesJpa();
    prefs.setId(1L);
    tester.proxy(Translation.class, 1, translation);
    tester.proxy(Refset.class, 1, refset);
    tester.proxy(Concept.class, 1, concept);
    tester.proxy(Project.class, 1, project);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("forAuthoring");
    tester.include("forReview");
    tester.include("revision");
    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test field indexing.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelIndexedFields041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test analyzed fields
    IndexedFieldTester tester = new IndexedFieldTester(object);
    tester.include("authors");
    tester.include("reviewers");
    tester.include("conceptName");
    tester.include("refsetName");
    tester.include("reviewersorder");
    tester.include("authorsorder");
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    tester = new IndexedFieldTester(object);
    tester.include("localSet");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("translationid");
    tester.include("refsetId");
    tester.include("refsetTerminologyId");
    tester.include("forReview");
    tester.include("revision");
    tester.include("forAuthoring");
    tester.include("projectId");
    tester.include("conceptId");
    tester.include("conceptTerminologyId");
    tester.include("conceptNameSort");
    tester.include("refsetNameSort");
    tester.include("refsetModuleId");
    tester.include("refsetType");
    tester.include("workflowStatus");
    assertTrue(tester.testNotAnalyzedIndexedFields());

  }

  /**
   * Teardown.
   */
  @After
  public void teardown() {
    // do nothing
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
