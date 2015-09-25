/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

/**
 * A generic map entry type.
 *
 * @param <K> the
 * @param <V> the
 */
public class MapEntryType<K, V> {

  /**  The key. */
  private K key;

  /**  The value. */
  private V value;

  /**
   * Instantiates an empty {@link MapEntryType}.
   */
  public MapEntryType() {
  }

  /**
   * Instantiates a {@link MapEntryType} from the specified parameters.
   *
   * @param e the e
   */
  public MapEntryType(Map.Entry<K, V> e) {
    key = e.getKey();
    value = e.getValue();
  }

  /**
   * Returns the key.
   *
   * @return the key
   */
  @XmlElement
  public K getKey() {
    return key;
  }

  /**
   * Sets the key.
   *
   * @param key the key
   */
  public void setKey(K key) {
    this.key = key;
  }

  /**
   * Returns the value.
   *
   * @return the value
   */
  @XmlElement
  public V getValue() {
    return value;
  }

  /**
   * Sets the value.
   *
   * @param value the value
   */
  public void setValue(V value) {
    this.value = value;
  }
}