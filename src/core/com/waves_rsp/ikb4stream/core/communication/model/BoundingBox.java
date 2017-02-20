package com.waves_rsp.ikb4stream.core.communication.model;

import com.waves_rsp.ikb4stream.core.model.LatLong;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * BoundingBox class represents an area by coordinates
 */
public class BoundingBox {
    private final LatLong[] latLongs;

    /**
     * The constructor of BoundingBox class
     * @param points an array of LatLong
     * @throws NullPointerException if {@param points} is null
     * @throws IllegalArgumentException if {@param points} has invalid size
     */
    public BoundingBox(LatLong[] points) {
        Objects.requireNonNull(points);
        Arrays.stream(points).forEach(Objects::requireNonNull);
        if(points.length < 1) {
            throw new IllegalArgumentException("We need at least 1 point in bounding box ");
        } else if (points.length > 1) {
            if(points.length < 5 || points[0] != points[points.length - 1]) { throw new IllegalArgumentException("BoundingBox is not closed."); }
        }
        this.latLongs = points;
    }

    /**
     *
     * @return a LatLong
     */
    public LatLong[] getLatLongs() {
        return latLongs;
    }

    @Override
    public String toString() {
        return '[' + Arrays.stream(latLongs).map(LatLong::toString).collect(Collectors.joining("{", ",", "}")) + "]";
    }
}
