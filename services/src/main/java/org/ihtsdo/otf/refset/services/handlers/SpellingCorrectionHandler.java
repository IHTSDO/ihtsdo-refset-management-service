/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.ihtsdo.otf.refset.helpers.Configurable;

/**
 * Generically represents a handler for accessing terminology objects.
 */
public interface SpellingCorrectionHandler extends Configurable {
	/**
	 * Adds content to index and performs re-index.
	 *
	 * @param newTerms
	 *            Defines list of terms that to be added to existing index
	 *
	 * @throws IOException
	 */
	public void addEntries(List<String> newTerms) throws IOException;

	/**
	 * Removes content to index and performs re-index.
	 *
	 * @param removeTerms
	 *            Defines list of terms that to be removed from existing index
	 *
	 * @throws IOException
	 */
	public void removeEntries(List<String> removeTerms) throws IOException;

	/**
	 * Creates a test dictionary from a pre-defined list of entries.
	 * 
	 * @param contents
	 *            A list of entries from which to populate the index.
	 * @param append
	 *            Defines if contents are to recreate or append index
	 *            
	 * @throws IOException
	 */
	public void updateDictionaryFromList(List<String> contents, boolean append) throws IOException;

	/**
	 * Creates index from a UTF-8 input stream.
	 *
	 * @param in
	 *            The input stream used to create the index
	 * @param append
	 *            Defines if contents are to recreate or append index
	 *
	 * @throws IOException
	 */
	public void updateDictionaryFromStream(InputStream in, boolean append) throws IOException;

	/**
	 * Updates index based on current contents.
	 *
	 * @param append
	 *            Defines if to append contents to existing index or create from
	 *            scratch
	 *
	 * @throws IOException
	 */
	public void reindex(boolean append) throws IOException;

	/**
	 * Clears the index of contents resulting in no entries in the index.
	 *
	 * @throws IOException
	 */
	public void clearIndex() throws IOException;

	/**
	 * Returns a list of suggested spellings based on a given term. If term
	 * exists in index, that item is not returned as part of suggested spelling.
	 *
	 * @param term
	 *            The term queried for suggestions
	 * @param amt
	 *            The number of terms returned
	 * @return List of Strings representing the suggested spellings
	 */
	public List<String> suggestSpelling(String term, int amt);

	/**
	 * Returns the index as an ByteArrayInputStream.
	 *
	 * @return ByteArrayInputStream representing index's contents.
	 *
	 * @throws IOException
	 */
	public ByteArrayInputStream getAllEntriesAsStream() throws IOException;

	/**
	 * Returns the index as a List of Streams.
	 *
	 * @return List of Strings representing index's contents
	 *
	 * @throws IOException
	 */
	public List<String> getAllEntriesAsList() throws IOException;

	/**
	 * Returns the number of entries in the index.
	 *
	 * @return The size of the index
	 *            
	 * @throws IOException
	 */
	public int getEntriesSize() throws IOException;

	/**
	 * Sets/Updates the translation identifier of the SpellingCorrectionHandler.
	 *
	 * @param translationId
	 *            The translation identifier for which the SpellingCorrectionHandler
	 *            represents
	 */
	public void setTranslationId(Long translationId);

	/**
	 * Returns the translation identifier of the SpellingCorrectionHandler.
	 *
	 * @return The translation identifier.
	 *
	 * @throws IOException
	 */
	public Long getTranslationId();


	/**
	 * Copy the handler. This is needed used because of how the terminology
	 * handler is instantiated. One template object is created, and then copies
	 * of it are returned for individual requests.
	 *
	 * @return the terminology handler
	 * @throws Exception
	 *             the exception
	 */
	public SpellingCorrectionHandler copy() throws Exception;

	/**
	 * Returns the terminology editions.
	 *
	 * @return the terminology editions
	 * @throws Exception
	 *             the exception
	 */
	public List<String> getSpellingCorrectionEditions() throws Exception;
}
