package org.openmaptiles.addons;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureCollector.Factory;
import com.onthegomap.planetiler.FeatureCollector.Feature;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SimpleFeature;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement.Relation;
import com.onthegomap.planetiler.reader.osm.OsmRelationInfo;
import com.onthegomap.planetiler.stats.Stats;
import com.onthegomap.planetiler.util.Translations;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.openmaptiles.Layer;
import org.openmaptiles.OpenMapTilesProfile;
import org.openmaptiles.layers.Boundary.BoundaryRelation;
import org.openmaptiles.util.OmtLanguageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Geocode
        implements Layer, OpenMapTilesProfile.OsmAllProcessor,
        OpenMapTilesProfile.OsmRelationPreprocessor,
        OpenMapTilesProfile.FinishHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Geocode.class);

    private final String LAYER_NAME = "geocode";
    private final double BUFFER_SIZE = 4.0;
    private final int ZOOM_LEVEL = 10;

    private Translations translations;
    private Stats stats;

    private final Map<Long, BoundaryInfo> boundaryGeometries = new HashMap<>();

    public Geocode(Translations translations, Stats stats) {
        this.translations = translations;
        this.stats = stats;
    }

    @Override
    public String name() {
        return LAYER_NAME;
    }

    @Override
    public void release() {
        boundaryGeometries.clear();
    }

    @Override
    public List<OsmRelationInfo> preprocessOsmRelation(Relation relation) {
        if (!relation.hasTag("type", "boundary") || !relation.hasTag("admin_level")
                || !relation.hasTag("boundary", "administrative"))
            return null;

        var adminLevel = relation.getLong("admin_level");
        if (adminLevel < 2 || adminLevel > 10)
            return null;

        var attrs = new HashMap<String, Object>();

        attrs.put("admin_level", relation.getTag("admin_level"));
        attrs.put("wikidata", relation.getTag("wikidata"));
        attrs.put("iso_a2", relation.getTag("ISO3166-1:alpha2"));

        attrs.putAll(OsmTags.GetOsmTags(relation));
        attrs.putAll(OmtLanguageUtils.getNames(relation.tags(), translations));

        synchronized (this) {
            var existing = boundaryGeometries.get(relation.id());
            if (existing != null) {
                existing.attrs.putAll(attrs);
            } else {
                boundaryGeometries.put(relation.id(), new BoundaryInfo(attrs, new ArrayList<>()));
            }
        }

        return null;
    }

    @Override
    public void processAllOsm(SourceFeature feature, FeatureCollector features) {
        if (!feature.canBeLine()) {
            return;
        }

        var relationInfos = feature.relationInfo(BoundaryRelation.class);
        if (relationInfos.isEmpty())
            return;

        synchronized (this) {
            for (var info : relationInfos) {
                try {
                    boundaryGeometries
                            .computeIfAbsent(info.relation().id(),
                                    key -> new BoundaryInfo(new HashMap<>(), new ArrayList<>())).linestrings
                            .add(feature.line());
                } catch (GeometryException e) {
                    LOGGER.warn("Cannot extract boundary line from " + feature);
                }
            }
        }
    }

    @Override
    public void finish(String sourceName, Factory featureCollectors, Consumer<Feature> emit) {
        if (!OpenMapTilesProfile.OSM_SOURCE.equals(sourceName))
            return;

        var timer = stats.startStage("geocode");

        for (var boundary : boundaryGeometries.entrySet()) {
            var regionId = boundary.getKey();
            var boundaryInfo = boundary.getValue();

            var polygonizer = new Polygonizer();
            polygonizer.add(boundaryInfo.linestrings);

            try {
                var combined = polygonizer.getGeometry().union();
                if (combined.isEmpty()) {
                    LOGGER.warn(
                            "Unable to form closed polygon for OSM relation " + regionId);
                } else {
                    var features = featureCollectors.get(SimpleFeature.fromWorldGeometry(combined));
                    features.polygon(LAYER_NAME).setBufferPixels(BUFFER_SIZE)
                            .setMinZoom(ZOOM_LEVEL).setMaxZoom(ZOOM_LEVEL)
                            .putAttrs(boundaryInfo.attrs);
                    for (var feature : features) {
                        emit.accept(feature);
                    }
                }
            } catch (TopologyException e) {
                LOGGER
                        .warn("Unable to build boundary polygon for OSM relation " + regionId + ": " + e.getMessage());
            }
        }

        timer.stop();
    }

    private record BoundaryInfo(Map<String, Object> attrs, List<Geometry> linestrings) {
    }
}
