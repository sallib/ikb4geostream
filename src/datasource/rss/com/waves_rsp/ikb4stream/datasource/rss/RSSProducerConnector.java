package com.waves_rsp.ikb4stream.datasource.rss;

import com.rometools.modules.georss.GeoRSSModule;
import com.rometools.modules.georss.GeoRSSUtils;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.GeoCoderJacksonParser;
import com.waves_rsp.ikb4stream.core.util.OpenNLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class RSSProducerConnector implements IProducerConnector {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(RSSProducerConnector.class, "resources/datasource/rss/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(RSSProducerConnector.class);
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    private final String source;
    private final URL url;

    public RSSProducerConnector() {
        try {
            this.source = PROPERTIES_MANAGER.getProperty("RSSProducerConnector.source");
            String urlString = PROPERTIES_MANAGER.getProperty("RSSProducerConnector.url");
            this.url = new URL(urlString);
        } catch (IllegalArgumentException | MalformedURLException e) {
            LOGGER.error("Invalid configuration [] ", e);
            throw new IllegalStateException("Invalid configuration");
        }
    }

    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                long start = System.currentTimeMillis(); //metrics
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(this.url));

                feed.getEntries().forEach(entry -> {
                    Date startDate = entry.getPublishedDate();
                    String title = entry.getTitle();
                    String description = entry.getDescription().getValue();
                    if (description == null)
                        description = "";

                    Date endDate = Date.from(Instant.now());
                    if (startDate == null)
                        startDate = endDate;

                    String completeDesc = title + " " + description;
                    GeoRSSModule module = GeoRSSUtils.getGeoRSS(entry);
                    if (module != null && module.getPosition() != null) {
                        LatLong latLong = new LatLong(module.getPosition().getLatitude(), module.getPosition().getLongitude());
                        Event event = new Event(latLong, startDate, endDate, completeDesc, source);
                        dataProducer.push(event);
                    } else {

                        LatLong latlong = geocodeRSS(completeDesc);
                        if (latlong != null) {
                            Event event = new Event(latlong, startDate, endDate, completeDesc, source);
                            dataProducer.push(event);
                        } else {
                            LOGGER.info("Can't geocode this RSS ");
                        }
                    }
                });
                long end = System.currentTimeMillis();
                METRICS_LOGGER.log("time_process_" + this.source, String.valueOf(end - start));
            } catch (IOException | FeedException e) {
                LOGGER.error("Can't parse RSS [] ", e);
            }
        }
    }


    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("RSSProducerConnector.enable"));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * Select a list of location from a RSS with the NER OpenNLP algorithme.
     * Then, geolocalize the first location found with the geocoder Nominatim (OSM)
     *
     * @param text to analyze
     * @return a latLong coordinates
     */
    private LatLong geocodeRSS(String text) {
        long start = System.currentTimeMillis();
        GeoCoderJacksonParser geocoder = new GeoCoderJacksonParser();
        List<String> locations = OpenNLP.applyNLPner(text, OpenNLP.nerOptions.LOCATION);
        if (!locations.isEmpty()) {
            long end = System.currentTimeMillis();
            METRICS_LOGGER.log("time_geocode_" + this.source, String.valueOf(end - start));
            return geocoder.parse(locations.get(0));
        }
        return null;
    }
}
