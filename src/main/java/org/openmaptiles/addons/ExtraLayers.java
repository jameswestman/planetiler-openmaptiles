package org.openmaptiles.addons;

import com.onthegomap.planetiler.config.PlanetilerConfig;
import com.onthegomap.planetiler.stats.Stats;
import com.onthegomap.planetiler.util.Translations;

import java.util.ArrayList;
import java.util.List;
import org.openmaptiles.Layer;

/**
 * Registry of extra custom layers that you can add to the openmaptiles schema.
 */
public class ExtraLayers {

  public static List<Layer> create(Translations translations, PlanetilerConfig config, Stats stats) {
    var layers = new ArrayList<Layer>();

    if (config.arguments().getBoolean("geocode", "Add geocode layer", false)) {
      layers.add(new Geocode(translations, stats));
    }

    return layers;
  }
}
