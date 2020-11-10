/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.services.handlers.ExportTranslationHandler;

/**
 * Implementation of an algorithm to export translations into 
 * Authoring Platform translation import template version 2 format
 */
public class ExportTranslationTsvHandler implements ExportTranslationHandler {

  /** The request cancel flag. */
  boolean requestCancel = false;
  
  /**  The case significance map. */
  Map<String, String> caseSignificanceMap = new HashMap<>();
  
  /**  The allowed language refsets. */
  Map<String, String> allowedLanguageRefsets = new HashMap<>();
  
  /**  The language refset to language code map. */
  Map<String, String> languageRefsetToLanguageCodeMap = new HashMap<>();

  /**
   * Instantiates an empty {@link ExportTranslationTsvHandler}.
   * @throws Exception if anything goes wrong
   */
  public ExportTranslationTsvHandler() throws Exception {
    super();
    caseSignificanceMap.put("900000000000448009", "ci");
    caseSignificanceMap.put("900000000000017005", "CS");
    caseSignificanceMap.put("900000000000020002", "cl");
    
    String infoString =
        ConfigUtility.getConfigProperties().getProperty("language.refset.dialect.MANAGED-SERVICE");

    for (final String info : infoString.split(";")) {
      String[] values = FieldedStringTokenizer.split(info, "|");
      
      // return language-country mapped to full-name
      allowedLanguageRefsets.put(values[2], values[0]); 
      languageRefsetToLanguageCodeMap.put(values[2], values[4]);
    }
  }

  /* see superclass */
  @Override
  public String getFileTypeFilter() {
    return ".tsv";
  }

  /* see superclass */
  @Override
  public String getMimeType() {
    return "text/plain";
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Export TSV";
  }

  /* see superclass */
  @Override
  public InputStream exportConcepts(Translation translation,
    List<Concept> concepts) throws Exception {
    Logger.getLogger(getClass()).info("Export translation concepts - "
        + translation.getTerminologyId() + ", " + translation.getName());

    
    StringBuilder descSb = new StringBuilder();
    descSb.append("Concept Id").append("\t");
    descSb.append("GB/US FSN Term (For reference only)").append("\t");
    descSb.append("Translated Term").append("\t");
    descSb.append("Language Code").append("\t");
    descSb.append("Case significance").append("\t");
    descSb.append("Type").append("\t");
    descSb.append("Language reference set").append("\t");
    descSb.append("Acceptability").append("\t");
    // these last columns will not be used, but are required on the template
    descSb.append("Language reference set").append("\t");
    descSb.append("Acceptability").append("\t");
    descSb.append("Language reference set").append("\t");
    descSb.append("Acceptability").append("\t");
    descSb.append("\r\n");

    for (final Concept concept : concepts) {
      
      if (concept.isRevision()) {
        continue;
      }
      
      // will save the synonyms to append at the end, to ensure PTs get printed first
      List<String> cachedSynMembers = new ArrayList<>();   
      
      for (final Description description : concept.getDescriptions()) {
        Logger.getLogger(getClass())
            .debug("  description = " + description.getTerminologyId() + ", "
                + description.getTerm());

        if (!description.isActive()) {
          continue;
        }     
       
        for (final LanguageRefsetMember member : description
            .getLanguageRefsetMembers()) {

          StringBuffer thisMember = new StringBuffer();
          Logger.getLogger(getClass()).debug("    member = " + member);
          thisMember.append(concept.getTerminologyId()).append("\t");

          thisMember.append(concept.getName()).append("\t");
          thisMember.append(description.getTerm()).append("\t");
          if (languageRefsetToLanguageCodeMap.containsKey(description.getLanguageCode())) {
            thisMember.append(languageRefsetToLanguageCodeMap.get(description.getLanguageCode())).append("\t");
          }  else {
            throw new LocalException("The TSV export handler does not currently support language " + description.getLanguageCode() + ".");
          }
          thisMember.append(caseSignificanceMap.get(description.getCaseSignificanceId())).append("\t");
          thisMember.append(description.getTypeId().contentEquals("900000000000003001") ? "FSN" : "SYNONYM").append("\t");
          if (allowedLanguageRefsets.containsKey(description.getLanguageCode())) {
            thisMember.append(allowedLanguageRefsets.get(description.getLanguageCode())).append("\t");
          } else {
            throw new LocalException("The TSV export handler does not currently support language " + description.getLanguageCode() + ".");
          }
          thisMember.append(member.getAcceptabilityId().contentEquals("900000000000548007") ? "PREFERRED" : "ACCEPTABLE");
          thisMember.append("\r\n");
          
          // if preferred, write out to descSb first
          if (member.getAcceptabilityId().contentEquals("900000000000548007")) {
            descSb.append(thisMember);
          // otherwise synonyms will get cached till after PTs are written
          } else {
            cachedSynMembers.add(thisMember.toString());
          }
          
        }
        
       
      }
      // PTs are already added to return buffer, so add the synonyms now
      for (String synMember : cachedSynMembers) {
        descSb.append(synMember);
      }
      
      // The TSV export handler does not currently support language xx / xx-YY

      // Free up memory
      concept.getDescriptions().clear();
    }

    //
    Logger.getLogger(getClass()).info("  prepare .zip file");

    
    return new ByteArrayInputStream(descSb.toString().getBytes("UTF-8"));
  }

  /* see superclass */
  @Override
  public InputStream exportDelta(Translation translation,
    List<Description> descriptions, List<LanguageRefsetMember> languages)
    throws Exception {
 
    return null;
  }

  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public String getFileName(String betaFileName) {
    return betaFileName;
  }

  /**
   * Returns the beta file name.
   *
   * @param translation the translation
   * @param type the type
   * @param version the version
   * @return the beta file name
   */
  @Override
  public String getBetaFileName(Translation translation, String type,
    String version) {
    return ConfigUtility.toCamelCase(translation.getName()) + type + "_"
        + translation.getLanguage() + "_" + ConfigUtility.DATE_FORMAT5.format(new Date())
        + getFileTypeFilter();

  }

}
