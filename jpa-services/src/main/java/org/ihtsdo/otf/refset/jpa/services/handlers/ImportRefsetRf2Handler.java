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

        final Concept concept = new ConceptJpa();
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

        final RefSetDefinitionRefSetMember member =
            new RefSetDefinitionRefSetMemberJpa();
        member.setTerminologyId(fields[0]);
        member.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(fields[1]));
        member.setLastModified(ConfigUtility.DATE_FORMAT.parse(fields[1]));
        member.setActive(fields[2].equals("1"));
        member.setModuleId(fields[3].intern());
        member.setRefSetId(fields[4]);
        final Concept concept = new ConceptJpa();
        concept.setTerminologyId(fields[5]);
        member.setConcept(concept);

        // Refset descriptor unique attributes
        member.setDefinition(fields[6]);
        
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

  @Override
  public void close() throws Exception {
    // TODO Auto-generated method stub
    
  }
  

}
