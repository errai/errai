/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.demo.grocery.client.local.map;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.Callback;
import com.google.gwt.geolocation.client.Geolocation;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.PositionError;
import com.google.gwt.maps.client.base.LatLng;

import javax.inject.Singleton;

/**
 * @author edewit@redhat.com
 */
@Singleton
public class LocationProvider {

    public void getCurrentPosition(final LocationCallback callback) {
        GoogleMapBootstrapper.whenReady(new Runnable() {

            @Override
            public void run() {
                Geolocation geolocation = Geolocation.getIfSupported();
                if (geolocation != null) {
                    geolocation.getCurrentPosition(new Callback<Position, PositionError>() {

                        @Override
                        public void onSuccess(Position result) {
                            callback.onSuccess(convertResult(result));
                        }

                        @Override
                        public void onFailure(PositionError reason) {
                            // fall back to Google's IP Geolocation
                            callback.onSuccess(getIpBasedLocation());
                        }
                    });
                }
            }
        });
    }

    public void watchPosition(final LocationCallback callback) {
        GoogleMapBootstrapper.whenReady(new Runnable() {

            @Override
            public void run() {
                Geolocation geolocation = Geolocation.getIfSupported();
                if (geolocation != null) {
                    geolocation.watchPosition(new Callback<Position, PositionError>() {
                        @Override
                        public void onSuccess(Position result) {
                            callback.onSuccess(convertResult(result));
                        }

                        @Override
                        public void onFailure(PositionError reason) {
                        }
                    });
                }
            }
        });
    }

    private LatLng convertResult(Position result) {
        return LatLng.newInstance(result.getCoordinates().getLatitude(), result.getCoordinates().getLongitude());
    }

    /**
     * Returns Google's guess at the user's physical location based on their IP address.
     */
    private static LatLng getIpBasedLocation() {
        return LatLng.newInstance(
            AjaxLoader.getClientLocation().getLatitude(),
            AjaxLoader.getClientLocation().getLongitude());
    }

    public static abstract class LocationCallback implements Callback<LatLng, PositionError> {

        public abstract void onSuccess(LatLng result);

        @Override
        public void onFailure(PositionError reason) {
            throw new IllegalArgumentException("onFailure is not allowed only want success");
        }
    }
}
