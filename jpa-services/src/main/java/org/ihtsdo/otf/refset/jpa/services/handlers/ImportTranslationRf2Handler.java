/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.algo.Algorithm;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefSetMemberJpa;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to import a refset definition.
 */
public class ImportTranslationRf2Handler extends RootServiceJpa implements
    ImportTranslationHandler, Algorithm {

  /** The language ref set members. */
  Map<String, Set<LanguageRefSetMember>> languageRefSetMembers =
      new HashMap<>();
  
  /** The descriptions. */
  Map<String, Description> descriptions =
      new HashMap<>();
  
  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();
  
  /** hash sets for retrieving concepts. */
  private Map<String, Concept> conceptCache = new HashMap<>();

  /** The request cancel flag. */
  boolean requestCancel = false;
 
  /** The id. */
  final String id = "id";



  /**
   * Instantiates an empty {@link ImportTranslationRf2Handler}.
   * @throws Exception if anything goes wrong
   */
  public ImportTranslationRf2Handler() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Import RF2";
  }

  @Override
  public List<Concept> importTranslation(InputStream content) throws Exception {
    // Read in a .zip file containing descriptions and language files
    // - find the ZipEntry corresponding to the descriptions file
    // - find the ZipEntry corresponding to the languages file
  
    ZipInputStream zin = new ZipInputStream(content);

    String line;
    for (ZipEntry zipEntry; (zipEntry = zin.getNextEntry()) != null;) {
      Logger.getLogger(getClass()).debug("Import translation Rf2 handler - reading zipEntry " + zipEntry.getName());
      if (zipEntry.getName().contains("sct2_Description")) {
        
        if (descriptions.size() != 0) 
          throw new Exception("More than one description file in translation import zip file.");
         
        Scanner sc = new Scanner(zin);
        while (sc.hasNextLine()) {
          line = sc.nextLine();
          final String fields[] = line.split("\t");
          if (!fields[0].equals(id)) {

            final Description description = new DescriptionJpa();
            description.setTerminologyId(fields[0]);
            description.setEffectiveTime(ConfigUtility.DATE_FORMAT
                .parse(fields[1]));
            description.setLastModified(ConfigUtility.DATE_FORMAT
                .parse(fields[1]));
            description.setActive(fields[2].equals("1"));
            description.setModuleId(fields[3].intern());

            // get concept if cached, otherwise create and add to cache
            Concept concept = null;
            if (conceptCache.containsKey(fields[4])) {
              concept = conceptCache.get(fields[4]);
              concept.addDescription(description);
            } else {
              concept = new ConceptJpa();
              concept.addDescription(description);
              concept.setTerminologyId(fields[4]);
              conceptCache.put(concept.getTerminologyId(), concept);
            }
            
            description.setLanguageCode(fields[5].intern());
            description.setTypeId(fields[6].intern());
            description.setTerm(fields[7]);
            description.setCaseSignificanceId(fields[8].intern());
            
            
            descriptions.put(description.getTerminologyId(), description);
            //concepts.add(description.getConcept());
          }
        }
      }
      
      else if (zipEntry.getName().contains("Refset_Language")) {

        Logger.getLogger(getClass()).debug("Import translation Rf2 handler - reading zipEntry " + zipEntry.getName());
        if (languageRefSetMembers.size() != 0)
          throw new Exception(
              "More than one language file in translation import zip file.");
        Scanner sc = new Scanner(zin);
        while (sc.hasNextLine()) {
          //System.out.println(sc.nextLine());
          line = sc.nextLine();
          line = line.replace("\r", "");
          final String fields[] = line.split("\t");

          if (fields.length != 7) {
            sc.close();
            throw new Exception("Unexpected field count in language file.");
          }

          if (!fields[0].equals(id)) { // header
 
            final LanguageRefSetMember member = new LanguageRefSetMemberJpa();

            // Universal RefSet attributes
            member.setTerminologyId(fields[0]);
            member.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(fields[1]));
            member.setLastModified(ConfigUtility.DATE_FORMAT.parse(fields[1]));
            member.setActive(fields[2].equals("1"));
            member.setModuleId(fields[3].intern());
            member.setRefSetId(fields[4].intern());
            member.setPublished(true);

            // Language unique attributes
            member.setAcceptabilityId(fields[6].intern());

            // Cache language refset members
            if (!languageRefSetMembers.containsKey(fields[5])) {
              languageRefSetMembers.put(fields[5],
                  new HashSet<LanguageRefSetMember>());
            }
            languageRefSetMembers.get(fields[5]).add(member);
          }
        } // end while
      }
    }  // zin close
    zin.close();
    
    if (languageRefSetMembers.size() == 0)
      throw new Exception(
          "Missing or empty language file.");
    if (descriptions.size() == 0)
      throw new Exception(
          "Missing or empty description file.");

    
    // Create description objects from RF2 (like from snaphsot loader)
    // Create concept objects for the descriptions (based on concept id field)
    // - make sure 2 descriptions with the same conceptId use the same concept
    // object
    // - attach descriptions to concepts
    // - for concepts set at least terminologyId, terminology, version
    // Create languages refset objects from RF2 (like from snapshot loader)
    // attach languages to descriptions
    // Verify that ALL languages connect to actual descriptions, FAIL if not

    for (Description description : descriptions.values()) {

      // Attach language refset members (if there are any)
      if (languageRefSetMembers.containsKey(description.getTerminologyId())) {
        for (LanguageRefSetMember member : languageRefSetMembers
            .get(description.getTerminologyId())) {
          member.setDescription(description);
          description.addLanguageRefSetMember(member);
        }
        // Remove used ones so we can keep track
        languageRefSetMembers.remove(description.getTerminologyId());
      } else {
        // Early SNOMED release have no languages
        throw new Exception(
                "  Description has no languages: "
                    + description.getTerminologyId());
      }

    }
    if (!languageRefSetMembers.isEmpty()) {
      throw new Exception("Language without description. " + languageRefSetMembers);
    }
    
    List<Concept> concepts = new ArrayList<>();
    for (Concept c : conceptCache.values()) {
      concepts.add(c);
    }
    return concepts;
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
