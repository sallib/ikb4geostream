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

package com.waves_rsp.ikb4stream.communication.web;

import com.waves_rsp.ikb4stream.core.communication.DatabaseReaderCallback;
import com.waves_rsp.ikb4stream.core.communication.model.IDatabaseReaderB;
import com.waves_rsp.ikb4stream.core.communication.model.BoundingBox;
import com.waves_rsp.ikb4stream.core.communication.model.IDatabaseReader;
import com.waves_rsp.ikb4stream.core.communication.model.Request;
import com.waves_rsp.ikb4stream.core.util.Geocoder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * This server relies on Vertx, to handle the REST requests. It is instanciated by the web communication connector.
 *
 * @author ikb4stream
 * @version 1.0
 * @see io.vertx.core.Verticle
 * @see io.vertx.core.AbstractVerticle
 */
public class VertxServer extends AbstractVerticle {
    /**
     * Logger used to log all information in this module
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VertxServer.class);
    /**
     * {@link IDatabaseReaderB} object to read data from database
     *
     * @see VertxServer#getEvent(Request, DatabaseReaderCallback)
     */
    private final IDatabaseReader databaseReader = WebCommunication.databaseReader;

    /**
     * Server starting behaviour
     *
     * @param fut Future that handles the start status
     * @throws NullPointerException if fut is null
     */
    @Override
    public void start(Future<Void> fut) {
        Objects.requireNonNull(fut);
        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedHeader("X-PINGARUNER")
                .allowedHeader("Content-Type")
        );

        router.route("/anomaly*").handler(BodyHandler.create()); // enable reading of request's body
        router.get("/anomaly").handler(this::getAnomalies);
        router.post("/anomaly").handler(this::getAnomalies);

        router.route("/delete*").handler(BodyHandler.create()); // enable reading of request's body
        router.get("/delete").handler(this::deleteEventById);
        router.post("/delete").handler(this::deleteEventById);
        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        config().getInteger("http.port", 8081), // default value: 8081
                        result -> {
                            if (result.succeeded()) {
                                fut.complete();
                            } else {
                                fut.fail(result.cause());
                            }
                        }
                );
        LOGGER.info("VertxServer started");
    }


    private void deleteEventById(RoutingContext rc){
        String objectID;
        try {
            LOGGER.info("Received web request: {}", rc.getBodyAsString());
            objectID = rc.getBodyAsString();
            if (objectID == null) {
                rc.response()
                        .setStatusCode(400)
                        .putHeader("Content-type", "application/json;charset:utf-8")
                        .end("{\"error\": \"Invalid address\"}");
                return;
            }
        } catch (DecodeException | NullPointerException e) {
            LOGGER.info("Received an invalid format request : {} ", e.getMessage());
            LOGGER.debug("DecodeException: {}", e);
            rc.fail(400);
            return;
        }
        LOGGER.info("Request : {}", objectID);
        LOGGER.info("rc= {}", rc);
        databaseReader.deleteEvent(objectID);
        rc.response().end("true");

    }
    /**
     * Reads a request from a routing context, and attach the response to it. It requests the database
     * with DatabaseReader.
     *
     * @param rc {@link RoutingContext}, which contains the request, and the response
     * @throws NullPointerException if rc is null
     */
    private void getAnomalies(RoutingContext rc) {
        Request request;
        try {
            LOGGER.info("Received web request: {}", rc.getBodyAsJson());
            request = parseRequest(rc.getBodyAsJson());
            if (request == null) {
                rc.response()
                        .setStatusCode(400)
                        .putHeader("Content-type", "application/json;charset:utf-8")
                        .end("{\"error\": \"Invalid address\"}");
                return;
            }
        } catch (DecodeException | NullPointerException e) {
            LOGGER.info("Received an invalid format request : {} ", e.getMessage());
            LOGGER.debug("DecodeException: {}", e);
            rc.fail(400);
            return;
        }
        LOGGER.info("Request : {}", request);
        LOGGER.info("rc= {}", rc);
        rc.response().putHeader("content-type", "application/json");

        getEvent(request, (t, result) -> {
            if (t != null) {
                LOGGER.error("DatabaseReader error: " + t.getMessage());
                return;
            }
            LOGGER.info("Found events: {}", result);
            JsonObject response = new JsonObject("{\"events\":" + result + "}");
            rc.response().end(response.encode());
        });
    }

    /**
     * Convert a request from Json to Java object
     *
     * @param jsonRequest {@link JsonObject} json formatted request
     * @return {@link Request}
     * @throws NullPointerException if jsonRequest is null
     */
    private Request parseRequest(JsonObject jsonRequest) {
        Objects.requireNonNull(jsonRequest);
        Date start = new Date(jsonRequest.getLong("start"));
        Date end = new Date(jsonRequest.getLong("end"));
        String search = jsonRequest.getString("search");
        String location = jsonRequest.getString("location");
        String source = jsonRequest.getString("source");
        if (location.isEmpty() || location == "") {
            return new Request(start, end, search, source, Date.from(Instant.now()));
        } else {
            Geocoder geocoder = Geocoder.geocode(location);
            if (geocoder.getLatLong() == null) {
                LOGGER.warn("Can't geocode this address {}", location);
                return null;
            }
            return new Request(start, end, search, source, new BoundingBox(geocoder.getBbox()), Date.from(Instant.now()));
        }

    }

    /**
     * Retrieve an event from database
     *
     * @param request                {@link Request} the user web request
     * @param databaseReaderCallback {@link DatabaseReaderCallback} called when request finished
     * @see VertxServer#databaseReader
     */
    private void getEvent(Request request, DatabaseReaderCallback databaseReaderCallback) {
        databaseReader.getEvent(request, databaseReaderCallback);
    }
}