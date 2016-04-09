/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;

/**
 * Implementation of an algorithm to export a refset definition.
 */
public class ExportRefsetRf2WithNameHandler implements ExportRefsetHandler {

  /** The request cancel flag. */
  boolean requestCancel = false;

  /**
   * Instantiates an empty {@link ExportRefsetRf2WithNameHandler}.
   * @throws Exception if anything goes wrong
   */
  public ExportRefsetRf2WithNameHandler() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public String getFileTypeFilter() {
    return ".txt";
  }

  /* see superclass */
  @Override
  public String getFileName(String betaFileName) {
    return betaFileName.substring(1);
  }

  /* see superclass */
  @Override
  public String getBetaFileName(String namespace, String type, String version) {
    return "xder2_Refset_Simple" + type + "_" + namespace + "_" + version
        + getFileTypeFilter();
  }

  /* see superclass */
  @Override
  public String getMimeType() {
    return "text/plain";
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Export RF2 with name";
  }

  /* see superclass */
  @Override
  public InputStream exportMembers(Refset refset,
    List<ConceptRefsetMember> members) throws Exception {
    Logger.getLogger(getClass()).info("Export refset members- "
        + refset.getTerminologyId() + ", " + refset.getName());

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
    sb.append("name").append("\t");
    sb.append("\r\n");

    for (ConceptRefsetMember member : members) {
      Logger.getLogger(getClass()).debug("  member = " + member);
      sb.append(member.getTerminologyId()).append("\t");
      if (member.getEffectiveTime() != null) {
        sb.append(ConfigUtility.DATE_FORMAT.format(member.getEffectiveTime()))
            .append("\t");
      } else {
        sb.append("\t");
      }
      sb.append(1).append("\t");
      sb.append(refset.getModuleId()).append("\t");
      sb.append(member.getRefset().getTerminologyId()).append("\t");
      sb.append(member.getConceptId()).append("\t");
      sb.append(member.getConceptName()).append("\t");
      sb.append("\r\n");
    }

    return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
  }

  /* see superclass */
  @Override
  public InputStream exportDefinition(Refset refset) throws Exception {
    Logger.getLogger(getClass())
        .info("Export refset definition - " + refset.getTerminologyId() + ", "
            + refset.getName() + ", " + refset.getDefinitionClauses());

    // Write RF2 refset definition pattern to an input stream
    StringBuilder sb = new StringBuilder();
    sb.append("id").append("\t");
    sb.append("effectiveTime").append("\t");
    sb.append("active").append("\t");
    sb.append("moduleId").append("\t");
    sb.append("refsetId").append("\t");
    sb.append("referencedComponentId").append("\t");
    sb.append("definition").append("\t");
    sb.append("\r\n");

    sb.append(UUID.randomUUID().toString()).append("\t");
    if (refset.getEffectiveTime() != null) {
      sb.append(ConfigUtility.DATE_FORMAT.format(refset.getEffectiveTime()))
          .append("\t");
    } else {
      sb.append("\t");
    }
    sb.append(1).append("\t");
    sb.append(refset.getModuleId()).append("\t");
    sb.append(refset.getTerminologyId()).append("\t");
    // fake id for now
    sb.append(refset.getTerminologyId()).append("\t");
    sb.append(refset.computeDefinition()).append("\t");
    sb.append("\r\n");

    return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

}
