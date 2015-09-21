/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.algo.Algorithm;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.RefSetDefinitionRefSetMember;
import org.ihtsdo.otf.refset.rf2.SimpleRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.RefSetDefinitionRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.SimpleRefSetMemberJpa;
import org.ihtsdo.otf.refset.services.handlers.ImportRefsetHandler;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;
import org.ihtsdo.otf.refset.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import a refset definition.
 */
public class ImportRefsetRf2Handler  implements
    ImportRefsetHandler, Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  boolean requestCancel = false;
  
  /**  The release version. */
  private String releaseVersion = "";
  
  /** The release version date. */
  private Date releaseVersionDate;
  
  /**  The terminology. */
  private String terminology = "";
  
  /** The terminology version. */
  private String terminologyVersion;

  /** counter for objects created, reset in each load section. */
  int objectCt; //

  /** The init pref name. */
  final String initPrefName = "No default preferred name found";

  /** The loader. */
  final String loader = "loader";

  /** The id. */
  final String id = "id";

  /** The published. */
  final String published = "PUBLISHED";

  /**
   * Instantiates an empty {@link ImportRefsetRf2Handler}.
   * @throws Exception if anything goes wrong
   */
  public ImportRefsetRf2Handler() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Import RF2";
  }

  /* see superclass */
  @Override
  public List<SimpleRefSetMember> importMembers(InputStream content)
    throws Exception {
    // Read lines of RF2 from the input stream
    //    - skip header
    //    - create SimpleRefsetMember objects (like RF2 snapshot loader)\
    //    - put into list
    // return list
    
    // FAIL if
    // the format of the line is wrong (e.g. unexpected number of fields)
    
    List<SimpleRefSetMember> list = new ArrayList<>();
    String line = "";
    
    Reader reader = new InputStreamReader(content, "UTF-8");
    PushBackReader pbr = new PushBackReader(reader);
    while ((line = pbr.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = line.split("\t");
      
      if (fields.length != 6) {
        pbr.close();
        throw new Exception("Unexpected field count in simple refset member file.");
      }
        
      if (!fields[0].equals("id")) { // header

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
          pbr.push(line);
          break;
        }

        final SimpleRefSetMember member = new SimpleRefSetMemberJpa();
        // Universal RefSet attributes
        member.setTerminologyId(fields[0]);
        member.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(fields[1]));
        member.setLastModified(ConfigUtility.DATE_FORMAT.parse(fields[1]));
        member.setActive(fields[2].equals("1"));
        member.setModuleId(fields[3].intern());
        member.setRefSetId(fields[4]);

        // SimpleRefSetMember unique attributes
        // NONE

        // Terminology attributes
        member.setTerminology(terminology);
        member.setVersion(terminologyVersion);  // TODO this is the refset version / not terminology version correct?
        member.setLastModified(releaseVersionDate);
        member.setLastModifiedBy(loader);
        member.setPublished(true);

        final Concept concept = new ConceptJpa();
        concept.setTerminology(terminology);
        concept.setVersion(terminologyVersion);
        concept.setTerminologyId(fields[5]);

        member.setConcept(concept);
        // Add member
        list.add(member);
      }      
    }
    pbr.close();
    return list;
  }

  /* see superclass */
  @Override
  public String importDefinition(InputStream content) throws Exception {
    // Read lines of RF2 from the input stream
    //    - skip header
    //    - Assume there are 7  fields, save value of the last field
    //    TODO: - verify there is only one line besides the header
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

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
          pbr.push(line);
          break;
        }

        final RefSetDefinitionRefSetMember member =
            new RefSetDefinitionRefSetMemberJpa();
        member.setTerminologyId(fields[0]);
        member.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(fields[1]));
        member.setLastModified(ConfigUtility.DATE_FORMAT.parse(fields[1]));
        member.setActive(fields[2].equals("1"));
        member.setModuleId(fields[3].intern());
        member.setRefSetId(fields[4]);
        final Concept concept = new ConceptJpa();
        concept.setTerminology(terminology);
        concept.setVersion(terminologyVersion);
        concept.setTerminologyId(fields[5]);
        member.setConcept(concept);

        // Refset descriptor unique attributes
        member.setDefinition(fields[6]);

        // Terminology attributes
        member.setTerminology(terminology);
        member.setVersion(terminologyVersion);
        member.setLastModified(releaseVersionDate);
        member.setLastModifiedBy(loader);
        member.setPublished(true);   
        
        pbr.close();
        return member.getDefinition();
      }
    }
    pbr.close();
    return null;
  }


  
  /* see superclass */
  @Override
  public void compute() throws Exception {

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
  public void setProperties(Properties p) throws Exception {
    // n/a

  }

  /**
   * Sets the terminology version.
   *
   * @param terminologyVersion the terminology version
   */
  public void setTerminologyVersion(String terminologyVersion) {
    this.terminologyVersion = terminologyVersion;
  }
  
  /**
   * Sets the release version.
   *
   * @param releaseVersion the release version
   */
  public void setReleaseVersion(String releaseVersion) {
    this.releaseVersion = releaseVersion;
    try {
      this.releaseVersionDate = ConfigUtility.DATE_FORMAT.parse(releaseVersion);
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  @Override
  public void close() throws Exception {
    // TODO Auto-generated method stub
    
  }
  

}
