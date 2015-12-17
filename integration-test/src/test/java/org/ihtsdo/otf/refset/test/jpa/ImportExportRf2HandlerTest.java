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

import org.ihtsdo.otf.refset.DefinitionClause;
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
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
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
                "../config/src/main/resources/data/refset/der2_Refset_SimpleSnapshot_INT_20140731.txt"));

    definitionInputStream =
        new FileInputStream(
            new File(
                "../config/src/main/resources/data/refset/der2_Refset_DefinitionSnapshot_INT_20140731.txt"));

    translationInputStream =
        new FileInputStream(new File(
            "../config/src/main/resources/data/translation/translation.zip"));
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
    // Set up superstructure
    Translation translation = new TranslationJpa();
    Refset refset = new RefsetJpa();
    refset.setModuleId("sampleModuleId");
    refset.setTerminologyId("sampleTerminologyId");
    translation.setRefset(refset);
    translation.setLanguage("en");
    translation.setVersion("20140731");

    // Import sample translation file
    ImportTranslationRf2Handler importHandler =
        new ImportTranslationRf2Handler();

    List<Concept> concepts =
        importHandler.importConcepts(translation, translationInputStream);

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
    // Set up refset
    Refset refset = new RefsetJpa();
    refset.setModuleId("sampleModuleId");
    refset.setTerminology("sampleTerminologyId");

    ImportRefsetRf2Handler importHandler = new ImportRefsetRf2Handler();
    List<ConceptRefsetMember> members =
        importHandler.importMembers(refset, membersInputStream);

    // Verify the member count
    Assert.assertEquals(members.size(), 21);
    List<DefinitionClause> definitionClauses = importHandler.importDefinition(definitionInputStream);
    Assert.assertEquals("<<410675002|Route of administration|", definitionClauses.get(0));

    ExportRefsetRf2Handler exportHandler = new ExportRefsetRf2Handler();
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(exportHandler.exportMembers(
            refset, members)));
    int ct = 0;
    while (reader.readLine() != null) {
      ct++;
    }
    // Verify the member count plus 1 for the header
    Assert.assertEquals(22, ct);
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
