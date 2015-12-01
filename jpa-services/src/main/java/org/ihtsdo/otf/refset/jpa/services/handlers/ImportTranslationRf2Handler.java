/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.MemoryEntryJpa;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;
import org.ihtsdo.otf.refset.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import a refset definition.
 */
public class ImportTranslationRf2Handler extends RootServiceJpa implements
    ImportTranslationHandler {

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The id. */
  final String id = "id";

  /**
   * Instantiates an empty {@link ImportTranslationRf2Handler}.
   * @throws Exception if anything goes wrong
   */
  public ImportTranslationRf2Handler() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public String getFileTypeFilter() {
    return ".zip";
  }

  /* see superclass */
  @Override
  public String getMimeType() {
    return "application/zip";
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Import RF2";
  }

  @Override
  public List<MemoryEntry> importPhraseMemory(Translation translation,
    InputStream content) throws Exception {
    List<MemoryEntry> list = new ArrayList<>();
    String line = "";
    Reader reader = new InputStreamReader(content, "UTF-8");
    PushBackReader pbr = new PushBackReader(reader);
    while ((line = pbr.readLine()) != null) {

      // Strip \r and split lines
      line = line.replace("\r", "");
      final String fields[] = line.split("\t");

      // Check field lengths
      if (fields.length != 2) {
        pbr.close();
        Logger.getLogger(getClass()).error("line = " + line);
        throw new Exception(
            "Unexpected field count in phrase memory file "
                + fields.length);
      }

      // Instantiate and populate members
      final MemoryEntry member = new MemoryEntryJpa();
      member.setName(fields[0]);
      member.setTranslatedName(fields[1]);
      // Add member
      list.add(member);
      Logger.getLogger(getClass()).debug("  phrasememory = " + member);
    }
    pbr.close();
    return list;
  }
  
  /* see superclass */
  @SuppressWarnings("resource")
  @Override
  public List<Concept> importConcepts(Translation translation,
    InputStream content) throws Exception {
    Logger.getLogger(getClass()).info("Import translation concepts");

    /** The descriptions. */
    Map<String, Description> descriptions = new HashMap<>();

    /** The language entries. */
    Map<String, LanguageRefsetMember> descLangMap = new HashMap<>();

    /** The desc seen. */
    boolean descSeen = false;

    /** The lang seen. */
    boolean langSeen = false;
    // Handle the input stream as a zip input stream
    ZipInputStream zin = new ZipInputStream(content);
    Map<String, Concept> conceptCache = new HashMap<>();

    // Iterate through the zip entries
    for (ZipEntry zipEntry; (zipEntry = zin.getNextEntry()) != null;) {

      Logger.getLogger(getClass()).debug(
          "Import translation Rf2 handler - reading zipEntry "
              + zipEntry.getName());

      // Find the descriptions file
      if (zipEntry.getName().contains("Description")) {

        // Verify we haven't seen it already
        if (descSeen) {
          throw new Exception(
              "More than one description file in translation import zip file.");
        }
        descSeen = true;

        // Scan through the file and create descriptions and cache concepts
        String line = null;
        Scanner sc = new Scanner(zin);
        while (sc.hasNextLine()) {
          line = sc.nextLine();
          final String fields[] = line.split("\t");
          if (fields.length != 9) {
            sc.close();
            throw new Exception("Unexpected field count in descriptions file.");
          }
          if (!fields[0].equals(id)) {

            // Skip inactive descriptions
            if (fields[2].equals("0")) {
              continue;
            }

            // Create description and populate from RF2
            final Description description = new DescriptionJpa();
            setCommonFields(description, translation.getRefset());
            description.setTerminologyId(fields[0]);
            description.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(fields[1]));
            description.setLanguageCode(fields[5].intern());
            description.setTypeId(fields[6].intern());
            description.setTerm(fields[7]);
            description.setCaseSignificanceId(fields[8].intern());

            // Handle the concept the description is connected to
            Concept concept = null;
            if (!conceptCache.containsKey(fields[4])) {
              conceptCache.put(fields[4], new ConceptJpa());
            }
            concept = conceptCache.get(fields[4]);
            setCommonFields(concept, translation.getRefset());
            concept.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(fields[1]));
            concept.setTerminologyId(fields[4]);
            concept.setDefinitionStatusId("unknown");
            concept.setTranslation(translation);
            concept.addDescription(description);
            description.setConcept(concept);

            // Cache the description for lookup by the language reset member
            descriptions.put(fields[0], description);
            Logger.getLogger(getClass()).debug(
                "  description = " + description.getTerminologyId() + ", "
                    + description.getTerm());
          }
        }

      }

      // Find the languages file
      else if (zipEntry.getName().contains("Refset_Language")) {

        Logger.getLogger(getClass()).debug(
            "Import translation Rf2 handler - reading zipEntry "
                + zipEntry.getName());

        // Verify we have not encountered this already
        if (langSeen) {
          throw new Exception(
              "More than one language file in translation import zip file.");
        }
        langSeen = true;

        String line = null;
        Scanner sc = new Scanner(zin);
        while (sc.hasNextLine()) {
          // System.out.println(sc.nextLine());
          line = sc.nextLine();
          line = line.replace("\r", "");
          final String fields[] = line.split("\t");

          if (fields.length != 7) {
            sc.close();
            throw new Exception("Unexpected field count in language file.");
          }

          if (!fields[0].equals(id)) { // header

            // Skip inactive language entries
            if (fields[2].equals("0")) {
              continue;
            }

            // Create and configure the member
            final LanguageRefsetMember member = new LanguageRefsetMemberJpa();
            setCommonFields(member, translation.getRefset());
            member.setTerminologyId(fields[0]);
            member.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(fields[1]));

            // Set from the translation refset
            member.setRefsetId(translation.getRefset().getTerminologyId());

            // Language unique attributes
            member.setAcceptabilityId(fields[6].intern());

            // Cache language refset members
            if (!descLangMap.containsKey(fields[5])) {
              descLangMap.put(fields[5], member);
            } else {
              throw new Exception(
                  "Unexpected description with multiple langauges - " + line);
            }
            Logger.getLogger(getClass()).debug("    member = " + member);

          }
        } // end while

      }
    } // zin close
    zin.close();

    // Verify that data was found
    if (!descSeen || descriptions.isEmpty()) {
      throw new Exception("Missing or empty description file.");
    }
    if (!langSeen) {
      throw new Exception("Missing or empty language file.");
    }

    // Connect descriptions and language refset member objects
    for (Description description : descriptions.values()) {

      // assign an initial preferred name
      // if (description.getConcept().getName() == null) {
      // description.getConcept().setName(description.getTerm());
      // }

      // Connect language and description
      if (descLangMap.containsKey(description.getTerminologyId())) {
        LanguageRefsetMember member =
            descLangMap.get(description.getTerminologyId());
        member.setDescriptionId(description.getTerminologyId());
        description.addLanguageRefetMember(member);

        // If a description is a synonym (e.g. typeId=)
        // and language is prefered (e.g. acceptabilityId=)
        // use as concept preferred name
        // if (description.getTypeId().equals("900000000000013009")
        // && descLangMap.get(description.getTerminologyId())
        // .getAcceptabilityId().equals("900000000000548007")) {
        // description.getConcept().setName(description.getTerm());
        // }

        descLangMap.remove(description.getTerminologyId());
      }

      else {
        throw new Exception("Unexpected description without language - "
            + description.getTerminologyId());
      }

    }

    // If any references are left in descLangMap, we've got a language without a
    // desc
    if (!descLangMap.isEmpty()) {
      throw new Exception("Languages without corresponding descriptions - "
          + descLangMap);
    }

    // Return list of concepts
    return new ArrayList<>(conceptCache.values());
  }

  /* see superclass */
  @Override
  public void refreshCaches() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a

  }

  /**
   * Sets the common fields.
   *
   * @param c the c
   * @param refset the refset
   */
  @SuppressWarnings("static-method")
  private void setCommonFields(Component c, Refset refset) {
    c.setActive(true);
    c.setEffectiveTime(null);
    c.setId(null);
    c.setPublishable(true);
    c.setPublished(false);
    c.setModuleId(refset.getModuleId());
    c.setTerminology(refset.getTerminology());
    c.setVersion(refset.getVersion());
  }
}
