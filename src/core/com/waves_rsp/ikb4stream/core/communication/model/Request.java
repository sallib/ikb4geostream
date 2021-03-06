/*
 * Copyright (C) 2017 ikb4stream team
 * ikb4stream is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ikb4stream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package com.waves_rsp.ikb4stream.core.communication.model;

import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Objects;

/**
 * Request class which represents an anomaly
 *
 * @author ikb4stream
 * @version 1.0
 */
public class Request {
    /**
     * Beginning of Events to get from database
     */
    private final Date start;
    /**
     * End of Events to get from database
     */
    private final Date end;
    /**
     * {@link BoundingBox} of Event to get
     */
    private final BoundingBox boundingBox;
    /**
     * The reception date of the request
     */
    private final Date requestReception;
    private final String search;
    private final String source;

    /**
     * Logger used to log all information in this module
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);

    /**
     * Properties of this class
     *
     * @see PropertiesManager
     * @see PropertiesManager#getProperty(String)
     * @see PropertiesManager#getInstance(Class)
     */
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(Request.class);

    /**
     * The Request class constructor
     *
     * @param start            is the starting date of an anomaly
     * @param end              is the end date of an anomaly
     * @param search           word
     * @param requestReception is the reception date of the request
     * @throws NullPointerException if one of params is null
     */
    public Request(Date start, Date end, String search, String source, BoundingBox boundingBox, Date requestReception) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        Objects.requireNonNull(search);
        Objects.requireNonNull(source);
        Objects.requireNonNull(boundingBox);
        Objects.requireNonNull(requestReception);

        this.start = start;
        this.end = end;
        this.boundingBox = boundingBox;
        this.search = search;
        this.source = source;
        this.requestReception = requestReception;
    }

    public Request(Date start, Date end, String search, String source, Date requestReception) {
            this.start = start;
            this.end = end;
            this.boundingBox = null;
            this.search = search;
            this.source = source;
            this.requestReception = requestReception;
    }

    /**
     * Get starting date
     *
     * @return the starting date of an anomaly
     * @see Request#start
     */
    public Date getStart() {
        return start;
    }

    /**
     * Get ending date
     *
     * @return the end date of an anomaly
     * @see Request#end
     */
    public Date getEnd() {
        return end;
    }

    /**
     * Get BoundingBox
     *
     * @return the bbox
     * @see Request#@BoundingBox
     */

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    /**
     * Get search word of Request
     *
     * @return the word request
     * @see Request#search
     */

    public String getSearch() {
        return search;
    }

    /**
     * Get source of Request
     *
     * @return the source request
     * @see Request#source
     */

    public String getSource() {
        return source;
    }
    /**
     * Represent that object in string
     *
     * @return String that represents this {@link Request}
     * @see Request#start
     * @see Request#end
     * @see Request#search
     * @see Request#requestReception
     */
    @Override
    public String toString() {
        return "Request{" +
                "start=" + start +
                ", end=" + end +
                ", search=" + search +
                ", requestReception=" + requestReception +
                '}';
    }
}
