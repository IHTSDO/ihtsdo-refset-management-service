/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.model;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserPreferences;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.EqualsHashcodeTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.UserPreferencesJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IndexedFieldTester;
import org.ihtsdo.otf.refset.jpa.helpers.NullableFieldTester;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.worfklow.TrackingRecordJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link TrackingRecordJpa}.
 */
public class ModelUnit041Test extends ModelUnitSupport {

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

  /** the test fixture l1 */
  private List<User> l1;

  /** the test fixture l2 */
  private List<User> l2;

  /** the test fixture u1 */
  private User u1;

  /** the test fixture u2 */
  private User u2;

  /** the test fixture up1 */
  private UserPreferences up1;

  /** the test fixture up2 */
  private UserPreferences up2;

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

    tester = new ProxyTester(new ConceptJpa());
    c1 = (ConceptJpa) tester.createObject(1);
    c2 = (ConceptJpa) tester.createObject(2);

    tester = new ProxyTester(new ProjectJpa());
    p1 = (ProjectJpa) tester.createObject(1);
    p2 = (ProjectJpa) tester.createObject(2);

    tester = new ProxyTester(new UserJpa());
    u1 = (UserJpa) tester.createObject(1);
    u2 = (UserJpa) tester.createObject(2);

    tester = new ProxyTester(new UserPreferencesJpa());
    up1 = (UserPreferencesJpa) tester.createObject(1);
    up2 = (UserPreferencesJpa) tester.createObject(2);

    u1.setUserPreferences(up1);
    u2.setUserPreferences(up2);

    l1 = new ArrayList<>();
    l1.add(u1);
    l2 = new ArrayList<>();
    l2.add(u2);
    r1.setProject(p1);
    r2.setProject(p2);
    t1.setProject(p1);
    t2.setProject(p2);
    t1.setRefset(r1);
    t2.setRefset(r2);
    r1.addTranslation(t1);
    r2.addTranslation(t2);
    t1.addConcept(c1);
    t2.addConcept(c2);
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
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("concept");
    tester.include("forAuthoring");
    tester.include("forReview");
    tester.include("translation");
    tester.include("authors");
    tester.include("reviewers");

    tester.proxy(Translation.class, 1, t1);
    tester.proxy(Translation.class, 2, t2);
    tester.proxy(Refset.class, 1, r1);
    tester.proxy(Refset.class, 2, r2);
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);
    tester.proxy(Project.class, 1, p1);
    tester.proxy(Project.class, 2, p2);
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
    assertTrue(tester.testIdentitiyFieldEquals());
    assertTrue(tester.testNonIdentitiyFieldEquals());
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentitiyFieldHashcode());
    assertTrue(tester.testNonIdentitiyFieldHashcode());
    assertTrue(tester.testIdentityFieldDifferentHashcode());
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
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
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

    User user = new UserJpa();
    user.setUserName("abc");
    Concept concept = new ConceptJpa();
    concept.setId(1L);
    Refset refset = new RefsetJpa();
    refset.setId(1L);
    Translation translation = new TranslationJpa();
    translation.setId(1L);
    Project project = new ProjectJpa();
    project.setId(1L);
    UserPreferences prefs = new UserPreferencesJpa();
    prefs.setId(1L);
    List<User> list = new ArrayList<>();
    list.add(user);
    tester.proxy(Translation.class, 1, translation);
    tester.proxy(Refset.class, 1, refset);
    tester.proxy(Concept.class, 1, concept);
    tester.proxy(Project.class, 1, project);
    tester.proxy(List.class, 1, list);
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
    tester.include("authorUserNames");
    tester.include("reviewerUserNames");
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    tester = new IndexedFieldTester(object);
    tester.include("conceptId");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("translationid");
    tester.include("refsetId");

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
