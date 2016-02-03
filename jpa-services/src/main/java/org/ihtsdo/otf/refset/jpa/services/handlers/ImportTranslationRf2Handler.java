/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;

/**
 * Implementation of an algorithm to import a refset definition.
 */
public class ImportTranslationRf2Handler implements ImportTranslationHandler {

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The id. */
  final String id = "id";

  /**  The validation result. */
  ValidationResult validationResult = new ValidationResultJpa();
  

  
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

  /* see superclass */
  @SuppressWarnings("resource")
  @Override
  public List<Concept> importConcepts(Translation translation,
    InputStream content) throws Exception {
    Logger.getLogger(getClass()).info("Import translation concepts");

    // initialize
    validationResult = new ValidationResultJpa();
    
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
          throw new LocalException(
              "More than one description file in translation import zip file.");
        }
        descSeen = true;

        // Scan through the file and create descriptions and cache concepts
        String line = null;
        Scanner sc = new Scanner(zin);
        while (sc.hasNextLine()) {
          line = sc.nextLine();
          final String fields[] = FieldedStringTokenizer.split(line,"\t");
          if (fields.length != 9) {
            sc.close();
            throw new LocalException(
                "Unexpected field count in descriptions file.");
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
            if (!fields[1].equals("")) {
              description.setEffectiveTime(ConfigUtility.DATE_FORMAT
                  .parse(fields[1]));
            }
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
            if (!fields[1].equals("")) {
              concept.setEffectiveTime(ConfigUtility.DATE_FORMAT
                  .parse(fields[1]));
            }
            concept.setTerminologyId(fields[4]);
            concept.setDefinitionStatusId("unknown");
            concept.setTranslation(translation);
            concept.getDescriptions().add(description);
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
          throw new LocalException(
              "More than one language file in translation import zip file.");
        }
        langSeen = true;

        String line = null;
        Scanner sc = new Scanner(zin);
        while (sc.hasNextLine()) {
          line = sc.nextLine();
          line = line.replace("\r", "");
          final String fields[] = FieldedStringTokenizer.split(line,"\t");

          if (fields.length != 7) {
            sc.close();
            throw new LocalException("Unexpected field count in language file.");
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
            if (!fields[1].equals("")) {
              member.setEffectiveTime(ConfigUtility.DATE_FORMAT
                  .parse(fields[1]));
            }

            // Set from the translation refset
            member.setRefsetId(translation.getRefset().getTerminologyId());

            // Language unique attributes
            member.setAcceptabilityId(fields[6].intern());

            // Cache language refset members
            if (!descLangMap.containsKey(fields[5])) {
              descLangMap.put(fields[5], member);
            } else {
              throw new LocalException(
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
      throw new LocalException("Missing or empty description file.");
    }
    if (!langSeen) {
      throw new LocalException("Missing or empty language file.");
    }

    // Connect descriptions and language refset member objects
    for (Description description : descriptions.values()) {

      // Connect language and description
      if (descLangMap.containsKey(description.getTerminologyId())) {
        LanguageRefsetMember member =
            descLangMap.get(description.getTerminologyId());
        member.setDescriptionId(description.getTerminologyId());
        description.getLanguageRefsetMembers().add(member);

        descLangMap.remove(description.getTerminologyId());
      }

      else {
        throw new LocalException("Unexpected description without language - "
            + description.getTerminologyId());
      }

    }

    // If any references are left in descLangMap, we've got a language without a
    // desc
    if (!descLangMap.isEmpty()) {
      throw new LocalException(
          "Languages without corresponding descriptions - " + descLangMap);
    }
    validationResult.addComment(descriptions.size() + " descriptions successfully loaded.");
    validationResult.addComment(descLangMap.size() + " language refset members successfully loaded.");
    // Return list of concepts
    return new ArrayList<>(conceptCache.values());
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
  }

  @Override
  public ValidationResult getValidationResults() throws Exception {
    return validationResult;
  }

}
