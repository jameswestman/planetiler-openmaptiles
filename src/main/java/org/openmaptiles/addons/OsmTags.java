package org.openmaptiles.addons;

import com.onthegomap.planetiler.reader.WithTags;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  public static Map<String, Object> GetOsmTags(WithTags sourceFeature) {
    var map = new HashMap<String, Object>();

    for (var tag : sourceFeature.tags().entrySet()) {
      final var key = tag.getKey();

      if (OSM_TAGS.contains(key) || key.startsWith("addr") || key.startsWith("contact")) {
        map.put("osm:" + key, tag.getValue());
      }
    }

    return map;
  }

  // Gets the tags from a source feature that are in the given set, prefixed with 'osm:'.
  private static Map<String, Object> GetOsmTags(WithTags sourceFeature, Set<String> includeTags) {
    var map = new HashMap<String, Object>();

    for (var tag : sourceFeature.tags().entrySet()) {
      final var key = tag.getKey();

      if (includeTags.contains(key)) {
        map.put("osm:" + key, tag.getValue());
      }
    }

    return map;
  }

  private static final HashSet<String> OSM_TRANSPORTATION_TAGS = new HashSet<String>(List.of(
    "maxspeed"
  ));

  public static Map<String, Object> GetOsmTransportationTags(WithTags sourceFeature) {
    return GetOsmTags(sourceFeature, OSM_TRANSPORTATION_TAGS);
  }
}
