/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
 * Implementation of an algorithm to export a refset definition.
 */
public class ExportTranslationRf2Handler implements ExportTranslationHandler {

  /** The request cancel flag. */
  boolean requestCancel = false;
  
  // map languageRefset file dialect to description file dialect
  Map<String, String> nameMapping = new HashMap<>();
  
  // map translation langauge to languageRefsetId
  Map<String, String> languageRefsetIdMapping = new HashMap<>();
  
  
  String currentDate = "";

  /**
   * Instantiates an empty {@link ExportTranslationRf2Handler}.
   * @throws Exception if anything goes wrong
   */
  public ExportTranslationRf2Handler() throws Exception {
    super();
     
    currentDate = ConfigUtility.DATE_FORMAT.format(new Date());
    
    // Check config.properties to see if language contribution to file name needs to be adjusted 
    String[] keys = ConfigUtility.getConfigProperties()
          .getProperty("language.refset.dialect").split(",");
    
    // add the dialect entries for each requested property to the map/list
    for (String propertyKey : keys) {
      String infoString =
          ConfigUtility.getConfigProperties().getProperty("language.refset.dialect." + propertyKey);

      for (final String info : infoString.split(";")) {
        String[] values = FieldedStringTokenizer.split(info, "|");
        // return language-country mapped to desired display name
        nameMapping.put(values[2], values[3]);   
        // return language-country mapped tolanguageRefsetId
        languageRefsetIdMapping.put(values[2], values[1]);
      }
    }
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
    return "Export RF2";
  }

