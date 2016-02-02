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
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.jpa.DefinitionClauseJpa;
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
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      // Check field lengths
      // Support any RF2 refset file with 6 or more fields.
      if (fields.length < 6) {
        pbr.close();
        Logger.getLogger(getClass()).error("line = " + line);
        throw new LocalException(
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
        member.setConceptActive(true);
        member.setRefset(refset);
        member.setConceptId(fields[5]);
        if (!fields[1].equals("")) {
          try{
          member.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(fields[1]));
          } catch (Exception e) {
            throw new LocalException("Unable to parse date, expecting format YYYYMMDD - " +fields[1]);
          }
        }

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
    Logger.getLogger(getClass()).info("Import refset definition");

    String line = "";
    List<DefinitionClause> definitionClauses = new ArrayList<>();

    // Read from input stream
    Reader reader = new InputStreamReader(content, "UTF-8");
    PushBackReader pbr = new PushBackReader(reader);
    while ((line = pbr.readLine()) != null) {

      // Strip \r chars and split line
      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line,"\t");

      // Check fields
      if (fields.length != 7) {
        pbr.close();
        throw new LocalException("Unexpected field count in refset definition file.");
      }

      // skip header
      if (!fields[0].equals("id")) {

        Logger.getLogger(getClass()).debug("  definition = " + fields[6]);

        // parse into definition clauses
        String part1 = "";
        String part2 = "";
        if (fields[6].contains(" + !")) {
          part1 = fields[6].substring(0, fields[6].indexOf(" + !"));
          part2 = fields[6].substring(fields[6].indexOf(" + !") + 4);
        } else {
          part1 = fields[6];
        }
        String[] positiveClauses = part1.split(" UNION ");
        for (String clause : positiveClauses) {
          DefinitionClause defClause = new DefinitionClauseJpa();
          defClause.setNegated(false);
          defClause.setValue(clause);
          definitionClauses.add(defClause);
        }
        String[] negativeClauses = part2.split(" + !");
        for (String clause : negativeClauses) {
          // Skip the empty clause (i.e. if there are no clauses)
          if (clause.equals("")) {
            continue;
          }
          // Skip project exclusion clause
          if (refset.getProject().getExclusionClause().equals(clause)) {
            continue;
          }
          DefinitionClause defClause = new DefinitionClauseJpa();
          defClause.setNegated(true);
          defClause.setValue(clause);
          definitionClauses.add(defClause);
        }

      }
    }
    pbr.close();
    return definitionClauses;
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
