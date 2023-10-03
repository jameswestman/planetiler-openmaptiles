package org.openmaptiles.addons;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SourceFeature;

public interface TimezoneProcessor {
    void processTimezone(SourceFeature feature, FeatureCollector features);
}
