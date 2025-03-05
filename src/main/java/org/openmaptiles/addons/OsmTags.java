package org.openmaptiles.addons;

import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.WithTags;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmSourceFeature;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class OsmTags {
  private static final HashSet<String> OSM_TAGS = new HashSet<String>(List.of(
    "brand:wikidata",
    "brand:wikipedia",
    "cuisine",
    "ele",
    "email",
    "internet_access",
    "level:ref",
    "network:wikidata",
    "note",
    "opening_hours",
    "phone",
    "population",
    "religion",
    "sport",
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

  public static long GetFeatureId(SourceFeature sourceFeature) {
    if (sourceFeature instanceof OsmSourceFeature osmSourceFeature) {
      var element = osmSourceFeature.originalElement();
      long id = sourceFeature.id() * 10;
      if (element instanceof OsmElement.Relation) {
        id += 4;
      } else if (element instanceof OsmElement.Way) {
        id += 1;
      }
      return id;
    } else {
      return sourceFeature.id() * 10 + 9;
    }
  }
}
