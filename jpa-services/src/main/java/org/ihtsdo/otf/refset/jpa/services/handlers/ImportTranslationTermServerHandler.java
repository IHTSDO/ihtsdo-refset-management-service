/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.handlers.ImportExportAbstract;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;

/**
 * Implementation of an algorithm to import a refset definition.
 */
public class ImportTranslationTermServerHandler extends ImportExportAbstract
    implements ImportTranslationHandler {

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The id. */
  final String id = "id";

  /** The validation result. */
  ValidationResult validationResult = new ValidationResultJpa();

  /** The Constant requestSize. */
  private final static int requestSize = 200;

  /**
   * Instantiates an empty {@link ImportTranslationTermServerHandler}.
   * @throws Exception if anything goes wrong
   */
  public ImportTranslationTermServerHandler() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public void setId(String id) {
    // not used
  }

  /* see superclass */
  @Override
  public String getId() {
    return this.id;
  }

  /* see superclass */
  @Override
  public boolean isDeltaHandler() {
    return false;
  }

  /* see superclass */
  @Override
  public String getFileTypeFilter() {
    return null;
  }

  /* see superclass */
  @Override
  public void setFileTypeFilter(String fileTypeFilder) {
    // not used
  }

  /* see superclass */
  @Override
  public void setMimeType(String mimeType) {
    // not used
  }

  /* see superclass */
  @Override
  public String getMimeType() {
    return null;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    // not used
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Import Term Server";
  }

  /* see superclass */
  @Override
  public void setIoType(IoType ioType) {
    // not used
  }

  /* see superclass */
  @Override
  public IoType getIoType() {
    return IoType.API;
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

    return null;
  }

  /* see superclass */
  @Override
  public List<Concept> importConcepts(Translation translation,
    Map<String, String> headers) throws Exception {

    Logger.getLogger(getClass()).info(
        "Import translation concepts translationid: " + translation.getId());

    try (
        final TranslationService translationService =
            new TranslationServiceJpa();
        final SecurityService securityService = new SecurityServiceJpa();) {

      final Project project = translation.getProject();

      final List<ConceptRefsetMember> members =
          translation.getRefset().getMembers();

      final TerminologyHandler handler =
          translationService.getTerminologyHandler(project, headers);

      ConceptList concepts = null;
      final Set<Concept> conceptList = new HashSet<>();

      final int pageSize = Math.min(members.size(), requestSize);
      int iteration = 0;
      int remainder = members.size();

      do {

        Logger.getLogger(getClass()).info("FROM: " + pageSize * iteration
            + " TO: " + Math.min(((iteration + 1) * pageSize), members.size()));

        List<ConceptRefsetMember> conceptSubset =
            members.subList(pageSize * iteration,
                Math.min(((iteration + 1) * pageSize), members.size()));
        iteration++;

        List<String> terminologyIds = new ArrayList<>();
        for (ConceptRefsetMember c : conceptSubset) {
          // TODO: lookup workflow status and possibily skip if any work has
          // been done on it.
          terminologyIds.add(c.getConceptId());
        }

        concepts = handler.getConcepts(terminologyIds,
            translation.getTerminology(), translation.getVersion(), true);

        conceptList.addAll(concepts.getObjects());
        remainder = members.size() - (iteration * pageSize);
        Logger.getLogger(getClass()).info("Remainder is : " + remainder);

      } while (concepts != null && remainder > 0);

      // find translations which match refset language.
      Logger.getLogger(getClass())
          .info("trans language: " + translation.getLanguage());

      return createTranslations(translation, conceptList);
    }
  }

  /**
   * Creates the translations.
   *
   * @param translation the translation
   * @param conceptList the concept list
   * @return the list
   */
  private List<Concept> createTranslations(Translation translation,
    Set<Concept> conceptList) throws Exception {

    final Map<String, Concept> conceptCache = new HashMap<>();

    final Map<String, String> caseSignificanceToId = new HashMap<>();
    caseSignificanceToId.put("CASE_SENSITIVE", "900000000000017005");
    caseSignificanceToId.put("CASE_INSENSITIVE", "900000000000448009");
    caseSignificanceToId.put("INITIAL_CHARACTER_CASE_INSENSITIVE",
        "900000000000020002");

    for (Concept sourceConcept : conceptList) {

      for (Description originalDescription : sourceConcept.getDescriptions()) {

        if (translation.getLanguage()
            .equalsIgnoreCase(originalDescription.getLanguageCode())) {

          final Description description = new DescriptionJpa();
          setCommonFields(description, translation.getRefset());

          description.setEffectiveTime(originalDescription.getEffectiveTime());
          description.setTerminologyId(originalDescription.getTerminologyId());
          description.setLanguageCode(originalDescription.getLanguageCode());
          description.setTypeId(originalDescription.getTypeId());
          description.setTerm(originalDescription.getTerm());
          if (caseSignificanceToId
              .containsKey(originalDescription.getCaseSignificanceId())) {
            description.setCaseSignificanceId(caseSignificanceToId
                .get(originalDescription.getCaseSignificanceId()));
          } else {
            throw new Exception("Case significance value of "
                + originalDescription.getCaseSignificanceId()
                + " is not handled.");
          }

          Concept concept = null;
          if (!conceptCache.containsKey(sourceConcept.getTerminologyId())) {
            conceptCache.put(sourceConcept.getTerminologyId(),
                new ConceptJpa());
          }
          concept = conceptCache.get(sourceConcept.getTerminologyId());
          setCommonFields(concept, translation.getRefset());
          concept.setName(sourceConcept.getName());
          concept.setEffectiveTime(sourceConcept.getEffectiveTime());
          concept.setTerminologyId(sourceConcept.getTerminologyId());
          concept.setDefinitionStatusId(sourceConcept.getDefinitionStatusId());
          concept.setTranslation(translation);
          concept.getDescriptions().add(description);
          description.setConcept(concept);

          for (LanguageRefsetMember lrm : originalDescription
              .getLanguageRefsetMembers()) {
            setCommonFields(lrm, translation.getRefset());
            description.getLanguageRefsetMembers().add(lrm);
          }

        } // end language check
      } // end for description
    } // end for concept

    if (conceptCache != null && conceptCache.size() > 0) {
      validationResult.addComment(conceptCache.size()
          + " concepts with descriptions found on Term Server.");
    } else {
      validationResult
          .addComment("No concepts with descriptions found on Term Server");
    }

    return new ArrayList<>(conceptCache.values());
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public ValidationResult getValidationResults() throws Exception {
    return validationResult;
  }

}
