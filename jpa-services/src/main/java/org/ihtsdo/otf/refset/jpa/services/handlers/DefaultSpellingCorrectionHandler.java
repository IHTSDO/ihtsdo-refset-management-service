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
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
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

  @Override
  public ByteArrayInputStream getEntriesAsStream(List<String> entries)
    throws UnsupportedEncodingException {
    StringBuilder sb = new StringBuilder();

    for (String s : entries) {
      sb.append(s);
      sb.append("\n");
    }

    return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
  }

  @Override
  public List<String> getEntriesAsList(InputStream is) throws Exception {
    String line;
    List<String> entries = new ArrayList<>();

    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    while ((line = reader.readLine()) != null) {
      entries.add(line);
    }

    return entries;
  }

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

      return convertResults(results);
    }

    return new StringList();
  }

  private StringList convertResults(String[] results) {

    StringList retStrList = new StringList();
    retStrList.setTotalCount(results.length);
    retStrList.setObjects(Arrays.asList(results));

    return retStrList;
  }

  @Override
  public SpellingCorrectionHandler copy() throws Exception {
    // Nothing to do
    return new DefaultSpellingCorrectionHandler();
  }
}
