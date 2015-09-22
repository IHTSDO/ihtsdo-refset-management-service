/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefSetMemberJpa;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;

/**
 * Implementation of an algorithm to import a refset definition.
 */
public class ImportTranslationRf2Handler extends RootServiceJpa implements
    ImportTranslationHandler {

  /** The descriptions. */
  Map<String, Description> descriptions = new HashMap<>();

  /** The language entries. */
  Map<String, LanguageRefSetMember> descLangMap = new HashMap<>();

  /** The desc seen. */
  boolean descSeen = false;

  /** The lang seen. */
  boolean langSeen = false;

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
  public String getName() {
    return "Import RF2";
  }

  /* see superclass */
  @SuppressWarnings("resource")
  @Override
  public List<Concept> importTranslation(InputStream content) throws Exception {
    Logger.getLogger(getClass()).info("Import translation concepts");
    // Handle the input stream as a zip input stream
    ZipInputStream zin = new ZipInputStream(content);

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
        Map<String, Concept> conceptCache = new HashMap<>();
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
            description.setTerminologyId(fields[0]);
            description.setEffectiveTime(ConfigUtility.DATE_FORMAT
                .parse(fields[1]));
            description.setLastModified(description.getEffectiveTime());
            description.setActive(true);
            description.setLanguageCode(fields[5].intern());
            description.setTypeId(fields[6].intern());
            description.setTerm(fields[7]);
            description.setCaseSignificanceId(fields[8].intern());

            // Leave the moduleId intentionally null, this should be set by
            // the translation project
            description.setModuleId(null);

            // Handle the concept the description is connected to
            Concept concept = null;
            if (!conceptCache.containsKey(fields[4])) {
              conceptCache.put(fields[4], new ConceptJpa());
            }
            concept = conceptCache.get(fields[4]);
            concept.setTerminologyId(fields[4]);
            concept.addDescription(description);
            description.setConcept(concept);

            // Cache the description for lookup by the language reset member
            descriptions.put(fields[0], description);
            Logger.getLogger(getClass()).info(
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
            final LanguageRefSetMember member = new LanguageRefSetMemberJpa();
            member.setTerminologyId(fields[0]);
            member.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(fields[1]));
            member.setLastModified(member.getEffectiveTime());
            member.setActive(true);

            // Leave module/refset id intentionally blank to be set by project
            member.setRefSetId(null);
            member.setModuleId(null);

            // Language unique attributes
            member.setAcceptabilityId(fields[6].intern());

            // Cache language refset members
            if (!descLangMap.containsKey(fields[5])) {
              descLangMap.put(fields[5], member);
            } else {
              throw new Exception(
                  "Unexpected description with multiple langauges - " + line);
            }
            Logger.getLogger(getClass()).info("    member = " + member);

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
    Set<Concept> concepts = new HashSet<>();
    for (Description description : descriptions.values()) {

      concepts.add(description.getConcept());

      // Connect language and description
      if (descLangMap.containsKey(description.getTerminologyId())) {
        LanguageRefSetMember member =
            descLangMap.get(description.getTerminologyId());
        member.setDescription(description);
        description.addLanguageRefSetMember(member);
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
    return new ArrayList<>(concepts);
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

}
