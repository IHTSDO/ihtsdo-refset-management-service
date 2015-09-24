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
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.rf2.RefsetDefinitionRefsetMember;
import org.ihtsdo.otf.refset.rf2.SimpleRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.RefsetDefinitionRefsetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.SimpleRefsetMemberJpa;
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
  public String getName() {
    return "Import RF2";
  }

  /* see superclass */
  @Override
  public List<SimpleRefsetMember> importMembers(InputStream content)
    throws Exception {
    Logger.getLogger(getClass()).info("Import refset members ");

    // Read lines of RF2 from the input stream
    // - skip header
    // - create SimpleRefsetMember objects (like RF2 snapshot loader)\
    // - put into list
    // return list

    // FAIL if
    // the format of the line is wrong (e.g. unexpected number of fields)

    List<SimpleRefsetMember> list = new ArrayList<>();
    String line = "";

    Reader reader = new InputStreamReader(content, "UTF-8");
    PushBackReader pbr = new PushBackReader(reader);
    while ((line = pbr.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = line.split("\t");

      if (fields.length != 6) {
        pbr.close();
        throw new Exception(
            "Unexpected field count in simple refset member file.");
      }

      if (!fields[0].equals("id")) { // header

        final SimpleRefsetMember member = new SimpleRefsetMemberJpa();
        // Universal Refset attributes
        member.setTerminologyId(fields[0]);
        member.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(fields[1]));
        member.setLastModified(ConfigUtility.DATE_FORMAT.parse(fields[1]));
        member.setActive(fields[2].equals("1"));
        member.setModuleId(fields[3].intern());
        member.setRefsetId(fields[4]);

        // SimpleRefsetMember unique attributes
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
    // Read lines of RF2 from the input stream
    // - skip header
    // - Assume there are 7 fields, save value of the last field
    // TODO: - verify there is only one line besides the header
    // Return the value of that 7th field.
    String line = "";

    Reader reader = new InputStreamReader(content, "UTF-8");
    PushBackReader pbr = new PushBackReader(reader);
    while ((line = pbr.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = line.split("\t");

      if (fields.length != 7) {
        pbr.close();
        throw new Exception("Unexpected field count in refset definition file.");
      }

      if (!fields[0].equals("id")) { // header

        final RefsetDefinitionRefsetMember member =
            new RefsetDefinitionRefsetMemberJpa();
        member.setTerminologyId(fields[0]);
        member.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(fields[1]));
        member.setLastModified(ConfigUtility.DATE_FORMAT.parse(fields[1]));
        member.setActive(fields[2].equals("1"));
        member.setModuleId(fields[3].intern());
        member.setRefsetId(fields[4]);

        // Refset descriptor unique attributes
        member.setDefinition(fields[6]);

        pbr.close();
        Logger.getLogger(getClass()).debug(
            "  definition = " + member.getDefinition());
        return member.getDefinition();
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
}
