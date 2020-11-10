/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.ihtsdo.otf.refset.algo.Algorithm;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;
import org.reflections.Reflections;

/**
 * Implementation of an algorithm to reindex all classes annotated
 * with @Indexed.
 */
public class LuceneReindexAlgorithm extends RootServiceJpa
    implements Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The terminology. */
  private String indexedObjects;

  /**
   * Batch size to load objects for Full Text Entity Manager. Default is 100.
   */
  private Integer batchSizeToLoadObjects;

  /**
   * Threads used for loading objects for Full Text Entity Manager. Default is
   * 4.
   */
  private Integer threadsToLoadObjects;

  /** The full text entity manager. */
  private FullTextEntityManager fullTextEntityManager;

  /** Sort object to index by size descending or ascending. */
  private static boolean DESC = false;

  /**
   * Instantiates an empty {@link LuceneReindexAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public LuceneReindexAlgorithm() throws Exception {
    super();
  }

  /**
   * Sets the indexed objects.
   *
   * @param indexedObjects the indexed objects
   */
  public void setIndexedObjects(String indexedObjects) {
    this.indexedObjects = indexedObjects;
  }

  /**
   * Sets the batch size to load objects.
   *
   * @param batchSizeToLoadObjects the batch size to load objects
   */
  public void setBatchSizeToLoadObjects(Integer batchSizeToLoadObjects) {
    this.batchSizeToLoadObjects = batchSizeToLoadObjects;
  }

  /**
   * Sets the threads to load objects.
   *
   * @param threadsToLoadObjects the threads to load objects
   */
  public void setThreadsToLoadObjects(Integer threadsToLoadObjects) {
    this.threadsToLoadObjects = threadsToLoadObjects;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    if (fullTextEntityManager == null) {
      fullTextEntityManager = Search.getFullTextEntityManager(manager);
    }
    computeLuceneIndexes(indexedObjects, this.batchSizeToLoadObjects,
        this.threadsToLoadObjects);
    // fullTextEntityManager.close();
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    if (fullTextEntityManager == null) {
      fullTextEntityManager = Search.getFullTextEntityManager(manager);
    }
    clearLuceneIndexes();
    // fullTextEntityManager.close();
  }

  /**
   * Compute lucene indexes.
   *
   * @param indexedObjects the indexed objects
   * @param batchSizeToLoadObjects the batch size to load objects
   * @param threadsToLoadObjects the threads to load objects
   * @throws Exception the exception
   */
  private void computeLuceneIndexes(String indexedObjects,
    Integer batchSizeToLoadObjects, Integer threadsToLoadObjects)
    throws Exception {
    // set of objects to be re-indexed
    final Set<String> objectsToReindex = new HashSet<>();
    final Map<String, Class<?>> reindexMap = new HashMap<>();
    final Reflections reflections = new Reflections();
    for (final Class<?> clazz : reflections
        .getTypesAnnotatedWith(Indexed.class)) {
      reindexMap.put(clazz.getSimpleName(), clazz);
    }

    // if no parameter specified, re-index all objects
    if (indexedObjects == null || indexedObjects.isEmpty()) {

      // Add all class names
      for (final String className : reindexMap.keySet()) {
        if (objectsToReindex.contains(className)) {
          // This restriction can be removed by using full class names
          // however, then calling the mojo is more complicated
          throw new Exception(
              "Reindex process assumes simple class names are different.");
        }
        objectsToReindex.add(className);
      }

      // otherwise, construct set of indexed objects
    } else {

      // remove white-space and split by comma
      String[] objects = indexedObjects.replaceAll(" ", "").split(",");

      // add each value to the set
      for (String object : objects)
        objectsToReindex.add(object);
    }

    Logger.getLogger(getClass()).info("Starting reindexing for:");
    for (String objectToReindex : objectsToReindex) {
      Logger.getLogger(getClass()).info("  " + objectToReindex);
    }

    // Reindex each object
    int batchSize =
        (batchSizeToLoadObjects != null ? batchSizeToLoadObjects : 500);
    int threads =
        (threadsToLoadObjects != null ? threadsToLoadObjects : 4);

    Map<String, Long> reindexMapOrdered =
        sortByValue(jpaSize(reindexMap), DESC);
    for (final String key : reindexMapOrdered.keySet()) {
      Logger.getLogger(getClass())
          .info("  " + key + " : " + reindexMapOrdered.get(key));
    }

    reindexMapOrdered.keySet().stream().forEach(key -> {
      // Concepts
      if (objectsToReindex.contains(key)) {
        final long startTime = System.currentTimeMillis();
        Logger.getLogger(getClass()).info(" Creating indexes for " + key);
        fullTextEntityManager.purgeAll(reindexMap.get(key));
        fullTextEntityManager.flushToIndexes();
        try {
          fullTextEntityManager.createIndexer(reindexMap.get(key))
              .batchSizeToLoadObjects(batchSize).cacheMode(CacheMode.NORMAL)
              .threadsToLoadObjects(threads).idFetchSize(1000)
              .startAndWait();
        } catch (InterruptedException e) {
          Logger.getLogger(getClass()).error("ERROR", e);
        }
        objectsToReindex.remove(key);
        Logger.getLogger(getClass()).info(" Finished " + key + " in "
            + (System.currentTimeMillis() - startTime) + " ms");
      }
    });

    if (objectsToReindex.size() != 0) {
      throw new Exception(
          "The following objects were specified for re-indexing, but do not exist as indexed objects: "
              + objectsToReindex.toString());
    }

    // Cleanup
    Logger.getLogger(getClass()).info("done ...");
  }

  /**
   * Clear lucene indexes.
   *
   * @throws Exception the exception
   */
  private void clearLuceneIndexes() throws Exception {

    final Reflections reflections = new Reflections();
    for (final Class<?> clazz : reflections
        .getTypesAnnotatedWith(Indexed.class)) {
      fullTextEntityManager.purgeAll(clazz);
    }
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
  public void checkPreconditions() throws Exception {
    // n/a
  }

  /**
   * Sort by value in ascending or descending order
   *
   * @param unsortMap the unsort map
   * @param order the order
   * @return the map
   */
  private Map<String, Long> sortByValue(Map<String, Long> unsortMap,
    final boolean order) {
    List<Entry<String, Long>> list = new LinkedList<>(unsortMap.entrySet());

    // Sorting the list based on values
    list.sort((o1, o2) -> order
        ? o1.getValue().compareTo(o2.getValue()) == 0
            ? o1.getKey().compareTo(o2.getKey())
            : o1.getValue().compareTo(o2.getValue())
        : o2.getValue().compareTo(o1.getValue()) == 0
            ? o2.getKey().compareTo(o1.getKey())
            : o2.getValue().compareTo(o1.getValue()));
    return list.stream().collect(Collectors.toMap(Entry::getKey,
        Entry::getValue, (a, b) -> b, LinkedHashMap::new));
  }

  /**
   * Get size of each Jpa.
   *
   * @param reindexMap the reindex map
   * @return the map
   * @throws Exception the exception
   */
  private Map<String, Long> jpaSize(Map<String, Class<?>> reindexMap)
    throws Exception {
    final Map<String, Long> objectSizes = new HashMap<>();
    try (RefsetService service = new RefsetServiceJpa();) {
      for (final String key : reindexMap.keySet()) {
        final Query query =
            ((RootServiceJpa) service).getEntityManager().createQuery(
                "select count(1) from " + reindexMap.get(key).getSimpleName());
        objectSizes.put(key, ((Long) query.getSingleResult()).longValue());
      }
    } catch (Exception e) {
      throw e;
    }
    return objectSizes;
  }

}
