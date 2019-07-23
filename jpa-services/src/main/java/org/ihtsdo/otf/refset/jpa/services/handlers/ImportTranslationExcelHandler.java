/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;

/**
 * Implementation of an algorithm to import a refset definition.
 */
public class ImportTranslationExcelHandler implements ImportTranslationHandler {

  
  private static final int CONCEPT_ID = 0;
  private static final int FSN_TERM = 1;
  private static final int TRANSLATED_TERM = 2;
  private static final int LANGUAGE_CODE = 3;
  private static final int CASE_SIGNIFIANCE = 4;
  private static final int TYPE = 5;
  private static final int LANGUAGE_REFERENCE_SET = 6;
  private static final int ACCEPTABILITY = 7;
  
  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The id. */
  final String id = "id";

  /** The validation result. */
  ValidationResult validationResult = new ValidationResultJpa();

  /**
   * Instantiates an empty {@link ImportTranslationExcelHandler}.
   * @throws Exception if anything goes wrong
   */
  public ImportTranslationExcelHandler() throws Exception {
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
    return ".xlsx";
  }

  /* see superclass */
  @Override
  public String getMimeType() {
    return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Import Excel";
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
  public List<Concept> importConcepts(Translation translation, InputStream content) throws Exception {
    Logger.getLogger(getClass()).info("Import translation concepts");

    // initialize
    validationResult = new ValidationResultJpa();

    /** The descriptions. */
    Map<String, Description> descriptions = new HashMap<>();

    /** The language entries. */
    Map<String, LanguageRefsetMember> descLangMap = new HashMap<>();
  

    
    final Map<String, Concept> conceptCache = new HashMap<>();
    // Handle the input stream as an input stream
    try (final Workbook workbook = WorkbookFactory.create(content);) {

      if (workbook.getNumberOfSheets() != 1) {
        throw new LocalException("Unexpected number of sheets in Excel file. File must have one sheet.");
      }

      // expecting only one workbook in file with translations.
      final Sheet sheet = workbook.getSheetAt(0);

      try (final RefsetService service = new RefsetServiceJpa();) {

        Logger.getLogger(getClass()).debug("Import translation Rf2 handler - reading Excel file.");

        boolean isFirstRow = true;
        for (final Row row : sheet) {

          // skip header
          if (isFirstRow) {
            isFirstRow = false;
            continue;
          }
          
          // file format
          // 1. Concept Id - CONCEPT_ID - 
          // 2. GB/US FSN Term - FSN_TERM - 
          // 3. Translated Term - TRANSLATED_TERM - 
          // 4. Language Code - LANGUAGE_CODE - 
          // 5. Case Signifiance - CASE_SIGNIFIANCE - used 
          // 6. Type - TYPE - used
          // 7. Language reference set - LANGUAGE_REFERENCE_SET - NOT used 
          // 8. Acceptability - ACCEPTABILITY - NOT used

          // Create description and populate from RF2
          final Description description = new DescriptionJpa();

          setCommonFields(description, translation.getRefset());
          // FSN Term
          description.setTerm(getCellValue(row, TRANSLATED_TERM));
          description.setTerminologyId(null);
          description.setLanguageCode(getCellValue(row, LANGUAGE_CODE));
          description.setTypeId(getCellValue(row, TYPE));
          description.setCaseSignificanceId(getCellValue(row, CASE_SIGNIFIANCE));
          description.setEffectiveTime(new Date());

          // Handle the concept the description is connected to
          final String conceptId = getCellValue(row, CONCEPT_ID);
          Concept concept = null;
          if (!conceptCache.containsKey(conceptId)) {
            conceptCache.put(conceptId, new ConceptJpa());
          }
          concept = conceptCache.get(conceptId);
          setCommonFields(concept, translation.getRefset());
          // not in file. set to current
          concept.setEffectiveTime(new Date());
          concept.setName(getCellValue(row, FSN_TERM));
          concept.setTerminologyId(conceptId);
          concept.setDefinitionStatusId("unknown");
          concept.setTranslation(translation);
          concept.getDescriptions().add(description);
          description.setConcept(concept);

          // Cache the description for lookup by the language reset member
          descriptions.put(conceptId, description);

          Logger.getLogger(getClass()).debug("  description = " + conceptId + ", "
              + description.getTerminologyId() + ", " + description.getTerm());

        }


        // Assign identifiers if descriptions have "TMP-" ids
        final IdentifierAssignmentHandler handler = service.getIdentifierAssignmentHandler(ConfigUtility.DEFAULT);
        for (final Description description : descriptions.values()) {
          if (description.getTerminologyId() == null || description.getTerminologyId().startsWith("TMP-")) {
            description.setTerminologyId("");
            description.setTerminologyId(handler.getTerminologyId(description));
            for (final LanguageRefsetMember member : description.getLanguageRefsetMembers()) {
              member.setDescriptionId(description.getTerminologyId());
            }
          }
        }

        validationResult.addComment(descriptions.size() + " descriptions successfully loaded.");

      } catch (Exception e) {
        Logger.getLogger(getClass()).error("", e);
        throw e;
      }
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
  
  //convert cell value to string
  private String getCellValue(Row row, int cellIndex) {
    
    String value = null;
    if (row.getCell(cellIndex).getCellType() == CellType.STRING) {
      value = row.getCell(cellIndex).getStringCellValue();
    } else if (row.getCell(cellIndex).getCellType() == CellType.NUMERIC) {
      value = NumberToTextConverter.toText(row.getCell(cellIndex).getNumericCellValue());
    }
    return value;
  }

}
