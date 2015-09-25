/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class MapType.
 *
 * @param <K> the
 * @param <V> the
 */
public class MapType<K, V> {

  /** The entry. */
  private List<MapEntryType<K, V>> entry = new ArrayList<MapEntryType<K, V>>();

  /**
   * Instantiates an empty {@link MapType}.
   */
  public MapType() {
  }

  /**
   * Instantiates a {@link MapType} from the specified parameters.
   *
   * @param map the map
   */
  public MapType(Map<K, V> map) {
    for (Map.Entry<K, V> e : map.entrySet()) {
      entry.add(new MapEntryType<K, V>(e));
    }
  }

  /**
   * Returns the entry.
   *
   * @return the entry
   */
  public List<MapEntryType<K, V>> getEntry() {
    return entry;
  }

  /**
   * Sets the entry.
   *
   * @param entry the entry
   */
  public void setEntry(List<MapEntryType<K, V>> entry) {
    this.entry = entry;
  }
}
