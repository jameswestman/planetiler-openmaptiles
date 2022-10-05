package org.openmaptiles.util;

import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class OsmTags {
  private static final HashSet<String> OSM_TAGS = new HashSet<String>(List.of(
    "brand:wikidata",
    "brand:wikipedia",
    "ele",
    "email",
    "internet_access",
    "note",
    "opening_hours",
    "phone",
    "population",
    "religion",
    "takeaway",
    "toilets",
    "website",
    "wheelchair",
    "wikidata",
    "wikipedia"
  ));

  // Gets standard OSM tags from a source feature.
  public static Map<String, Object> GetOsmTags(SourceFeature sourceFeature) {
    var map = new HashMap<String, Object>();

    for (var tag : sourceFeature.tags().entrySet()) {
      final var key = tag.getKey();

      if (OSM_TAGS.contains(key) || key.startsWith("addr") || key.startsWith("contact")) {
        map.put("osm:" + key, tag.getValue());
      }
    }

    return map;
  }
}
