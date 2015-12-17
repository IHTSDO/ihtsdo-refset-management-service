/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.ihtsdo.otf.refset.helpers.KeyValuesMap;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.services.handlers.SpellingCorrectionHandler;

/**
 * Default implementation of {@link SpellingCorrectionHandler}. Leverages the
 * IHTSDO SpellingCorrection server to the extent possible for interacting with
 * SpellingCorrection components. Uses local storage where not possible.
 */
public class DefaultSpellingCorrectionHandler extends RootServiceJpa implements
    SpellingCorrectionHandler {
  /**
   * Instantiates an empty {@link DefaultSpellingCorrectionHandler}.
   *
   * @throws Exception the exception
   */
  public DefaultSpellingCorrectionHandler() throws Exception {
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // N/A
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Default SpellingCorrection handler";
  }

  /* see superclass */
  @Override
  public ByteArrayInputStream getEntriesAsStream(List<String> entries)
    throws UnsupportedEncodingException {
    StringBuilder sb = new StringBuilder();

    for (String s : entries) {
      sb.append(s);
      sb.append(System.getProperty("line.separator"));
    }

    return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
  }

  /* see superclass */
  @Override
  public List<String> getEntriesAsList(InputStream is) throws Exception {
    String line;
    List<String> entries = new ArrayList<>();

    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    while ((line = reader.readLine()) != null) {
      line = line.replace("\r", "");
      // If the line contains any whitespace, reject the format
      if (line.matches("\\s")) {
        throw new Exception(
            "Badly formatted spelling file, no whitespace allowed, words only.");
      }
      entries.add(line);
    }

    return entries;
  }

  /* see superclass */
  @Override
  public StringList suggestSpelling(String term, List<String> entries, int amt,
    Long tid) throws IOException {
    if (!entries.contains(term)) {
      File tmpIndexFile =
          new File(Long.toString(System.currentTimeMillis()),
              Long.toString(tid));

      StringBuilder builder = new StringBuilder();
      for (String s : entries) {
        builder.append(s);
        builder.append("\n");
      }

      InputStream is =
          new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));
      PlainTextDictionary ptDict = new PlainTextDictionary(is);

      IndexWriterConfig iwConfig =
          new IndexWriterConfig(Version.LATEST, new StandardAnalyzer());
      FSDirectory indexDir = FSDirectory.open(tmpIndexFile);
      SpellChecker checker = new SpellChecker(indexDir);

      checker.indexDictionary(ptDict, iwConfig, true);
      tmpIndexFile.delete();

      String[] results = checker.suggestSimilar(term, amt);
      checker.close();
      return convertResults(results);
    }

    return new StringList();
  }

  @Override
  public KeyValuesMap suggestBatchSpelling(StringList lookupTerms,
    List<String> dictionaryEntries, int amt, Long tid) throws IOException {
    KeyValuesMap retMap = new KeyValuesMap();

    // Only process if lookupTerms need suggestions, so idenify if case
    boolean termsToProcess = false;
    for (String termToSearch : lookupTerms.getObjects()) {
      if (!dictionaryEntries.contains(termToSearch)) {
        termsToProcess = true;
        break;
      }
    }

    if (termsToProcess) {
      HashMap<String, StringList> resultHashMap = new HashMap<>();

      // Create input stream for Dictionary
      StringBuilder builder = new StringBuilder();
      for (String s : dictionaryEntries) {
        builder.append(s);
        builder.append("\n");
      }

      InputStream is =
          new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));

      // Create Index Writer
      PlainTextDictionary ptDict = new PlainTextDictionary(is);
      IndexWriterConfig iwConfig =
          new IndexWriterConfig(Version.LATEST, new StandardAnalyzer());

      // Create tmp file for SpellChecker
      File tmpIndexFile =
          new File(Long.toString(System.currentTimeMillis()),
              Long.toString(tid));
      FSDirectory indexDir = FSDirectory.open(tmpIndexFile);

      // Create SpellChecker
      SpellChecker checker = new SpellChecker(indexDir);

      // Index Checker and delete tmp file
      checker.indexDictionary(ptDict, iwConfig, true);
      tmpIndexFile.delete();

      // Iterate through lookup terms and store their collections in map
      for (String termToSearch : lookupTerms.getObjects()) {
        if (!dictionaryEntries.contains(termToSearch)) {
          String[] results = checker.suggestSimilar(termToSearch, amt);
          StringList resultsForTerm = convertResults(results);

          resultHashMap.put(termToSearch, resultsForTerm);
        }
      }

      checker.close();

      // Convert Map to proper return type
      retMap.setMap(resultHashMap);
    }

    return retMap;
  }

  /**
   * Convert results.
   *
   * @param results the results
   * @return the string list
   */
  @SuppressWarnings("static-method")
  private StringList convertResults(String[] results) {

    StringList retStrList = new StringList();
    retStrList.setTotalCount(results.length);
    retStrList.setObjects(Arrays.asList(results));

    return retStrList;
  }

  /* see superclass */
  @Override
  public SpellingCorrectionHandler copy() throws Exception {
    // Nothing to do
    return new DefaultSpellingCorrectionHandler();
  }
}
