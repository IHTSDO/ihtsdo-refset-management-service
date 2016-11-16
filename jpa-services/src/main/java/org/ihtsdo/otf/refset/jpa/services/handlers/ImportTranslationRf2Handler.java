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
import java.util.UUID;
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
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;

/**
 * Implementation of an algorithm to import a refset definition.
 */
public class ImportTranslationRf2Handler implements ImportTranslationHandler {

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The id. */
  final String id = "id";

  /** The validation result. */
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
  public boolean isDeltaHandler() {
    return false;
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

  /**
   * Import concepts.
   *
   * @param translation the translation
   * @param content the content
   * @return the list
   * @throws Exception the exception
   */
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
    final ZipInputStream zin = new ZipInputStream(content);
    final Map<String, Concept> conceptCache = new HashMap<>();

    int inactiveDescriptionCt = 0;
    int inactiveMemberCt = 0;

    final RefsetService service = new RefsetServiceJpa();
    try {
      final IdentifierAssignmentHandler handler =
          service.getIdentifierAssignmentHandler(ConfigUtility.DEFAULT);

      // Iterate through the zip entries
      for (ZipEntry zipEntry; (zipEntry = zin.getNextEntry()) != null;) {

        Logger.getLogger(getClass())
            .debug("Import translation Rf2 handler - reading zipEntry "
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

              // Skip inactive descriptions
              if (fields[2].equals("0")) {
                inactiveDescriptionCt++;
                continue;
              }

              // Create description and populate from RF2
              final Description description = new DescriptionJpa();
              setCommonFields(description, translation.getRefset());
              if (!fields[1].equals("")) {
                description.setEffectiveTime(
                    ConfigUtility.DATE_FORMAT.parse(fields[1]));
              }

              // Either use description id or fake it and assign later
              if (fields[0].equals("") || fields[0].startsWith("TMP-")) {
                description
                    .setTerminologyId(handler.getTerminologyId(description));
              } else {
                description.setTerminologyId(fields[0]);
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
                concept.setEffectiveTime(
                    ConfigUtility.DATE_FORMAT.parse(fields[1]));
              }
              concept.setTerminologyId(fields[4]);
              concept.setDefinitionStatusId("unknown");
              concept.setTranslation(translation);
              concept.getDescriptions().add(description);
              description.setConcept(concept);

              // Cache the description for lookup by the language reset member
              descriptions.put(fields[0], description);
              Logger.getLogger(getClass())
                  .info("  description = " + fields[0] + ", "
                      + description.getTerminologyId() + ", "
                      + description.getTerm());
            }
          }

        }

        // Find the languages file
        else if (zipEntry.getName().contains("Refset_Language")) {

          Logger.getLogger(getClass())
              .debug("Import translation Rf2 handler - reading zipEntry "
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
              throw new LocalException(
                  "Unexpected field count in language file.");
            }

            if (!fields[0].equals(id)) { // header

              // Skip inactive language entries
              if (fields[2].equals("0")) {
                inactiveMemberCt++;
                continue;
              }

              // Create and configure the member
              final LanguageRefsetMember member = new LanguageRefsetMemberJpa();
              setCommonFields(member, translation.getRefset());
              if (fields[0].equals("") || fields[0].startsWith("TMP-")) {
                member.setTerminologyId(UUID.randomUUID().toString());
              } else {
                member.setTerminologyId(fields[0]);
              }
              if (!fields[1].equals("")) {
                member.setEffectiveTime(
                    ConfigUtility.DATE_FORMAT.parse(fields[1]));
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
    } catch (Exception e) {
      throw e;
    } finally {
      service.close();
      zin.close();
    }
    // Verify that data was found
    if (!descSeen || descriptions.isEmpty()) {
      throw new LocalException("Missing or empty description file.");
    }
    if (!langSeen) {
      throw new LocalException("Missing or empty language file.");
    }

    final int langCt = descLangMap.size();

    // Connect descriptions and language refset member objects
    for (final String key : new HashSet<>(descriptions.keySet())) {
      final Description description = descriptions.get(key);
      // Connect language and description
      final LanguageRefsetMember member = descLangMap.get(key);
      if (member != null) {
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
      validationResult.addError(descLangMap.size()
          + " language refset members without matching descriptions.");
    }
    validationResult
        .addComment(descriptions.size() + " descriptions successfully loaded.");
    validationResult
        .addComment(langCt + " language refset members successfully loaded.");

    if (inactiveDescriptionCt == 1) {
      validationResult.addWarning("1 inactive description not loaded.");
    } else if (inactiveDescriptionCt != 0) {
      validationResult
          .addWarning(inactiveMemberCt + " inactive descriptions not loaded.");
    }
    if (inactiveMemberCt == 1) {
      validationResult.addWarning("1 inactive member not loaded.");
    } else if (inactiveMemberCt != 0) {
      validationResult
          .addWarning(inactiveMemberCt + " inactive members not loaded.");
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
