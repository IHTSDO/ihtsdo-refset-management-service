/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
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
	 * @throws Exception
	 *             the exception
	 */
	@BeforeClass
	public static void setupClass() throws Exception {
		// do nothing
	}

	/**
	 * Create test fixtures per test.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setup() throws Exception {
		// n/a
	}

	/**
	 * Testing results if term not in suggestion list.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testSuggestSpellingNoMatch() throws Exception {
		Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

		ProjectService service = new ProjectServiceJpa();
		SpellingCorrectionHandler handler = service
				.getSpellingCorrectionHandler();
		handler.setTranslationId(new Long(1));
		createTestList(handler);

		List<String> results = handler.suggestSpelling("Word", 10);

		assertTrue(results.size() == 3);
		assertTrue(results.contains("Word1"));
		assertTrue(results.contains("Word2"));
		assertTrue(results.contains("Word3"));

		service.close();
	}

	/**
	 * Testing results if term is in suggestion list.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testSuggestSpellingWithMatch() throws Exception {
		Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

		ProjectService service = new ProjectServiceJpa();
		SpellingCorrectionHandler handler = service
				.getSpellingCorrectionHandler();
		handler.setTranslationId(new Long(2));
		createTestList(handler);

		List<String> results = handler.suggestSpelling("Word1", 10);

		assertTrue(results.size() == 2);
		assertTrue(results.contains("Word2"));
		assertTrue(results.contains("Word3"));

		service.close();
	}

	/**
	 * Testing adding multiple terms into list then showing term not in
	 * suggestion list.
	 *
	 * @throws Exception
	 *             the exception
	 */

	@Test
	public void testSuggestBeforeAfterNewTermsNoMatch() throws Exception {
		Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
		ProjectService service = new ProjectServiceJpa();
		SpellingCorrectionHandler handler = service
				.getSpellingCorrectionHandler();
		handler.setTranslationId(new Long(3));
		createTestList(handler);

		List<String> results = handler.suggestSpelling("Word99", 10);
		assertTrue(results.size() == 3);
		assertTrue(results.contains("Word1"));
		assertTrue(results.contains("Word2"));
		assertTrue(results.contains("Word3"));
		assertTrue(!results.contains("Word99"));

		List<String> newTerms = new ArrayList<>();
		newTerms.add("Word96");
		newTerms.add("Word97");
		newTerms.add("Word98");
		handler.addEntries(newTerms);

		List<String> results2 = handler.suggestSpelling("Word99", 10);

		assertTrue(results2.size() == 6);
		assertTrue(results2.contains("Word1"));
		assertTrue(results2.contains("Word2"));
		assertTrue(results2.contains("Word3"));
		assertTrue(results2.contains("Word96"));
		assertTrue(results2.contains("Word97"));
		assertTrue(results2.contains("Word98"));

		service.close();
	}

	/**
	 * Testing adding single term into list then showing term is in suggestion
	 * list.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testSuggestBeforeAfterNewTermsWithMatch() throws Exception {
		Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
		ProjectService service = new ProjectServiceJpa();
		SpellingCorrectionHandler handler = service
				.getSpellingCorrectionHandler();
		handler.setTranslationId(new Long(4));
		createTestList(handler);

		List<String> results = handler.suggestSpelling("Word99", 10);

		assertTrue(results.size() == 3);
		assertTrue(results.contains("Word1"));
		assertTrue(results.contains("Word2"));
		assertTrue(results.contains("Word3"));
		assertTrue(!results.contains("Word99"));

		List<String> newTerms = new ArrayList<>();
		newTerms.add("Word99");
		handler.addEntries(newTerms);

		List<String> results2 = handler.suggestSpelling("Word99", 10);
		assertTrue(results2.size() == 3);
		assertTrue(results2.contains("Word1"));
		assertTrue(results2.contains("Word2"));
		assertTrue(results2.contains("Word3"));
		assertTrue(!results2.contains("Word99"));

		List<String> results3 = handler.suggestSpelling("Word999", 10);
		assertTrue(results3.size() == 4);
		assertTrue(results3.contains("Word1"));
		assertTrue(results3.contains("Word2"));
		assertTrue(results3.contains("Word3"));
		assertTrue(results3.contains("Word99"));

		service.close();
	}

	/**
	 * Testing clearing the index of all terms as searching afterwards yields
	 * zero results.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testClearIndex() throws Exception {
		Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

		ProjectService service = new ProjectServiceJpa();
		SpellingCorrectionHandler handler = service
				.getSpellingCorrectionHandler();
		handler.setTranslationId(new Long(5));
		createTestList(handler);
		handler.clearIndex();

		List<String> results = handler.suggestSpelling("Word", 10);

		assertTrue(results.size() == 0);

		service.close();
	}

	/**
	 * Testing clearing an index and recreating content with same handler
	 * behaves as before clearing it.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testClearIndexRepopulate() throws Exception {
		Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

		ProjectService service = new ProjectServiceJpa();
		SpellingCorrectionHandler handler = service
				.getSpellingCorrectionHandler();
		handler.setTranslationId(new Long(6));
		createTestList(handler);
		handler.clearIndex();
		createTestList(handler);

		List<String> results = handler.suggestSpelling("Word", 10);

		assertTrue(results.size() == 3);

		service.close();
	}

	/**
	 * Testing copying handler creates new version of the handler with identical
	 * content.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCopy() throws Exception {
		Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

		ProjectService service = new ProjectServiceJpa();
		SpellingCorrectionHandler handler = service
				.getSpellingCorrectionHandler();
		handler.setTranslationId(new Long(7));
		createTestList(handler);

		SpellingCorrectionHandler handlerCopy = handler.copy();

		List<String> results = handler.suggestSpelling("Word", 10);
		List<String> results2 = handlerCopy.suggestSpelling("Word", 10);
		assertEquals(results, results2);

		results = handler.suggestSpelling("Word1", 10);
		results2 = handlerCopy.suggestSpelling("Word1", 10);
		assertEquals(results, results2);

		service.close();
	}

	/**
	 * Testing the ability to create an index from a stream.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCreateDictionaryFromStream() throws Exception {
		Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

		ProjectService service = new ProjectServiceJpa();
		SpellingCorrectionHandler handler = service
				.getSpellingCorrectionHandler();
		handler.setTranslationId(new Long(8));

		StringBuilder builder = new StringBuilder();
		builder.append("Word-A");
		builder.append("\n");
		builder.append("Word-B");
		builder.append("\n");

		try {
			InputStream in = new ByteArrayInputStream(builder.toString()
					.getBytes("UTF-8"));
			in.close();
			handler.updateDictionaryFromStream(in, true);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> results = handler.suggestSpelling("Word-A", 10);
		assertTrue(results.size() == 1);
		assertTrue(!results.contains("Word-A"));
		assertTrue(results.contains("Word-B"));

		List<String> results2 = handler.suggestSpelling("Word-B", 10);
		assertTrue(results2.size() == 1);
		assertTrue(results2.contains("Word-A"));
		assertTrue(!results2.contains("Word-B"));

		service.close();
	}

	/**
	 * Testing grabbing content from one handler as a stream.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testGetAllEntriesAsStream() throws Exception {
		Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

		ProjectService service = new ProjectServiceJpa();
		SpellingCorrectionHandler handler = service
				.getSpellingCorrectionHandler();
		handler.setTranslationId(new Long(9));
		createTestList(handler);

		InputStream entriesStream = handler.getAllEntriesAsStream();
		SpellingCorrectionHandler handler2 = new DefaultSpellingCorrectionHandler(
				new Long(10), entriesStream);

		List<String> results = handler.suggestSpelling("Word1", 10);
		List<String> results2 = handler2.suggestSpelling("Word1", 10);
		assertEquals(results, results2);

		results = handler.suggestSpelling("Word2", 10);
		results2 = handler2.suggestSpelling("Word2", 10);
		assertEquals(results, results2);

		results = handler.suggestSpelling("Word3", 10);
		results2 = handler2.suggestSpelling("Word3", 10);
		assertEquals(results, results2);

		results = handler.suggestSpelling("Word4", 10);
		results2 = handler2.suggestSpelling("Word4", 10);
		assertEquals(results, results2);

		service.close();
	}

	/**
	 * Testing grabbing size of content.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testGetEntriesSize() throws Exception {
		Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

		ProjectService service = new ProjectServiceJpa();
		SpellingCorrectionHandler handler = service
				.getSpellingCorrectionHandler();
		handler.setTranslationId(new Long(11));
		createTestList(handler);

		assertEquals(3, handler.getEntriesSize());

		service.close();
	}

	/**
	 * Testing grabbing content from one handler via a List.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testGetAllEntriesAsList() throws Exception {
		Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

		ProjectService service = new ProjectServiceJpa();
		SpellingCorrectionHandler handler = service
				.getSpellingCorrectionHandler();
		handler.setTranslationId(new Long(12));
		createTestList(handler);

		List<String> baseList = new ArrayList<>();
		baseList.add("Word1");
		baseList.add("Word2");
		baseList.add("Word3");

		assertEquals(baseList, handler.getAllEntriesAsList());

		service.close();
	}

	/**
	 * Testing setting Translation Id.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testTranslationId() throws Exception {
		ProjectService service = new ProjectServiceJpa();
		SpellingCorrectionHandler handler = service
				.getSpellingCorrectionHandler();
		assertEquals(handler.getTranslationId(), new Long(-1));

		handler.setTranslationId(new Long(13));
		Long updatedTid = handler.getTranslationId();
		assertEquals(updatedTid, new Long(13));
	}

	/**
	 * Testing removing an entry from list.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testRemoveEntryFromIndex() throws Exception {
		Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

		ProjectService service = new ProjectServiceJpa();
		SpellingCorrectionHandler handler = service
				.getSpellingCorrectionHandler();
		handler.setTranslationId(new Long(14));
		createTestList(handler);

		List<String> results = handler.suggestSpelling("Word1", 10);

		assertTrue(results.size() == 2);
		assertTrue(!results.contains("Word1"));
		assertTrue(results.contains("Word2"));
		assertTrue(results.contains("Word3"));

		List<String> removeTerms = new ArrayList<>();
		removeTerms.add("Word1");
		handler.addEntries(removeTerms);
		handler.removeEntries(removeTerms);

		List<String> results2 = handler.suggestSpelling("Word4", 10);

		assertEquals(results, results2);

		assertTrue(results2.size() == 2);
		assertTrue(!results.contains("Word1"));
		assertTrue(results.contains("Word2"));
		assertTrue(results.contains("Word3"));

		service.close();
	}

	/**
	 * Teardown.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@After
	public void teardown() throws Exception {
		// n/a
	}

	/**
	 * Teardown class.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@AfterClass
	public static void teardownClass() throws Exception {
		// do nothing
	}

	private void createTestList(SpellingCorrectionHandler handler)
			throws IOException {
		List<String> dictContents = new ArrayList<>();
		dictContents.add("Word1");
		dictContents.add("Word2");
		dictContents.add("Word3");

		handler.updateDictionaryFromList(dictContents, false);
	}
}
