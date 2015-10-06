/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.test.jpa;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.handlers.DefaultTerminologyHandler;
import org.ihtsdo.otf.refset.rf2.Description;
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
    // n/a
  }

  /**
   * Test getting descriptions from Snow Owl.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDescriptions() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    ProjectService service = new ProjectServiceJpa();
    Description description = service.getTerminologyHandler("DEFAULT").getDescription("1215974010",
        "SNOMEDCT", "latest");
    assertEquals("Tumour of kidney",description.getTerm());
    assertEquals(1, description.getLanguageRefsetMembers().size());
    service.close();
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
