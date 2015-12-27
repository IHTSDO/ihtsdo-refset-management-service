/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.helpers.KeyValuesMap;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.MemoryEntryJpa;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.handlers.PhraseMemoryHandler;
import org.ihtsdo.otf.refset.services.helpers.PushBackReader;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Default implementation of {@link PhraseMemoryHandler}.
 */
public class DefaultPhraseMemoryHandler extends RootServiceJpa implements
    PhraseMemoryHandler {
  /**
   * Instantiates an empty {@link DefaultPhraseMemoryHandler}.
   *
   * @throws Exception the exception
   */
  public DefaultPhraseMemoryHandler() throws Exception {
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // N/A
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Default PhraseMemory handler";
  }

  /* see superclass */
  @Override
  public ByteArrayInputStream getEntriesAsStream(List<MemoryEntry> entries)
    throws UnsupportedEncodingException {
    final StringBuilder sb = new StringBuilder();
    for (final MemoryEntry entry : entries) {
      sb.append(entry.getName()).append("|").append(entry.getTranslatedName())
          .append(System.getProperty("line.separator"));
    }

    return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
  }

  /* see superclass */
  @Override
  public List<MemoryEntry> getEntriesAsList(InputStream content)
    throws Exception {
    final List<MemoryEntry> list = new ArrayList<>();
    String line = "";
    final Reader reader = new InputStreamReader(content, "UTF-8");
    final PushBackReader pbr = new PushBackReader(reader);
    while ((line = pbr.readLine()) != null) {

      // Strip \r and split lines
      line = line.replace("\r", "");
      final String fields[] = line.split("\\|");

      // Check field lengths
      if (fields.length != 2) {
        pbr.close();
        Logger.getLogger(getClass()).error("line = " + line);
        throw new Exception("Unexpected field count in phrase memory file "
            + fields.length);
      }

      // Instantiate and populate members
      final MemoryEntry member = new MemoryEntryJpa();
      member.setName(fields[0]);
      member.setTranslatedName(fields[1]);
      // Add member
      list.add(member);
      Logger.getLogger(getClass()).debug("  phrasememory = " + member);
    }
    pbr.close();
    return list;
  }

  /* see superclass */
  @Override
  public StringList suggestPhraseMemory(String name, Long translationId,
    TranslationService translationService) throws Exception {
    final String query = "name:" + name;
    final StringList strList = new StringList();
    List<MemoryEntry> entries;
    entries =
        translationService.findMemoryEntryForTranslation(translationId, query,
            null);
    final List<String> results =
        Lists.transform(entries, new Function<MemoryEntry, String>() {

          @Override
          public String apply(MemoryEntry arg0) {
            return arg0.getTranslatedName();
          }

        });
    strList.setTotalCount(results.size());
    strList.setObjects(results);
    return strList;
  }

  @Override
  public KeyValuesMap suggestBatchPhraseMemory(StringList lookupTerms,
    Long translationId, TranslationService translationService)
    throws IOException {
    KeyValuesMap retMap = new KeyValuesMap();

    return retMap;
  }

  /* see superclass */
  @Override
  public PhraseMemoryHandler copy() throws Exception {
    // Nothing to do
    return new DefaultPhraseMemoryHandler();
  }
}
