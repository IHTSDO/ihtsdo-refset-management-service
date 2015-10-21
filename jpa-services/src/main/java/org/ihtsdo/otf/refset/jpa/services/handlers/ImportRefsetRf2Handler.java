/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.handlers.ImportRefsetHandler;
import org.ihtsdo.otf.refset.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import a refset definition.
 */
public class ImportRefsetRf2Handler implements ImportRefsetHandler {

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** counter for objects created, reset in each load section. */
  int objectCt; //

  /** The id. */
  final String id = "id";

  /**
   * Instantiates an empty {@link ImportRefsetRf2Handler}.
   * @throws Exception if anything goes wrong
   */
  public ImportRefsetRf2Handler() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public String getFileTypeFilter() {
    return ".txt";
  }

  /* see superclass */
  @Override
  public String getMimeType() {
    return "text/plain";
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Import RF2";
  }

  /* see superclass */
  @Override
  public List<ConceptRefsetMember> importMembers(Refset refset,
    InputStream content) throws Exception {
    Logger.getLogger(getClass()).info("Import refset members ");

    // Read from input stream
    List<ConceptRefsetMember> list = new ArrayList<>();
    String line = "";
    Reader reader = new InputStreamReader(content, "UTF-8");
    PushBackReader pbr = new PushBackReader(reader);
    while ((line = pbr.readLine()) != null) {

      // Strip \r and split lines
      line = line.replace("\r", "");
      final String fields[] = line.split("\t");

      // Check field lengths
      if (fields.length != 6) {
        pbr.close();
        Logger.getLogger(getClass()).error("line = " + line);
        throw new Exception(
            "Unexpected field count in simple refset member file "
                + fields.length);
      }

      // skip header
      if (!fields[0].equals("id")) { // header

        // Skip inactive entries
        if (fields[2].equals("0")) {
          continue;
        }
        
        // Instantiate and populate members
        final ConceptRefsetMember member = new ConceptRefsetMemberJpa();
        setCommonFields(member, refset);
        member.setRefset(refset);
        member.setConceptId(fields[5]);

        // Add member
        list.add(member);
        Logger.getLogger(getClass()).debug("  member = " + member);
      }
    }
    pbr.close();
    return list;
  }

  /* see superclass */
  @Override
  public String importDefinition(InputStream content) throws Exception {
    Logger.getLogger(getClass()).info("Import refset definition");

    String line = "";

    // Read from input stream
    Reader reader = new InputStreamReader(content, "UTF-8");
    PushBackReader pbr = new PushBackReader(reader);
    while ((line = pbr.readLine()) != null) {

      // Strip \r chars and split line
      line = line.replace("\r", "");
      final String fields[] = line.split("\t");

      // Check fields
      if (fields.length != 7) {
        pbr.close();
        throw new Exception("Unexpected field count in refset definition file.");
      }

      // skip header
      if (!fields[0].equals("id")) {

        // Return fields[6]
        pbr.close();
        Logger.getLogger(getClass()).debug("  definition = " + fields[6]);
        return fields[6];
      }
    }
    pbr.close();
    return null;
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
    c.setId(null);
    c.setPublishable(true);
    c.setPublished(false);
    c.setModuleId(refset.getModuleId());
    c.setTerminology(refset.getTerminology());
    c.setVersion(refset.getVersion());
  }
}
