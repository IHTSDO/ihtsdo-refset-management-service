/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.algo.Algorithm;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefSetMember;
import org.ihtsdo.otf.refset.rf2.SimpleRefSetMember;
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to export a refset definition.
 */
public class ExportRefsetRf2Handler extends RootServiceJpa implements
    ExportRefsetHandler, Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  boolean requestCancel = false;

  /**
   * Instantiates an empty {@link ExportRefsetRf2Handler}.
   * @throws Exception if anything goes wrong
   */
  public ExportRefsetRf2Handler() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Export RF2";
  }

  /* see superclass */
  @Override
  public InputStream exportMembers(Refset refset, List<SimpleRefSetMember> members)
    throws Exception {
    // Write a header
    // Obtain members for refset,
    // Write RF2 simple refset pattern to a StringBuilder
    // wrap and return the string for that as an input stream
    
    StringBuilder sb = new StringBuilder();
    sb.append("id").append("\t");
    sb.append("effectiveTime").append("\t");
    sb.append("active").append("\t");
    sb.append("moduleId").append("\t");
    sb.append("refsetId").append("\t");
    sb.append("referencedComponentId").append("\t");
    sb.append("\r\n");
    
    for (SimpleRefSetMember member : members) {
      sb.append(member.getTerminologyId()).append("\t");
      sb.append(ConfigUtility.DATE_FORMAT.format(member.getEffectiveTime())).append("\t");
      sb.append(member.isActive() ? "1" : "0").append("\t");
      sb.append(member.getModuleId()).append("\t");
      sb.append(member.getRefSetId()).append("\t");
      sb.append(member.getComponent().getTerminologyId()).append("\t");
      sb.append("\r\n");
    }
    
    return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
  }

  /* see superclass */
  @Override
  public InputStream exportDefinition(Refset refset) throws Exception {
    // Write RF2 refset definition pattern  to an input stream
    StringBuilder sb = new StringBuilder();
    sb.append("id").append("\t");  
    sb.append("effectiveTime").append("\t");  
    sb.append("active").append("\t");
    sb.append("moduleId").append("\t");  
    sb.append("refsetId").append("\t");  
    sb.append("referencedComponentId").append("\t");  
    sb.append("definition").append("\t");  
    sb.append("\r\n");  
    
    sb.append(refset.getDefinitionUuid()).append("\t");
    sb.append(ConfigUtility.DATE_FORMAT.format(refset.getEffectiveTime())).append("\t");
    sb.append("1").append("\t");
    sb.append(refset.getModuleId()).append("\t");
    sb.append(refset.getTerminologyId()).append("\t");
    // fake id for now
    sb.append(refset.getTerminologyId()).append("\t"); 
    sb.append(refset.getDefinition()).append("\t");
      
    sb.append("\r\n");
    
    return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
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
