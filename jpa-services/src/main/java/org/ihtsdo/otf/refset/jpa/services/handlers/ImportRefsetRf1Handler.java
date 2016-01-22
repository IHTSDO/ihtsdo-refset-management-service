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
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.handlers.ImportRefsetHandler;
import org.ihtsdo.otf.refset.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import refset members from RF1.
 */
public class ImportRefsetRf1Handler implements ImportRefsetHandler {

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** counter for objects created, reset in each load section. */
  int objectCt; //

  /** The id. */
  final String id = "id";

  /**
   * Instantiates an empty {@link ImportRefsetRf1Handler}.
   * @throws Exception if anything goes wrong
   */
  public ImportRefsetRf1Handler() throws Exception {
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
    return "Import RF1";
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
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      // Check field lengths
      // Support any RF1 refset file with 4 fields (4th field might be blank)
      if (fields.length < 3) {
        pbr.close();
        Logger.getLogger(getClass()).error("line = " + line);
        throw new LocalException(
            "Unexpected field count in subsetmembers file " + fields.length);
      }

      // skip header
      if (!fields[1].equals("MEMBERID")) {

        // Instantiate and populate members
        final ConceptRefsetMember member = new ConceptRefsetMemberJpa();
        setCommonFields(member, refset);
        member.setConceptActive(true);
        member.setRefset(refset);
        member.setConceptId(fields[1]);
        member.setEffectiveTime(null);

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
  public List<DefinitionClause> importDefinition(Refset refset,
    InputStream content) throws Exception {
    throw new LocalException(
        "This handler only supports importing of members.");
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
  }
}
