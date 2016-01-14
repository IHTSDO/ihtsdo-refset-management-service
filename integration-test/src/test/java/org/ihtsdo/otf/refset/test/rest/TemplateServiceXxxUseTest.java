/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.test.rest;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.services.rest.SecurityServiceRest;
import org.ihtsdo.otf.refset.rest.client.SecurityClientRest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TODO: edit the description Implementation of the
 * "Template Service REST XXX Use" Test Cases.
 */
public class TemplateServiceXxxUseTest extends RestSupport {

  // TODO: edit the service to use
  /** The service. */
  @SuppressWarnings("unused")
  private SecurityClientRest service;

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {
    // do nothing
  }

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    // TODO: Open the service (or services) you want to use
    service = new SecurityClientRest(ConfigUtility.getConfigProperties());
  }

  /**
   * TODO: edit this comment to match the test case Test Xxx use of the yyy of
   * {@link SecurityServiceRest}.
   * 
   * @throws Exception the exception
   */
  @Test
  // TODO: Edit this method name to match the test case
  public void testXxxUseRestTemplate001() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // TODO: implement the test case

  }

  // TODO: have additional methods like the one above as needed

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // do nothing (service does not need to be closed)
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
