/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.handlers.DefaultSpellingCorrectionHandler;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.ihtsdo.otf.refset.services.handlers.SpellingCorrectionHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Some initial testing for {@link DefaultSpellingCorrectionHandler}. Assumes
 * stock dev load.
 */
public class DefaultSpellingCorrectionHandlerTest extends JpaSupport {

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {
    // do nothing
  }

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    // n/a
  }

  /**
   * Testing results if term not in suggestion list.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSuggestSpellingNoMatch() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    ProjectService service = new ProjectServiceJpa();
    SpellingCorrectionHandler handler = service.getSpellingCorrectionHandler();

    List<String> origVals = new ArrayList<String>();
    origVals.add("Word1");
    origVals.add("Word2");
    origVals.add("Word3");
    StringList results =
        handler.suggestSpelling("Word", origVals, 10, new Long(1));

    assertEquals(3, results.getTotalCount());
    assertTrue(results.getObjects().contains("Word1"));
    assertTrue(results.getObjects().contains("Word2"));
    assertTrue(results.getObjects().contains("Word3"));

    service.close();
  }

  /**
   * Testing results if term is in suggestion list.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSuggestSpellingWithMatch() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    ProjectService service = new ProjectServiceJpa();
    SpellingCorrectionHandler handler = service.getSpellingCorrectionHandler();

    List<String> origVals = new ArrayList<String>();
    origVals.add("Word-A");
    origVals.add("Word-B");
    origVals.add("Word-C");
    StringList results =
        handler.suggestSpelling("Word-A", origVals, 10, new Long(1));

    assertEquals(results.getTotalCount(), 0);

    service.close();
  }

  /**
   * Testing the ability to create an index from a stream.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetEntriesAsStreamWithValues() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    ProjectService service = new ProjectServiceJpa();
    SpellingCorrectionHandler handler = service.getSpellingCorrectionHandler();

    // Setup
    List<String> origVals = new ArrayList<String>();
    origVals.add("Word-A");
    origVals.add("Word-B");

    InputStream convertedStream = handler.getEntriesAsStream(origVals);

    // Convert
    try {
      StringBuilder builder = new StringBuilder();
      for (String s : origVals) {
        builder.append(s);
        builder.append("\n");

      }

      InputStream origStream =
          new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));
      origStream.close();

      String origLine;
      String convertedLine;
      BufferedReader origReader =
          new BufferedReader(new InputStreamReader(origStream));
      BufferedReader convertedReader =
          new BufferedReader(new InputStreamReader(convertedStream));

      for (int i = 0; i < origVals.size(); i++) {
        origLine = origReader.readLine();
        convertedLine = convertedReader.readLine();

        // Assert
        assertEquals(origLine, convertedLine);
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    service.close();
  }

  /**
   * Testing the ability to create an index from a stream.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetEntriesAsStreamNoValues() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    ProjectService service = new ProjectServiceJpa();
    SpellingCorrectionHandler handler = service.getSpellingCorrectionHandler();

    // Setup
    List<String> origVals = new ArrayList<String>();
    InputStream convertedStream = handler.getEntriesAsStream(origVals);

    // Convert
    try {
      StringBuilder builder = new StringBuilder();
      for (String s : origVals) {
        builder.append(s);
        builder.append("\n");

      }

      InputStream origStream =
          new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));
      origStream.close();

      String origLine;
      String convertedLine;
      BufferedReader origReader =
          new BufferedReader(new InputStreamReader(origStream));
      BufferedReader convertedReader =
          new BufferedReader(new InputStreamReader(convertedStream));

      for (int i = 0; i < origVals.size(); i++) {
        origLine = origReader.readLine();
        convertedLine = convertedReader.readLine();

        // Assert
        assertEquals(origLine, convertedLine);
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    service.close();
  }

  /**
   * Testing the ability to create an index from a stream.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetEntriesAsListWithValues() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    ProjectService service = new ProjectServiceJpa();
    SpellingCorrectionHandler handler = service.getSpellingCorrectionHandler();

    // Setup
    List<String> origVals = new ArrayList<String>();
    origVals.add("Word-A");
    origVals.add("Word-B");

    StringBuilder builder = new StringBuilder();
    for (String s : origVals) {
      builder.append(s);
      builder.append("\n");

    }

    // Convert
    List<String> convertedVals = null;
    try {
      InputStream in =
          new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));
      in.close();
      convertedVals = handler.getEntriesAsList(in);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    // Assert
    assertEquals(origVals, convertedVals);
    service.close();
  }

  /**
   * Testing the ability to create an index from a stream.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetEntriesAsListNoValues() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    ProjectService service = new ProjectServiceJpa();
    SpellingCorrectionHandler handler = service.getSpellingCorrectionHandler();

    // Setup
    List<String> origVals = new ArrayList<String>();

    StringBuilder builder = new StringBuilder();
    for (String s : origVals) {
      builder.append(s);
      builder.append("\n");

    }

    // Convert
    List<String> convertedVals = null;
    try {
      InputStream in =
          new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));
      in.close();
      convertedVals = handler.getEntriesAsList(in);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    // Assert
    assertEquals(origVals, convertedVals);
    service.close();
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // n/a
  }

  /**
   * Teardown class.
   *
   * @throws Exception the exception
   */
  @AfterClass
  public static void teardownClass() throws Exception {
    // do nothing
  }
}
