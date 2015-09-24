/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.test.jpa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.services.handlers.ExportRefsetRf2Handler;
import org.ihtsdo.otf.refset.jpa.services.handlers.ExportTranslationRf2Handler;
import org.ihtsdo.otf.refset.jpa.services.handlers.ImportRefsetRf2Handler;
import org.ihtsdo.otf.refset.jpa.services.handlers.ImportTranslationRf2Handler;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.SimpleRefsetMember;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Implementation of the "Helper Jpa Normal Use" Test Cases.
 */
public class ImportExportRf2HandlerTest {

  /** The members input stream. */
  private InputStream membersInputStream = null;

  /** The definition input stream. */
  private InputStream definitionInputStream = null;

  /** The content input stream. */
  private InputStream translationInputStream = null;

  /** The output file. */
  File outputFile = null;

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

    translationInputStream =
        new FileInputStream(new File(
            "../config/src/main/resources/data/test-data/test-data.zip"));
  }

  /**
   * Test normal use of the {@link ImportTranslationRf2Handler} &&
   * {@link ExportTranslationRf2Handler}.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  @Test
  public void testTranslationHandlerJpa001() throws Exception {

    // Import sample translation file
    ImportTranslationRf2Handler importHandler =
        new ImportTranslationRf2Handler();
    List<Concept> concepts =
        importHandler.importTranslation(translationInputStream);

    // Verify concept, description and language refset count
    int conceptCt = concepts.size();
    int descCt = 0;
    int langCt = 0;
    for (Concept concept : concepts) {
      descCt += concept.getDescriptions().size();
      langCt += concept.getDescriptions().size();
    }
    Assert.assertEquals(104, conceptCt);
    Assert.assertEquals(352, descCt);
    Assert.assertEquals(352, langCt);

    // Export
    Translation translation = new TranslationJpa();
    translation.setLanguage("en");
    translation.setVersion("20140731");
    ExportTranslationRf2Handler exportHandler =
        new ExportTranslationRf2Handler();
    InputStream is = exportHandler.exportConcepts(translation, concepts);
    ZipInputStream zin = new ZipInputStream(is);

    descCt = 0;
    langCt = 0;
    Set<String> cid = new HashSet<>();
    for (ZipEntry zipEntry; (zipEntry = zin.getNextEntry()) != null;) {
      if (zipEntry.getName().contains("Description")) {

        Scanner sc = new Scanner(zin);
        while (sc.hasNextLine()) {
          String line = sc.nextLine();
          String[] fields = FieldedStringTokenizer.split(line, "\t");
          descCt++;
          cid.add(fields[4]);
        }
      }
      if (zipEntry.getName().contains("Language")) {

        Scanner sc = new Scanner(zin);
        while (sc.hasNextLine()) {
          sc.nextLine();
          langCt++;
        }
      }
    }

    // Verify counts plus 1 for headers
    Assert.assertEquals(105, cid.size());
    Assert.assertEquals(353, descCt);
    Assert.assertEquals(353, langCt);

  }

  /**
   * Test normal use of the {@link ImportRefsetRf2Handler} &&
   * {@link ExportRefsetRf2Handler}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRefsetHandlerJpa002() throws Exception {

    ImportRefsetRf2Handler importHandler = new ImportRefsetRf2Handler();
    List<SimpleRefsetMember> members =
        importHandler.importMembers(membersInputStream);

    // Verify the member count
    Assert.assertEquals(members.size(), 35);
    String definition = importHandler.importDefinition(definitionInputStream);
    Assert.assertEquals("testDefinition", definition);

    Refset exportRefset = new RefsetJpa();
    ExportRefsetRf2Handler exportHandler = new ExportRefsetRf2Handler();
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(exportHandler.exportMembers(
            exportRefset, members)));
    int ct = 0;
    while (reader.readLine() != null) {
      ct++;
    }
    // Verify the member count plus 1 for the header
    Assert.assertEquals(36, ct);
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
    translationInputStream.close();
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
