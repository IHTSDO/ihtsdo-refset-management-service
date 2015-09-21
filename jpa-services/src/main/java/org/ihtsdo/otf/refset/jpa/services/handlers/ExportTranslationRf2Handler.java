/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.algo.Algorithm;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefSetMember;
import org.ihtsdo.otf.refset.services.handlers.ExportTranslationHandler;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to export a refset definition.
 */
public class ExportTranslationRf2Handler extends RootServiceJpa implements
    ExportTranslationHandler, Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  boolean requestCancel = false;

  /**
   * Instantiates an empty {@link ExportTranslationRf2Handler}.
   * @throws Exception if anything goes wrong
   */
  public ExportTranslationRf2Handler() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Export RF2 .zip file";
  }

  @Override
  public InputStream exportConcepts(Translation translation,
    List<Concept> concepts) throws Exception {
    // Use info from "translation" object to get file name right.
    String languageRefsetMemberFileName = "der2_cRefset_LanguageSnapshot-" + 
        translation.getLanguage() + "_INT_" + translation.getVersion() + ".txt";
    String descriptionFileName = "sct2_Description_INT_" + translation.getVersion() + 
        ".txt";
    
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
    descSb.append("caseSignificanceId").append("\t");
    descSb.append("\r\n");
    
    StringBuilder langSb = new StringBuilder();
    langSb.append("id").append("\t");
    langSb.append("effectiveTime").append("\t");
    langSb.append("active").append("\t");
    langSb.append("moduleId").append("\t");
    langSb.append("refsetId").append("\t");
    langSb.append("referencedComponentId").append("\t");
    langSb.append("acceptabilityId").append("\t");
    langSb.append("\r\n");
    
    
    for (Concept concept : concepts) {
      for (Description description : concept.getDescriptions()) {
        
        descSb.append(description.getTerminologyId()).append("\t");
        descSb.append(ConfigUtility.DATE_FORMAT.format(description.getEffectiveTime())).append("\t");
        descSb.append(description.isActive() ? "1" : "0").append("\t");
        descSb.append(description.getModuleId()).append("\t");
        descSb.append(concept.getTerminologyId()).append("\t");
        descSb.append(description.getLanguageCode()).append("\t");
        descSb.append(description.getTypeId()).append("\t");
        descSb.append(description.getTerm()).append("\t");
        descSb.append(description.getCaseSignificanceId()).append("\t");
        descSb.append("\r\n");
        
        for (LanguageRefSetMember lrm : description.getLanguageRefSetMembers()) {
          langSb.append(lrm.getTerminologyId()).append("\t");
          langSb.append(ConfigUtility.DATE_FORMAT.format(lrm.getEffectiveTime())).append("\t");
          langSb.append(lrm.isActive() ? "1" :"0").append("\t");
          langSb.append(lrm.getModuleId()).append("\t");
          langSb.append(lrm.getRefSetId()).append("\t"); 
          langSb.append(description.getTerminologyId()).append("\t");
          langSb.append(lrm.getAcceptabilityId()).append("\t");
          langSb.append("\r\n");
        }
      }
    }


    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try(ZipOutputStream zos = new ZipOutputStream(baos)) {

      /* File is not on the disk, test.txt indicates
         only the file name to be put into the zip */
      ZipEntry descEntry = new ZipEntry(descriptionFileName); 
      zos.putNextEntry(descEntry);
      zos.write(descSb.toString().getBytes());
      zos.closeEntry();

      ZipEntry langEntry = new ZipEntry(languageRefsetMemberFileName); 
      zos.putNextEntry(langEntry);
      zos.write(langSb.toString().getBytes());
      zos.closeEntry();

      } catch(IOException ioe) {
        ioe.printStackTrace();
      }
    
    return new ByteArrayInputStream(baos.toByteArray());
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a
  }

  /**
   * Fires a {@link ProgressEvent}.
   * @param pct percent done
   * @param note progress note
   */
  public void fireProgressEvent(int pct, String note) {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    Logger.getLogger(getClass()).info("    " + pct + "% " + note);
  }

  /* see superclass */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /* see superclass */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }

  /* see superclass */
  @Override
  public void cancel() {
    requestCancel = true;
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
