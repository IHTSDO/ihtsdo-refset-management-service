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
public class ImportRefsetRf2DeltaHandler implements ImportRefsetHandler {

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** counter for objects created, reset in each load section. */
  int objectCt; //

  /** The id. */
  final String id = "id";

  /** The validation result. */
  ValidationResult validationResult = new ValidationResultJpa();

  /**
   * Instantiates an empty {@link ImportRefsetRf2Handler}.
   * @throws Exception if anything goes wrong
   */
  public ImportRefsetRf2DeltaHandler() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public boolean isDeltaHandler() {
    return true;
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
    return "Import RF2 Delta";
  }

  /* see superclass */
  @Override
  public List<ConceptRefsetMember> importMembers(Refset refset,
    InputStream content) throws Exception {
    Logger.getLogger(getClass()).info("Import refset members ");

    // initialize
    validationResult = new ValidationResultJpa();

    /** The inactive ct. */
    int inactiveCt = 0;
    int addedCt = 0;

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

        // Mark inactive
        if (fields[2].equals("0")) {
          inactiveCt++;
        }

        // Instantiate and populate members
        final ConceptRefsetMember member = new ConceptRefsetMemberJpa();

        // Look for a module id change
        if (!refset.getModuleId().equals(fields[3])) {
          pbr.close();
          throw new LocalException(
              "Module id has changed, make sure to update the refset module id first - "
                  + fields[3]);
        }

        // Look for a refset id change
        if (!refset.getTerminologyId().equals(fields[4])) {
          pbr.close();
          throw new LocalException(
              "Refset id has changed, must create a new refset for this delta - "
                  + fields[4]);
        }

        setCommonFields(member, refset);
        // Set the active field properly
        member.setActive(fields[2].equals("1"));

        // save original ids if refset matches
        member.setTerminologyId(fields[0]);
        member.setConceptActive(true);
        member.setRefset(refset);
        String conceptId = fields[5].trim();
        if (!conceptId.equals(fields[5])) {
        	pbr.close();
        	throw new LocalException("Unexpected white space padding the concept id *"
                + fields[5] + "*");
        }
        member.setConceptId(conceptId);
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
        addedCt++;
        Logger.getLogger(getClass()).debug("  member = " + member);
      }
    }

    if (addedCt == 1) {
      validationResult
          .addComment("1 new member successfully loaded or updatd.");
    } else {
      validationResult.addComment(addedCt
          + " new members successfully loaded or updated.");
    }

    if (inactiveCt == 1) {
      validationResult.addWarning("1 member was retired.");
    } else if (inactiveCt != 0) {
      validationResult.addWarning(inactiveCt + " members were retired.");
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
