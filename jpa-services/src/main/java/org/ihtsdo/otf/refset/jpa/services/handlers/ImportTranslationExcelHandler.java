/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.jpa.ConceptNoteJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportExportAbstract;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;

/**
 * Implementation of an algorithm to import a refset definition.
 */
public class ImportTranslationExcelHandler extends ImportExportAbstract
    implements ImportTranslationHandler {

  /** The Constant CONCEPT_ID. */
  private static final int CONCEPT_ID = 0;

  /** The Constant FSN_TERM. */
  private static final int FSN_TERM = 1;

  /** The Constant PERFERRED_TERM */
  private static final int PREFERRED_TERM = 2;

  /** The Constant TRANSLATED_TERM. */
  private static final int TRANSLATED_TERM = 3;

  /** The Constant LANGUAGE_CODE. */
  private static final int LANGUAGE_CODE = 4;

  /** The Constant CASE_SIGNIFIANCE. */
  private static final int CASE_SIGNIFIANCE = 5;

  /** The Constant TYPE. */
  private static final int TYPE = 6;

  /** The Constant LANGUAGE_REFERENCE_SET. */
  private static final int LANGUAGE_REFERENCE_SET = 7;

  /** The Constant ACCEPTABILITY. */
  private static final int ACCEPTABILITY = 8;

  /** The Constant NOTES. */
  private static final int NOTES = 13;

  // file format
  // 01. Concept Id - CONCEPT_ID
  // 02. GB/US FSN Term - FSN_TERM
  // 03. Preferred Term (For reference only)
  // 04. Translated Term - TRANSLATED_TERM
  // 05. Language Code - LANGUAGE_CODE
  // 06. Case Signifiance - CASE_SIGNIFIANCE
  // 07. Type - TYPE
  // 08. Language reference set - LANGUAGE_REFERENCE_SET used
  // 09. Acceptability - ACCEPTABILITY - used
  // 10. Language reference set - LANGUAGE_REFERENCE_SET - NOT used
  // 11. Acceptability - ACCEPTABILITY - NOT used
  // 12. Language reference set - LANGUAGE_REFERENCE_SET - NOT used
  // 13. Acceptability - ACCEPTABILITY - NOT used
  // 14. Notes

  private Map<Integer, String> columnIndexNameMap = new HashMap<>() {
    {
      put(0, "Concept ID");
      put(1, "GB/US FSN Term");
      put(2, "Preferred Term");
      put(3, "Translated Term");
      put(4, "Language Code");
      put(5, "Case significance");
      put(6, "Type");
      put(7, "Language reference set");
      put(8, "Acceptability");
      put(9, "Language reference set");
      put(10, "Acceptability");
      put(11, "Language reference set");
      put(12, "Acceptability");
      put(13, "Notes");
    }
  };

  private Map<String, String> acceptibilityCodeMap = new HashMap<>();

  private Map<String, String> descriptionTypeCodeMap = new HashMap<>();

  private Map<String, String> caseSignificanceCodeMap = new HashMap<>();

  private Map<String, String> languageReferenceSetCodeMap = new HashMap<>();

  private Map<String, String> inactivationReasonCodeMap = new HashMap<>();

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
    populateCodeMaps();
  }

  /**
   * Populate the code maps
   */
  private void populateCodeMaps() {
    acceptibilityCodeMap.put("PREFERRED", "900000000000548007");
    acceptibilityCodeMap.put("ACCEPTABLE", "900000000000549004");

    descriptionTypeCodeMap.put("SYNONYM", "900000000000013009");
    descriptionTypeCodeMap.put("FSN", "900000000000003001");

    caseSignificanceCodeMap.put("ci", "900000000000448009");
    caseSignificanceCodeMap.put("CS", "900000000000017005");
    caseSignificanceCodeMap.put("cI", "900000000000020002");

    languageReferenceSetCodeMap.put("Belgian French", "21000172104");
    languageReferenceSetCodeMap.put("Belgian Dutch", "31000172101");
    languageReferenceSetCodeMap.put("GB English", "900000000000508004");
    languageReferenceSetCodeMap.put("US English", "900000000000509007");
    languageReferenceSetCodeMap.put("Irish", "21000220103");
    languageReferenceSetCodeMap.put("Danish", "554461000005103");
    languageReferenceSetCodeMap.put("Swiss German", "2041000195100");
    languageReferenceSetCodeMap.put("Swiss French", "2021000195106");
    languageReferenceSetCodeMap.put("Swiss Italian", "2031000195108");
    languageReferenceSetCodeMap.put("Norwegian Bokmål", "61000202103");
    languageReferenceSetCodeMap.put("Norwegian Nynorsk", "91000202106");
    languageReferenceSetCodeMap.put("Estonian", "71000181105");
    languageReferenceSetCodeMap.put("Swedish", "46011000052107");

    inactivationReasonCodeMap.put("Not semantically equivalent", "723278000");
    inactivationReasonCodeMap.put("Outdated", "900000000000483008");
    inactivationReasonCodeMap.put("Erroneous", "900000000000485001");
    inactivationReasonCodeMap.put("Non-conformance to editorial policy", "723277005");
    inactivationReasonCodeMap.put("Duplicate", "900000000000482003");
    inactivationReasonCodeMap.put("Ambiguous", "900000000000484000");
    inactivationReasonCodeMap.put("Moved elsewhere", "900000000000487009");
    inactivationReasonCodeMap.put("Limited", "900000000000486000");
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
  public void setFileTypeFilter(String fileTypeFilter) {
    // not used
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
  public void setMimeType(String mimeType) {
    // not used
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    // not used
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Import Excel";
  }

  /* see superclass */
  @Override
  public void setIoType(IoType ioType) {
    // not used
  }

  /* see superclass */
  @Override
  public IoType getIoType() {
    return IoType.FILE;
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
  public List<Concept> importConcepts(Translation translation, InputStream content)
    throws Exception {
    Logger.getLogger(getClass()).info("Import translation concepts");

    // initialize
    validationResult = new ValidationResultJpa();

    int skippedDueToLanguageNotMatching = 0;
    int skippedDueToMissingData = 0;

    // map languageRefset file dialect to description file dialect
    Map<String, String> nameMapping = new HashMap<>();

    // Check config.properties to see if language contribution to file name
    // needs to be adjusted
    String[] keys =
        ConfigUtility.getConfigProperties().getProperty("language.refset.dialect").split(",");

    // add the dialect entries for each requested property to the map/list
    for (String propertyKey : keys) {
      String infoString =
          ConfigUtility.getConfigProperties().getProperty("language.refset.dialect." + propertyKey);

      for (final String info : infoString.split(";")) {
        String[] values = FieldedStringTokenizer.split(info, "|");
        // return full-name mapped to language-dialect combo
        nameMapping.put(values[0], values[2]);
      }
    }

    /** The descriptions. */
    List<Description> descriptions = new ArrayList<>();

    final Map<String, Concept> conceptCache = new HashMap<>();
    // Handle the input stream as an input stream
    try (final Workbook workbook = WorkbookFactory.create(content);) {

      // expecting given template
      final Sheet sheet = workbook.getSheet("Description Additions");

      try (final RefsetService service = new RefsetServiceJpa();) {

        Logger.getLogger(getClass()).debug("Import translation Rf2 handler - reading Excel file.");

        boolean isFirstRow = true;
        for (final Row row : sheet) {

          // skip header
          if (isFirstRow) {
            isFirstRow = false;
            continue;
          }
          
          //pre-emptive check for translation term and ID
          if((row.getCell(3) == null || row.getCell(3).getStringCellValue() == "") && (row.getCell(1) == null || row.getCell(1).getStringCellValue() == ""))
            continue;

          // file format
          // 1. Concept Id - CONCEPT_ID -
          // 2. GB/US FSN Term - FSN_TERM -
          // 3. Preferred Term (For reference only)
          // 4. Translated Term - TRANSLATED_TERM -
          // 5. Language Code - LANGUAGE_CODE -
          // 6. Case Signifiance - CASE_SIGNIFIANCE - used
          // 7. Type - TYPE - used
          // 8. Language reference set - LANGUAGE_REFERENCE_SET - NOT used
          // 9. Acceptability - ACCEPTABILITY - used
          // 10. Language reference set - LANGUAGE_REFERENCE_SET - NOT used
          // 11. Acceptability - ACCEPTABILITY - NOT used
          // 12. Language reference set - LANGUAGE_REFERENCE_SET - NOT used
          // 13. Acceptability - ACCEPTABILITY - NOT used
          // 14. Notes

          boolean skipRow = false;
          // Check for missing required data
          for (int i = 0; i < columnIndexNameMap.size(); i++) {
            // The language reference set column is not required.
            if (i == LANGUAGE_REFERENCE_SET) {
              continue;
            }
            if (row.getCell(i) == null) {
              validationResult.addError("Required \"" + columnIndexNameMap.get(i)
                  + "\" data missing for at least one row.");

              skippedDueToMissingData++;
              skipRow = true;
            }
          }
          if (skipRow) {
            continue;
          }

          // If excel language doesn't match translation's language, skip
          if (!translation.getLanguage().equals(getCellValue(row, LANGUAGE_CODE))) {
            // check if the dialect matches, before skipping
            if (nameMapping.get(getCellValue(row, LANGUAGE_REFERENCE_SET)) == null
                || !translation.getLanguage()
                    .contentEquals(nameMapping.get(getCellValue(row, LANGUAGE_REFERENCE_SET)))) {
              skippedDueToLanguageNotMatching++;
              continue;
            }
          }

          // Create description and populate from RF2
          final Description description = new DescriptionJpa();

          setCommonFields(description, translation.getRefset());
          // FSN Term
          description.setTerm(getCellValue(row, TRANSLATED_TERM));
          description.setTerminologyId(null);
          description.setLanguageCode(translation.getLanguage());
          description.setTypeId(descriptionTypeCodeMap.get(getCellValue(row, TYPE)));
          String caseSignificanceString = getCellValue(row, CASE_SIGNIFIANCE);
          String caseSignificanceId = caseSignificanceCodeMap.get(caseSignificanceString);
          description.setCaseSignificanceId(
              caseSignificanceCodeMap.get(getCellValue(row, CASE_SIGNIFIANCE)));
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
          concept.setName(getCellValue(row, PREFERRED_TERM));
          concept.setTerminologyId(conceptId);
          concept.setDefinitionStatusId("unknown");
          concept.setTranslation(translation);
          concept.getDescriptions().add(description);

          final String notes = getCellValue(row, NOTES);
          if (StringUtils.isNotBlank(notes)) {
            final Note conceptNote = new ConceptNoteJpa();
            conceptNote.setValue(notes);
            ((ConceptNoteJpa) conceptNote).setConcept(concept);

            concept.getNotes().add(conceptNote);
          }

          description.setConcept(concept);

          // Cache the description for lookup by the language reset member
          descriptions.add(description);

          Logger.getLogger(getClass()).debug("  description = " + conceptId + ", "
              + description.getTerminologyId() + ", " + description.getTerm());

          // Create and configure the member
          final LanguageRefsetMember member = new LanguageRefsetMemberJpa();
          setCommonFields(member, translation.getRefset());
          member.setTerminologyId(UUID.randomUUID().toString());
          member.setEffectiveTime(new Date());

          // Set from the translation refset
          member.setRefsetId(translation.getRefset().getTerminologyId());

          // Language unique attributes
          member.setAcceptabilityId(acceptibilityCodeMap.get(getCellValue(row, ACCEPTABILITY)));

          // Connect description and language refset member object
          member.setDescriptionId(description.getTerminologyId());
          description.getLanguageRefsetMembers().add(member);
        }

        // Assign identifiers
        final IdentifierAssignmentHandler handler =
            service.getIdentifierAssignmentHandler(ConfigUtility.DEFAULT);
        for (final Description description : descriptions) {
          if (description.getTerminologyId() == null
              || description.getTerminologyId().startsWith("TMP-")) {
            description.setTerminologyId("");
            description.setTerminologyId(handler.getTerminologyId(description));
          }
        }

        validationResult.addComment(descriptions.size() + " descriptions read from file.");

        if (skippedDueToLanguageNotMatching > 0) {
          validationResult.addComment(skippedDueToLanguageNotMatching
              + " descriptions skipped: language code in file not same as translation language.");
        }
        if (skippedDueToMissingData > 0) {
          validationResult
              .addComment(skippedDueToMissingData + " descriptions skipped: missing required data");
        }

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

  /* see superclass */
  @Override
  public ValidationResult getValidationResults() throws Exception {
    return validationResult;
  }

  /**
   * Returns the cell value.
   *
   * @param row the row
   * @param cellIndex the cell index
   * @return the cell value
   */
  // convert cell value to string
  private String getCellValue(Row row, int cellIndex) {

    String value = null;
    if (row.getCell(cellIndex).getCellType() == CellType.STRING) {
      value = row.getCell(cellIndex).getStringCellValue();
    } else if (row.getCell(cellIndex).getCellType() == CellType.NUMERIC) {
      value = NumberToTextConverter.toText(row.getCell(cellIndex).getNumericCellValue());
    }
    return value;
  }

  @Override
  public List<Concept> importConcepts(Translation translation, Map<String, String> headers)
    throws Exception {
    // not implemented
    return null;
  }

}
