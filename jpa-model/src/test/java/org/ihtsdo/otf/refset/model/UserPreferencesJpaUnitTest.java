/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.model;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserPreferences;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.EqualsHashcodeTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.ProxyTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.UserPreferencesJpa;
import org.ihtsdo.otf.refset.jpa.helpers.NullableFieldTester;
import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageDescriptionTypeJpa;
import org.ihtsdo.otf.refset.rf2.jpa.RelationshipJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for {@link UserPreferencesJpa}.
 */
public class UserPreferencesJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private UserPreferencesJpa object;

  /** The u1. */
  private User u1;

  /** The u2. */
  private User u2;

  /** The l1. */
  @SuppressWarnings("rawtypes")
  private List l1;

  /** The l2. */
  @SuppressWarnings("rawtypes")
  private List l2;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Before
  public void setup() throws Exception {
    object = new UserPreferencesJpa();
    ProxyTester tester = new ProxyTester(new UserJpa());
    u1 = (UserJpa) tester.createObject(1);
    u2 = (UserJpa) tester.createObject(2);

    ProxyTester tester2 = new ProxyTester(new LanguageDescriptionTypeJpa());
    ProxyTester tester3 = new ProxyTester(new DescriptionTypeJpa());
    l1 = new ArrayList<>();
    LanguageDescriptionType ldt1 =
        (LanguageDescriptionType) tester2.createObject(1);
    ldt1.setDescriptionType((DescriptionTypeJpa) tester3.createObject(1));
    l1.add(ldt1);

    l2 = new ArrayList<>();
    LanguageDescriptionType ldt2 =
        (LanguageDescriptionType) tester2.createObject(2);
    ldt2.setDescriptionType((DescriptionTypeJpa) tester3.createObject(2));
    l2.add(ldt2);

  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet030() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("userName");
    tester.exclude("userId");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode030() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("languageDescriptionTypes");
    tester.include("lastAccordion");
    tester.include("lastTab");
    tester.include("memoryEnabled");
    tester.include("spellingEnabled");
    tester.include("user");

    // Set up objects
    tester.proxy(User.class, 1, u1);
    tester.proxy(User.class, 2, u2);
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);

    assertTrue(tester.testIdentitiyFieldEquals());
    assertTrue(tester.testNonIdentitiyFieldEquals());
    // the "setUserName" is actually changing the u1 object to have a username
    // of u2 so we need to recreate
    ProxyTester tester2 = new ProxyTester(new UserJpa());
    u1 = (UserJpa) tester2.createObject(1);
    u2 = (UserJpa) tester2.createObject(2);
    tester.proxy(User.class, 1, u1);
    tester.proxy(User.class, 2, u2);
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentitiyFieldHashcode());
    assertTrue(tester.testNonIdentitiyFieldHashcode());

    // the "setUserName" is actually changing the u1 object to have a username
    // of u2 so we need to recreate
    u1 = (UserJpa) tester2.createObject(1);
    u2 = (UserJpa) tester2.createObject(2);
    tester.proxy(User.class, 1, u1);
    tester.proxy(User.class, 2, u2);
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy030() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);

    // Set up objects
    tester.proxy(User.class, 1, u1);
    tester.proxy(User.class, 2, u2);

    assertTrue(tester.testCopyConstructor(UserPreferences.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization030() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);

    // Set up objects
    User u = new UserJpa();
    u.setId(1L);
    u.setUserName("1");
    tester.proxy(User.class, 1, u);

    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField030() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("memoryEnabled");
    tester.include("spellingEnabled");
    assertTrue(tester.testNotNullFields());
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
