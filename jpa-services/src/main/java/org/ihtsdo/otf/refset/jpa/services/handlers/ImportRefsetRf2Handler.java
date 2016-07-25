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
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.jpa.DefinitionClauseJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
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

  /** The validation result. */
  ValidationResult validationResult = new ValidationResultJpa();

  /** The ct. */
  private int ct = 0;

  /** The inactive ct. */
  private int inactiveCt = 0;

  /**
   * Instantiates an empty {@link ImportRefsetRf2DeltaHandler}.
   * @throws Exception if anything goes wrong
   */
  public ImportRefsetRf2Handler() throws Exception {
    super();

  }

  /* see superclass */
  @Override
  public boolean isDeltaHandler() {
    return false;
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

    // initialize
    validationResult = new ValidationResultJpa();
    ct = 0;
    inactiveCt = 0;

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
          inactiveCt++;
          continue;
        }

        // Instantiate and populate members
        final ConceptRefsetMember member = new ConceptRefsetMemberJpa();
        setCommonFields(member, refset);
        // save original ids if refset matches
        if (refset.getTerminologyId().equals(fields[4])) {
          member.setTerminologyId(fields[0]);
        }
        member.setConceptActive(true);
        member.setRefset(refset);
        member.setConceptId(fields[5]);
        if (!fields[1].equals("")) {
          try {
            member.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(fields[1]));
          } catch (Exception e) {
            pbr.close();
            throw new LocalException(
                "Unable to parse date, expecting format YYYYMMDD - "
                    + fields[1]);
          }
        }

        // Add member
        list.add(member);
        ct++;
        Logger.getLogger(getClass()).debug("  member = " + member);
      }
    }
    if (ct == 1) {
      validationResult.addComment("1 member successfully loaded.");
    } else {
      validationResult.addComment(ct + " members successfully loaded.");
    }
    if (inactiveCt == 1) {
      validationResult.addWarning("1 inactive member was skipped.");
    } else if (inactiveCt != 0) {
      validationResult.addWarning(inactiveCt
          + " inactive members were skipped.");
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
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      // Check fields
      if (fields.length != 7) {
        pbr.close();
        throw new LocalException(
            "Unexpected field count in refset definition file.");
      }

      // skip header
      if (!fields[0].equals("id")) {

        Logger.getLogger(getClass()).debug("  definition = " + fields[6]);

        // parse into definition clauses
        String part1 = "";
        String part2 = "";
        if (fields[6].contains(" MINUS ")) {
          part1 = fields[6].substring(0, fields[6].indexOf(" MINUS "));
          part2 = fields[6].substring(fields[6].indexOf(" MINUS ") + 6);
        } else {
          part1 = fields[6];
        }

        String[] positiveClauses = part1.split(" OR ");
        for (String clause : positiveClauses) {
          DefinitionClause defClause = new DefinitionClauseJpa();
          defClause.setNegated(false);
          defClause.setValue(trimClause(clause));
          definitionClauses.add(defClause);
        }

        String[] negativeClauses = part2.split(" OR ");
        for (String clause : negativeClauses) {
          // Skip the empty clause (i.e. if there are no clauses)
          if (clause.equals("")) {
            continue;
          }
          // Skip project exclusion clause
          if (trimClause(clause).equals(
              refset.getProject().getExclusionClause())) {
            continue;
          }

          DefinitionClause defClause = new DefinitionClauseJpa();
          defClause.setNegated(true);
          defClause.setValue(trimClause(clause));
          definitionClauses.add(defClause);
        }
      }
    }
    pbr.close();
    return definitionClauses;
  }

  /**
   * Trim clause.
   *
   * @param clause the clause
   * @return the string
   */
  @SuppressWarnings("static-method")
  private String trimClause(String clause) {
    // Strip leading parens
    String retval =
        clause.replaceFirst("^\\s*\\(\\s*", "")
            .replaceFirst("^\\s*\\(\\s*", "").replaceFirst("^\\s*\\(\\s*", "");
    // Strip trailing parens
    retval =
        retval.replaceFirst("\\s*\\)\\s*$", "")
            .replaceFirst("\\s*\\)\\s*$", "").replaceFirst("\\s*\\)\\s*$", "");
    return retval.trim();
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

  @Override
  public ValidationResult getValidationResults() {
    return validationResult;
  }
}
