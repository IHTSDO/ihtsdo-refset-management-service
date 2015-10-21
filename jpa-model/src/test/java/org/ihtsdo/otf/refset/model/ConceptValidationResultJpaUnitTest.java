/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.model;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.ConceptValidationResult;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.EqualsHashcodeTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.ConceptValidationResultJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link ConceptValidationResultJpa}.
 */
public class ConceptValidationResultJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private ConceptValidationResultJpa object;

  /** the test fixture c1 */
  private Concept c1;

  /** the test fixture c2 */
  private Concept c2;

  /** the test fixture l1 */
  private Set<String> s1;

  /** the test fixture l2 */
  private Set<String> s2;

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
    object = new ConceptValidationResultJpa();

    ProxyTester tester = new ProxyTester(new ConceptJpa());
    c1 = (ConceptJpa) tester.createObject(1);
    c2 = (ConceptJpa) tester.createObject(2);
    s1 = new HashSet<>();
    s1.add("1");
    s2 = new HashSet<>();
    s2.add("2");
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet042() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("valid");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode042() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("concept");
    tester.include("errors");
    tester.include("warnings");
    tester.include("comments");

    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);
    tester.proxy(Set.class, 1, s1);
    tester.proxy(Set.class, 2, s2);

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
  public void testModelCopy042() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);
    tester.proxy(Set.class, 1, s1);
    tester.proxy(Set.class, 2, s2);

    assertTrue(tester.testCopyConstructor(ConceptValidationResult.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization042() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);

    Concept concept = new ConceptJpa();
    concept.setId(1L);

    tester.proxy(Concept.class, 1, concept);

    assertTrue(tester.testXmlSerialization());
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
