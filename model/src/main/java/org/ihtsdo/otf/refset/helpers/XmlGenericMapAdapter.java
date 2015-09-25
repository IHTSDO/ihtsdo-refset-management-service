/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The Class XmlGenericMapAdapter.
 *
 * @param <K> the
 * @param <V> the
 */
public class XmlGenericMapAdapter<K, V> extends
    XmlAdapter<MapType<K, V>, Map<K, V>> {

  /* see superclass */
  @Override
  public Map<K, V> unmarshal(MapType<K, V> v) throws Exception {
    HashMap<K, V> map = new HashMap<K, V>();

    for (MapEntryType<K, V> mapEntryType : v.getEntry()) {
      map.put(mapEntryType.getKey(), mapEntryType.getValue());
    }
    return map;
  }

  /* see superclass */
  @SuppressWarnings("rawtypes")
  @Override
  public MapType marshal(Map<K, V> v) throws Exception {
    MapType<K, V> mapType = new MapType<K, V>();

    for (Map.Entry<K, V> entry : v.entrySet()) {
      MapEntryType<K, V> mapEntryType = new MapEntryType<K, V>();
      mapEntryType.setKey(entry.getKey());
      mapEntryType.setValue(entry.getValue());
      mapType.getEntry().add(mapEntryType);
    }
    return mapType;
  }
}