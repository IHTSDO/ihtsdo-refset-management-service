/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.services.handlers.SpellingCorrectionHandler;

/**
 * Default implementation of {@link SpellingCorrectionHandler}. Leverages the
 * IHTSDO SpellingCorrection server to the extent possible for interacting with
 * SpellingCorrection components. Uses local storage where not possible.
 */
public class DefaultSpellingCorrectionHandler implements
    SpellingCorrectionHandler {

  /** The spell checker. */
  private SpellChecker checker;

  /**
   * Instantiates an empty {@link DefaultSpellingCorrectionHandler}.
   *
   * @throws Exception the exception
   */
  public DefaultSpellingCorrectionHandler() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // N/A
  }

  /* see superclass */
  @Override
  public void setTranslation(Translation translation) throws Exception {

    String dir =
        ConfigUtility.getConfigProperties().getProperty(
            "hibernate.search.default.indexBase");
    if (dir == null) {
      throw new Exception(
          "Index directory hibernate.search.default.indexBase not set in config.properties");
    }
    // e.g. $dir/spelling/$translationId
    File indexDir =
        new File(new File(dir, "spelling"), translation.getId().toString());

    // Load the dictionary

    // Create the index writer
    FSDirectory indexFsDir = FSDirectory.open(indexDir);
    checker = new SpellChecker(indexFsDir);
    // Presumably not needed - index already built, just opening it
    // reindex(translation.getSpellingDictionary().getEntries(),true);
    checker.setAccuracy(.5f);
    checker.setStringDistance(new LevensteinDistance());

  }

  /* see superclass */
  @Override
  public void reindex(List<String> entries, boolean merge) throws Exception {
    if (checker == null) {
      throw new Exception(
          "Set translation must be called prior to calling reindex");
    }
    IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LATEST,
    // lowercase keyword analyzer
        new Analyzer() {
          @Override
          protected TokenStreamComponents createComponents(String fieldName,
            Reader reader) {
            Tokenizer source = new KeywordTokenizer(reader);
            TokenStream filter = new LowerCaseFilter(source);
            return new TokenStreamComponents(source, filter);
          }
        });
    StringBuilder builder = new StringBuilder();
    for (String s : entries) {
      builder.append(s);
      builder.append("\n");
    }
    InputStream is =
        new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));
    PlainTextDictionary ptDict = new PlainTextDictionary(is);
    if (!merge) {
      checker.clearIndex();
    }
    checker.indexDictionary(ptDict, iwConfig, false);
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
      // bad format
      if (line.length() > 500) {
        throw new LocalException(
            "Line is too long, > 500 chars, likely bad format.");
      }
      line = line.replace("\r", "");
      // If the line contains any whitespace, reject the format
      if (line.matches("\\s")) {
        throw new LocalException(
            "Badly formatted spelling file, no whitespace allowed, words only.");
      }
      entries.add(line);
    }

    return entries;
  }

  /* see superclass */
  @Override
  public StringList suggestSpelling(String term, int amt) throws Exception {
    if (checker == null) {
      throw new LocalException(
          "Set translation must be called prior to calling suggest spelling");
    }
    // Assume terms of length 1 or 2 always exist
    if (!checker.exist(term) && term.length() > 2) {
      String[] results = checker.suggestSimilar(term, amt);

      // Handle the case of no suggestions, determine whether it exists
      return convertResults(results);
    }

    return new StringList();
  }

  @Override
  public boolean exists(String term) throws Exception {
    return checker.exist(term);
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

}
