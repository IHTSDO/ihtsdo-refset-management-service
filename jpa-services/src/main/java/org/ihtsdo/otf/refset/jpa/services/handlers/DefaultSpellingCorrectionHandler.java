/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.services.handlers.SpellingCorrectionHandler;

/**
 * Default implementation of {@link SpellingCorrectionHandler}. Leverages the
 * IHTSDO SpellingCorrection server to the extent possible for interacting with
 * SpellingCorrection components. Uses local storage where not possible.
 */
public class DefaultSpellingCorrectionHandler extends RootServiceJpa implements
		SpellingCorrectionHandler {

	private static final String INDEX_NAME = "spellingCorrectionIdx-";

	private PlainTextDictionary ptDict;
	private SpellChecker checker;
	private Long tId = new Long(-1);
	private FSDirectory indexDir;

	/**
	 * Instantiates an empty {@link DefaultSpellingCorrectionHandler}.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public DefaultSpellingCorrectionHandler() throws Exception {
	}

	public DefaultSpellingCorrectionHandler(Long translationId)
			throws Exception {
		tId = translationId;
	}

	public DefaultSpellingCorrectionHandler(Long translationId,
			List<String> contents) throws Exception {
		tId = translationId;
		updateDictionaryFromList(contents, true);
	}

	public DefaultSpellingCorrectionHandler(Long translationId, InputStream in)
			throws Exception {
		tId = translationId;
		updateDictionaryFromStream(in, true);
	}

	
	

	@Override
	public void addEntries(List<String> newTerms) throws IOException {
		buildDictionary(newTerms);
		reindex(true);
	}

	@Override
	public void removeEntries(List<String> removeTerms) throws IOException {
		StringBuilder sb = new StringBuilder();

		File f = new File(INDEX_NAME + tId + ".txt");

		if (f.exists()) {
			// Create new Stream except for selected terms
			IndexReader ir = DirectoryReader.open(FSDirectory.open(f));
			int maxDoc = ir.maxDoc();

			if (maxDoc > 0) {
				for (int i = 0; i < maxDoc; i++) {
					Document doc = ir.document(i);
					String val = doc.get(doc.getFields().get(0).name());

					if (!removeTerms.contains(val)) {
						sb.append(val);
						sb.append("\n");
					}
				}
			}

			// Clear Index prior to rebuild
			clearIndex();

			// Build new Index From Stream
			updateDictionaryFromStream(new ByteArrayInputStream(sb.toString()
					.getBytes("UTF-8")), true);
		}
	}

	@Override
	public void updateDictionaryFromList(List<String> contents, boolean append)
			throws IOException {
		buildDictionary(contents);
		reindex(append);
	}

	public void updateDictionaryFromStream(InputStream in, boolean append)
			throws IOException {
		ptDict = new PlainTextDictionary(in);

		reindex(append);
	}

	@Override
	public void reindex(boolean append) throws IOException {
		try {
			IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LATEST,
					new StandardAnalyzer());
			if (append) {
				iwConfig.setOpenMode(OpenMode.APPEND);
			}

			if (tId < 0) {
				throw new IOException("Translation Id never set");
			}
			indexDir = FSDirectory.open(new File(INDEX_NAME + tId + ".txt"));
			checker = new SpellChecker(indexDir);

			checker.indexDictionary(ptDict, iwConfig, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void clearIndex() throws IOException {
		checker.clearIndex();
	}

	@Override
	public List<String> suggestSpelling(String term, int amt) {
		try {
			String[] results = checker.suggestSimilar(term, amt);
			return Arrays.asList(results);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public ByteArrayInputStream getAllEntriesAsStream() throws IOException {
		StringBuilder sb = new StringBuilder();

		File f = new File(INDEX_NAME + tId + ".txt");

		if (f.exists()) {
			IndexReader ir = DirectoryReader.open(FSDirectory.open(f));
			int maxDoc = ir.maxDoc();

			if (maxDoc > 0) {
				for (int i = 0; i < maxDoc; i++) {
					Document doc = ir.document(i);
					String val = doc.get(doc.getFields().get(0).name());
					sb.append(val);
					sb.append("\n");
				}
			}
		}

		return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
	}

	@Override
	public List<String> getAllEntriesAsList() throws IOException {
		List<String> retList = new ArrayList<>();

		File f = new File(INDEX_NAME + tId + ".txt");

		if (f.exists()) {
			IndexReader ir = DirectoryReader.open(FSDirectory.open(f));
			int maxDoc = ir.maxDoc();

			if (maxDoc > 0) {
				for (int i = 0; i < maxDoc; i++) {
					Document doc = ir.document(i);
					String val = doc.get(doc.getFields().get(0).name());
					retList.add(val);
				}
			}
		}

		return retList;
	}

	@Override
	public int getEntriesSize() throws IOException {
		return getAllEntriesAsList().size();
	}

	@Override
	public void setTranslationId(Long translationId) {
		tId = translationId;
	}

	@Override
	public Long getTranslationId() {
		return tId;
	}


	/* see superclass */
	@Override
	public SpellingCorrectionHandler copy() throws Exception {
		DefaultSpellingCorrectionHandler handler = new DefaultSpellingCorrectionHandler();
		handler.setTranslationId(this.tId);

		InputStream in = this.getAllEntriesAsStream();

		if (this.getEntriesSize() > 0) {
			handler.updateDictionaryFromStream(in, true);
		}

		return handler;
	}

	/* see superclass */
	@Override
	public void setProperties(Properties p) throws Exception {
		if (p.containsKey("translationId")) {
			this.setTranslationId(Long.parseLong(p.getProperty("translationId")));
		} else {
			this.setTranslationId(new Long(-1));
		}
	}

	/* see superclass */
	@Override
	public String getName() {
		return "Default SpellingCorrection handler";
	}

	/* see superclass */
	@Override
	public List<String> getSpellingCorrectionEditions() throws Exception {
		return Arrays.asList(new String[] { "SNOMEDCT" });
	}

	private void buildDictionary(List<String> dictContents) {
		StringBuilder builder = new StringBuilder();
		for (String s : dictContents) {
			builder.append(s);
			builder.append("\n");
		}

		try {
			InputStream is = new ByteArrayInputStream(builder.toString()
					.getBytes("UTF-8"));
			ptDict = new PlainTextDictionary(is);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