  /* see superclass */
  @Override
  public InputStream exportConcepts(Translation translation,
    List<Concept> concepts) throws Exception {
    Logger.getLogger(getClass()).info("Export translation concepts - "
        + translation.getTerminologyId() + ", " + translation.getName());

    // Make sure translated language has a refsetId listed in config.properties: "language.refset.dialect.DISPLAY="
    String languageRefsetId = (languageRefsetIdMapping.containsKey(translation.getLanguage()) ? languageRefsetIdMapping.get(translation.getLanguage()) : "");
   
    // Certain languages have no refsetId (e.g. Czech), so in those cases set to empty string. 
    if(languageRefsetId == null) { 
      languageRefsetId = "";
    }
    
    // Use info from nameMapping map and "translation" object to get file names right.
    String namespace = translation.getProject().getNamespace();
    String languageRefsetMemberFileName = "der2_cRefset_LanguageSnapshot-"
        + translation.getLanguage().replace('-', '_') + (namespace == null ? "nonamespace" : namespace) + "_" + currentDate + ".txt";
    String descriptionDialect = (nameMapping.containsKey(translation.getLanguage()) ? nameMapping.get(translation.getLanguage()) : translation.getLanguage()).replace('-', '_');
    String descriptionFileName = "sct2_Description_Snapshot-"
        + descriptionDialect + (namespace == null ? "nonamespace" : namespace)
            + "_" + currentDate + ".txt";

    // TODO: rewire this to just extract all descriptions, then all langauges
    // and call the exportDelta with those. then logic is the same
    // need to test import/export when we do this

    // Write descriptions and language refset entries
    // in SNOMED CT Structure to a .zip file
    // and write the zip file to an input stream
    StringBuilder descSb = new StringBuilder();
    descSb.append("id").append("\t");
    descSb.append("effectiveTime").append("\t");
    descSb.append("active").append("\t");
    descSb.append("moduleId").append("\t");
    descSb.append("conceptId").append("\t");
    descSb.append("languageCode").append("\t");
    descSb.append("typeId").append("\t");
    descSb.append("term").append("\t");
    descSb.append("caseSignificanceId");
    descSb.append("\r\n");

    StringBuilder langSb = new StringBuilder();
    langSb.append("id").append("\t");
    langSb.append("effectiveTime").append("\t");
    langSb.append("active").append("\t");
    langSb.append("moduleId").append("\t");
    langSb.append("refsetId").append("\t");
    langSb.append("referencedComponentId").append("\t");
    langSb.append("acceptabilityId");
    langSb.append("\r\n");

    for (final Concept concept : concepts) {
      if (concept.isRevision()) {
        continue;
      }
      for (final Description description : concept.getDescriptions()) {
        Logger.getLogger(getClass())
            .debug("  description = " + description.getTerminologyId() + ", "
                + description.getTerm() + ", "
                + description.getEffectiveTime());

        descSb.append(description.getTerminologyId()).append("\t");
        if (description.getEffectiveTime() != null) {
          descSb.append(
              ConfigUtility.DATE_FORMAT.format(description.getEffectiveTime()))
              .append("\t");
        } else {
          descSb.append("\t");
        }
        descSb.append(description.isActive() ? "1" : "0").append("\t");
        descSb.append(translation.getModuleId()).append("\t");
        descSb.append(concept.getTerminologyId()).append("\t");
        descSb.append(description.getLanguageCode()).append("\t");
        descSb.append(description.getTypeId()).append("\t");
        descSb.append(description.getTerm()).append("\t");
        descSb.append(description.getCaseSignificanceId());
        descSb.append("\r\n");

        for (final LanguageRefsetMember member : description
            .getLanguageRefsetMembers()) {
          Logger.getLogger(getClass()).debug("    member = " + member);
          langSb.append(member.getTerminologyId()).append("\t");
          if (member.getEffectiveTime() != null) {
            langSb
                .append(
                    ConfigUtility.DATE_FORMAT.format(member.getEffectiveTime()))
                .append("\t");
          } else {
            langSb.append("\t");
          }
          langSb.append(member.isActive() ? "1" : "0").append("\t");
          langSb.append(translation.getModuleId()).append("\t");
          langSb.append(languageRefsetId).append("\t");
          langSb.append(description.getTerminologyId()).append("\t");
          langSb.append(member.getAcceptabilityId());
          langSb.append("\r\n");
        }
      }

      // Free up memory
      concept.getDescriptions().clear();
    }

    //
    Logger.getLogger(getClass()).info("  prepare .zip file");

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ZipOutputStream zos = new ZipOutputStream(baos);

    final ZipEntry descEntry = new ZipEntry(descriptionFileName);
    zos.putNextEntry(descEntry);
    byte[] bytes = descSb.toString().getBytes("UTF-8");
    zos.write(bytes, 0, bytes.length);
    zos.closeEntry();

    final ZipEntry langEntry = new ZipEntry(languageRefsetMemberFileName);
    zos.putNextEntry(langEntry);

    bytes = langSb.toString().getBytes("UTF-8");
    zos.write(bytes, 0, bytes.length);
    zos.closeEntry();
    zos.close();
    return new ByteArrayInputStream(baos.toByteArray());
  }

