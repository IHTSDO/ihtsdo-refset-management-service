/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.lists;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.helpers.CopyConstructorTester;
import org.ihtsdo.otf.refset.helpers.EqualsHashcodeTester;
import org.ihtsdo.otf.refset.helpers.GetterSetterTester;
import org.ihtsdo.otf.refset.helpers.XmlSerializationTester;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IndexedFieldTester;
import org.ihtsdo.otf.refset.jpa.helpers.NullableFieldTester;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * Unit testing for {@link ProjectJpa}.
 */
public class ListUnitSupport {

  /**  The name. */
  @Rule
  public TestName name = new TestName();
  
}