/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import java.util.List;

import org.ihtsdo.otf.refset.helpers.LogEntry;
import org.ihtsdo.otf.refset.helpers.PfsParameter;

/**
 * Generically represents a service.
 */
public interface RootService {

  /**
   * Open the factory.
   *
   * @throws Exception the exception
   */
  public void openFactory() throws Exception;

  /**
   * Close the factory.
   *
   * @throws Exception the exception
   */
  public void closeFactory() throws Exception;

  /**
   * Gets the transaction per operation.
   *
   * @return the transaction per operation
   * @throws Exception the exception
   */
  public boolean getTransactionPerOperation() throws Exception;

  /**
   * Sets the transaction per operation.
   *
   * @param transactionPerOperation the new transaction per operation
   * @throws Exception the exception
   */
  public void setTransactionPerOperation(boolean transactionPerOperation)
    throws Exception;

  /**
   * Commit.
   *
   * @throws Exception the exception
   */
  public void commit() throws Exception;

  /**
   * Rollback.
   *
   * @throws Exception the exception
   */
  public void rollback() throws Exception;

  /**
   * Begin transaction.
   *
   * @throws Exception the exception
   */
  public void beginTransaction() throws Exception;

  /**
   * Closes the manager.
   *
   * @throws Exception the exception
   */
  public void close() throws Exception;

  /**
   * Clears the manager.
   *
   * @throws Exception the exception
   */
  public void clear() throws Exception;

  /**
   * Refresh caches.
   *
   * @throws Exception the exception
   */
  public void refreshCaches() throws Exception;

  /**
   * Sets the last modified flag.
   *
   * @param lastModifiedFlag the last modified flag
   */
  public void setLastModifiedFlag(boolean lastModifiedFlag);

  /**
   * Find log entries for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public List<LogEntry> findLogEntriesForQuery(String query, PfsParameter pfs)
    throws Exception;

  /**
   * Adds the log entry.
   *
   * @param logEntry the log entry
   * @return the log entry
   * @throws Exception the exception
   */
  public LogEntry addLogEntry(LogEntry logEntry) throws Exception;

  /**
   * Update log entry.
   *
   * @param logEntry the log entry
   * @throws Exception the exception
   */
  public void updateLogEntry(LogEntry logEntry) throws Exception;

  /**
   * Removes the log entry.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeLogEntry(Long id) throws Exception;

  /**
   * Returns the log entry.
   *
   * @param id the id
   * @return the log entry
   * @throws Exception the exception
   */
  public LogEntry getLogEntry(Long id) throws Exception;

  /**
   * Apply pfs to list.
   *
   * @param <T> the
   * @param list the list
   * @param clazz the clazz
   * @param totalCt the total ct
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public <T> List<T> applyPfsToList(List<T> list, Class<T> clazz,
    int[] totalCt, PfsParameter pfs) throws Exception;

}