  /* see superclass */
  @Override
  public InputStream exportDelta(Translation translation,
    List<Description> descriptions, List<LanguageRefsetMember> languages)
    throws Exception {
    Logger.getLogger(getClass()).info("Export translation contents - "
        + translation.getTerminologyId() + ", " + translation.getName());

    // Make sure translated language has a refsetId listed in config.properties: "language.refset.dialect.DISPLAY="
    String languageRefsetId = (languageRefsetIdMapping.containsKey(translation.getLanguage()) ? languageRefsetIdMapping.get(translation.getLanguage()) : "");

    // Certain languages have no refsetId (e.g. Czech), so in those cases set to empty string. 
    if(languageRefsetId == null) { 
      languageRefsetId = "";
    }
    
    // Use info from nameMapping map and "translation" object to get file names right.
    String languageRefsetMemberFileName = "der2_cRefset_LanguageDelta-"
        + translation.getLanguage().replace('-', '_') + translation.getProject().getNamespace() + "_" + currentDate + ".txt";
    String descriptionDialect = (nameMapping.containsKey(translation.getLanguage()) ? nameMapping.get(translation.getLanguage()) : translation.getLanguage()).replace('-', '_');
    String descriptionFileName = "sct2_Description_Delta-" + descriptionDialect + translation.getProject().getNamespace()
            + "_" + currentDate + ".txt";

    // Write descriptions and language refset entries
    // in SNOMED CT Structure to a .zip file
    // and write the zip file to an input stream
    StringBuilder descSb = new StringBuilder();
    descSb.append("id").append("\t");
    descSb.append("effectiveTime").append("\t");
    descSb.append("active").append("\t");
    descSb.append("moduleId").append("\t");
    descSb.append("conceptId").append("\t");
    descSb.append("languageCode").append("\t");
    descSb.append("typeId").append("\t");
    descSb.append("term").append("\t");
    descSb.append("caseSignificanceId");
    descSb.append("\r\n");

    StringBuilder langSb = new StringBuilder();
    langSb.append("id").append("\t");
    langSb.append("effectiveTime").append("\t");
    langSb.append("active").append("\t");
    langSb.append("moduleId").append("\t");
    langSb.append("refsetId").append("\t");
    langSb.append("referencedComponentId").append("\t");
    langSb.append("acceptabilityId");
    langSb.append("\r\n");

    for (Description description : descriptions) {
      Logger.getLogger(getClass())
          .info("  description = " + description.getTerminologyId() + ", "
              + description.getTerm() + ", " + description.getEffectiveTime());

      descSb.append(description.getTerminologyId()).append("\t");
      if (description.getEffectiveTime() != null) {
        descSb.append(
            ConfigUtility.DATE_FORMAT.format(description.getEffectiveTime()))
            .append("\t");
      } else {
        descSb.append("\t");
      }
      descSb.append(description.isActive() ? "1" : "0").append("\t");
      descSb.append(translation.getModuleId()).append("\t");
      descSb.append(description.getConcept().getTerminologyId()).append("\t");
      descSb.append(description.getLanguageCode()).append("\t");
      descSb.append(description.getTypeId()).append("\t");
      descSb.append(description.getTerm()).append("\t");
      descSb.append(description.getCaseSignificanceId());
      descSb.append("\r\n");
    }

    for (LanguageRefsetMember member : languages) {
      Logger.getLogger(getClass()).info("    member = " + member);
      langSb.append(member.getTerminologyId()).append("\t");
      if (member.getEffectiveTime() != null) {
        langSb
            .append(ConfigUtility.DATE_FORMAT.format(member.getEffectiveTime()))
            .append("\t");
      } else {
        langSb.append("\t");
      }
      langSb.append(member.isActive() ? "1" : "0").append("\t");
      langSb.append(translation.getModuleId()).append("\t");
      langSb.append(languageRefsetId).append("\t");
      langSb.append(member.getDescriptionId()).append("\t");
      langSb.append(member.getAcceptabilityId());
      langSb.append("\r\n");
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);

    /*
     * File is not on the disk, test.txt indicates only the file name to be put
     * into the zip
     */
    ZipEntry descEntry = new ZipEntry(descriptionFileName);
    zos.putNextEntry(descEntry);
    byte[] bytes = descSb.toString().getBytes("UTF-8");
    zos.write(bytes, 0, bytes.length);
    zos.closeEntry();

    ZipEntry langEntry = new ZipEntry(languageRefsetMemberFileName);
    zos.putNextEntry(langEntry);

    bytes = langSb.toString().getBytes("UTF-8");
    zos.write(bytes, 0, bytes.length);
    zos.closeEntry();
    zos.close();
    return new ByteArrayInputStream(baos.toByteArray());
  }

  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public String getFileName(String betaFileName) {
    // Strip off the "x" and remove the build date
    String fileExt = betaFileName.substring(betaFileName.lastIndexOf('.'));
    String fileName = betaFileName.substring(1, betaFileName.lastIndexOf('_'));
    return fileName + fileExt;
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
    String namespace = translation.getProject().getNamespace();
    return "xder2_translation_"
        + ConfigUtility.toCamelCase(translation.getName()) + type + "_"
        + (namespace == null || namespace.isEmpty() ? "INT" : namespace) + "_"
        + ConfigUtility.DATE_FORMAT5.format(new Date())
        + getFileTypeFilter();

  }

}
