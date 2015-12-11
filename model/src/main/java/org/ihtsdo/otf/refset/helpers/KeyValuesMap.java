/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Container for key value-list pairs. Uses concrete collections classes to
 * support JAXB.
 */
@XmlRootElement(name = "keyValuesMap")
public class KeyValuesMap {

  /** The map. */
  private HashMap<String, StringList> map;

  /**
   * Instantiates an empty {@link KeyValuesMap}.
   */
  public KeyValuesMap() {
    //
  }

  /**
   * Instantiates a {@link KeyValuesMap} from the specified parameters.
   *
   * @param map the map
   */
  public KeyValuesMap(KeyValuesMap map) {
    this.map = new HashMap<String, StringList>(map.getMap());
  }

  /**
   * Put key and value into the map.
   *
   * @param key the key
   * @param value the value
   */
  public void add(String key, String value) {
    if (map == null) {
      map = new HashMap<>();
    }
    if (!map.containsKey(key)) {
      StringList list = new StringList();
      map.put(key, list);
    }
    map.get(key).getObjects().add(value);
  }

  /**
   * Returns the map.
   *
   * @return the map
   */
  @XmlElement
  public HashMap<String, StringList> getMap() {
    if (map == null) {
      map = new HashMap<>();
    }
    return map;
  }

  /**
   * Sets the map.
   *
   * @param map the map
   */
  public void setMap(HashMap<String, StringList> map) {
    this.map = map;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((map == null) ? 0 : map.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    KeyValuesMap other = (KeyValuesMap) obj;
    if (map == null) {
      if (other.map != null)
        return false;
    } else if (!map.equals(other.map))
      return false;
    return true;
  }

}