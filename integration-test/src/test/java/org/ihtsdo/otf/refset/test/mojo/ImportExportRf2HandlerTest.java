/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.test.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;












import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.services.handlers.ExportRefsetRf2Handler;
import org.ihtsdo.otf.refset.jpa.services.handlers.ExportTranslationRf2Handler;
import org.ihtsdo.otf.refset.jpa.services.handlers.ImportRefsetRf2Handler;
import org.ihtsdo.otf.refset.jpa.services.handlers.ImportTranslationRf2Handler;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.SimpleRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

/**
 * Implementation of the "Helper Jpa Normal Use" Test Cases.
 */
public class ImportExportRf2HandlerTest {

  /**  The members input stream. */
  private InputStream membersInputStream = null;
  
  /**  The definition input stream. */
  private InputStream definitionInputStream = null;
  
  /**  The content input stream. */
  private InputStream contentInputStream = null;
  
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

    membersInputStream =
        new FileInputStream(
            new File(
                "../config/src/main/resources/data/test-data/der2_Refset_SimpleSnapshot_INT_20140731.txt"));
    
    definitionInputStream =
        new FileInputStream(
            new File(
                "../config/src/main/resources/data/test-data/der2_Refset_DefinitionSnapshot_INT_20140731.txt"));
    
    contentInputStream =
        new FileInputStream(
            new File(
                "../config/src/main/resources/data/test-data/test-data.zip"));
  }

  /**
   * Test normal use of the {@link ImportTranslationRf2Handler} &&
   * {@link ExportTranslationRf2Handler}.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @Test
  public void testTranslationHandlerJpa001() throws Exception {

    ImportTranslationRf2Handler importHandler = new ImportTranslationRf2Handler();
    importHandler.setReleaseVersion("20140731");
    importHandler.setTerminology("SNOMEDCT_US");
    importHandler.setTerminologyVersion("20140731");
    List<Concept> concepts = importHandler.importTranslation(contentInputStream);
    Assert.assertEquals(concepts.size(), 34105);
    
    Translation translation = new TranslationJpa();
    translation.setLanguage("en");
    translation.setVersion("20140731");
    ExportTranslationRf2Handler exportHandler = new ExportTranslationRf2Handler();
    InputStream is = exportHandler.exportConcepts(translation, concepts);
    // TODO: open ZipInputStream and find corresponding zip entries
    ZipInputStream zin = new ZipInputStream(is);

    List<String> descriptionRows = new ArrayList<>();
    for (ZipEntry zipEntry; (zipEntry = zin.getNextEntry()) != null;) {
      Logger.getLogger(getClass()).debug("Import translation Rf2 handler - reading zipEntry " + zipEntry.getName());
      if (zipEntry.getName().contains("sct2_Description")) {
         
        Scanner sc = new Scanner(zin);
        while (sc.hasNextLine()) {
          descriptionRows.add(sc.nextLine());
        }
      }
    }
    Assert.assertEquals(descriptionRows.size(), 0);
  }


  /**
   * Test normal use of the {@link ImportRefsetRf2Handler} &&
   * {@link ExportRefsetRf2Handler}.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @Test
  public void testRefsetHandlerJpa002() throws Exception {


    ImportRefsetRf2Handler importHandler = new ImportRefsetRf2Handler();
    importHandler.setReleaseVersion("20140731");
    importHandler.setTerminology("SNOMEDCT_US");
    importHandler.setTerminologyVersion("20140731");
    List<SimpleRefSetMember> members = importHandler.importMembers(membersInputStream);
    Assert.assertEquals(members.size(), 35);
    String definition = importHandler.importDefinition(definitionInputStream);
    Assert.assertEquals("testDefinition", definition);
    
    Refset exportRefset = new RefsetJpa();
    ExportRefsetRf2Handler exportHandler = new ExportRefsetRf2Handler();
    InputStream is = exportHandler.exportMembers(exportRefset, members);

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // close test fixtures per class
    definitionInputStream.close();
    membersInputStream.close();
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
