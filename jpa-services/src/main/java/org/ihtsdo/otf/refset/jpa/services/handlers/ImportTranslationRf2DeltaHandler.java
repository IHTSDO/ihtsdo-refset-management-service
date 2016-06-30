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
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;

/**
 * Implementation of an algorithm to import a refset definition.
 */
public class ImportTranslationRf2DeltaHandler implements
    ImportTranslationHandler {

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The id. */
  final String id = "id";

  /** The validation result. */
  ValidationResult validationResult = new ValidationResultJpa();

  /**
   * Instantiates an empty {@link ImportTranslationRf2DeltaHandler}.
   * @throws Exception if anything goes wrong
   */
  public ImportTranslationRf2DeltaHandler() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public boolean isDeltaHandler() {
    return true;
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
    return "Import RF2 Delta";
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

    int inactiveDescriptionCt = 0;
    int inactiveMemberCt = 0;
    int addedDescriptionCt = 0;
    int addedMemberCt = 0;

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
          final String fields[] = FieldedStringTokenizer.split(line, "\t");
          if (fields.length != 9) {
            sc.close();
            throw new LocalException(
                "Unexpected field count in descriptions file.");
          }
          if (!fields[0].equals(id)) {

            // Mark inactive
            if (fields[2].equals("0")) {
              inactiveDescriptionCt++;
            }

            // Look for a module id change
            if (!translation.getModuleId().equals(fields[3])) {
              zin.close();
              throw new LocalException(
                  "Module id has changed, make sure to update the refset/translation module id first - "
                      + fields[3]);
            }

            // Create description and populate from RF2
            final Description description = new DescriptionJpa();
            setCommonFields(description, translation.getRefset());
            description.setActive(fields[2].equals("1"));
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
              concept = conceptCache.get(fields[4]);
              setCommonFields(concept, translation.getRefset());
              if (!fields[1].equals("")) {
                concept.setEffectiveTime(ConfigUtility.DATE_FORMAT
                    .parse(fields[1]));
              }
              concept.setTerminologyId(fields[4]);
              concept.setDefinitionStatusId("unknown");
              concept.setTranslation(translation);
            }
            concept = conceptCache.get(fields[4]);
            concept.getDescriptions().add(description);
            description.setConcept(concept);
            // Cache the description for lookup by the language reset member
            descriptions.put(fields[0], description);
            addedDescriptionCt++;
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
          final String fields[] = FieldedStringTokenizer.split(line, "\t");

          if (fields.length != 7) {
            sc.close();
            throw new LocalException("Unexpected field count in language file.");
          }

          if (!fields[0].equals(id)) { // header

            // Mark inactive language entries
            if (fields[2].equals("0")) {
              inactiveMemberCt++;
            }

            // Look for a module id change
            if (!translation.getModuleId().equals(fields[3])) {
              zin.close();
              throw new LocalException(
                  "Module id has changed, make sure to update the refset/translation module id first - "
                      + fields[3]);
            }

            // Look for a refset id change
            if (!translation.getTerminologyId().equals(fields[4])) {
              zin.close();
              throw new LocalException(
                  "Translation refset id has changed, must create a new refset/translation for this delta - "
                      + fields[4]);
            }

            // Create and configure the member
            final LanguageRefsetMember member = new LanguageRefsetMemberJpa();
            setCommonFields(member, translation.getRefset());
            member.setActive(fields[2].equals("1"));
            member.setTerminologyId(fields[0]);
            if (!fields[1].equals("")) {
              member.setEffectiveTime(ConfigUtility.DATE_FORMAT
                  .parse(fields[1]));
            }

            // Other fields
            member.setRefsetId(translation.getRefset().getTerminologyId());
            member.setDescriptionId(fields[5].intern());
            member.setAcceptabilityId(fields[6].intern());

            // Cache language refset members
            if (!descLangMap.containsKey(fields[5])) {
              descLangMap.put(fields[5], member);
            } else {
              throw new LocalException(
                  "Unexpected description with multiple langauges - " + line);
            }
            addedMemberCt++;
            Logger.getLogger(getClass()).debug("    member = " + member);

          }
        } // end while

      }
    } // zin close
    zin.close();

    // Verify that data was found
    if (!descSeen) {
      throw new LocalException("Missing description file.");
    }
    if (!langSeen) {
      throw new LocalException("Missing language file.");
    }

    // NOTE: this all assumes that descriptions have at most ONE langauge refset
    // entry

    // Get a service to look stuff up
    final TranslationService service = new TranslationServiceJpa();
    try {

      // Connect descriptions and language refset member objects
      for (final Description description : descriptions.values()) {

        // Connect language and description
        if (descLangMap.containsKey(description.getTerminologyId())) {
          final LanguageRefsetMember member =
              descLangMap.get(description.getTerminologyId());
          description.getLanguageRefsetMembers().add(member);
        }

        // Otherwise, need to look up existing language members for this
        // description and attach to the new one.
        // If the description is null, this is an error, a description should
        // only be here without languages if it already exists and is being
        // updated
        else {
          for (final LanguageRefsetMember member : service
              .getTranslationDescription(description.getTerminologyId(),
                  translation.getId()).getLanguageRefsetMembers()) {
            description.getLanguageRefsetMembers().add(member);
          }
        }
        // Remove the entry from the map
        descLangMap.remove(description.getTerminologyId());

      }

      // If any references are left in descLangMap, we've got a language without
      // a desc -> need to look up description
      if (descLangMap.size() > 0) {
        for (final String descriptionId : descLangMap.keySet()) {

          // Read description from DB and do a shallow copy (this should exist)
          final Description orig =
              service.getTranslationDescription(descriptionId,
                  translation.getId());
          if (orig == null) {
            // This in SPANISH SNOMED is a description that is from English
            // SNOMED.
            Logger.getLogger(getClass()).warn(
                "  language references description not in descriptions file.");
            continue;
          }
          final Description description = new DescriptionJpa(orig, false);

          Concept concept = null;
          final String conceptId = description.getConcept().getTerminologyId();
          // if not in cache, create concept and add to cache
          if (!conceptCache.containsKey(conceptId)) {
            concept = new ConceptJpa();
            conceptCache.put(conceptId, concept);
            concept = conceptCache.get(conceptId);
            setCommonFields(concept, translation.getRefset());
            concept.setEffectiveTime(description.getEffectiveTime());
            concept.setTerminologyId(conceptId);
            concept.setDefinitionStatusId("unknown");
            concept.setTranslation(translation);
            concept.getDescriptions().add(description);
          }
          // otherwise, just add the description to the cached concept
          else {
            concept = conceptCache.get(conceptId);
            concept.getDescriptions().add(description);
          }

          // Attach local refset member
          final LanguageRefsetMember member =
              descLangMap.get(description.getTerminologyId());
          description.getLanguageRefsetMembers().add(member);

          descLangMap.remove(description.getTerminologyId());
        }
      }
    } catch (Exception e) {
      throw e;
    } finally {
      service.close();
    }

    if (inactiveDescriptionCt == 1) {
      validationResult.addComment("1 description was retired.");
    } else if (inactiveDescriptionCt != 0) {
      validationResult.addComment(inactiveDescriptionCt
          + " inactive descriptions were retured.");
    }
    if (inactiveMemberCt == 1) {
      validationResult.addComment("1 language refset member was retired.");
    } else if (inactiveMemberCt != 0) {
      validationResult.addComment(inactiveMemberCt
          + " language refset members retired.");
    }

    if (addedDescriptionCt == 1) {
      validationResult
          .addComment("1 description successfully loaded or updated.");
    } else if (addedDescriptionCt != 0) {
      validationResult.addComment(addedDescriptionCt
          + " inactive descriptions successfully loaded or updated.");
    }
    if (addedMemberCt == 1) {
      validationResult
          .addComment("1 language refset member successfully loaded or updated.");
    } else if (addedMemberCt != 0) {
      validationResult.addComment(addedMemberCt
          + " language refset members successfully loaded or udpated.");
    }
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
