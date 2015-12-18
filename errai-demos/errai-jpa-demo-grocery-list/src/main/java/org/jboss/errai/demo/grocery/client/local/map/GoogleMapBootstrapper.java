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

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;

/**
 * Attempts to load the Google Maps JavaScript asynchronously, passes in the Maps API key, and delays app initialization until
 * the maps library is loaded and ready.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@EntryPoint
public class GoogleMapBootstrapper {

    private static boolean ready = false;

    // TODO not using InitVotes because of ERRAI-472
    private static final List<Runnable> runWhenReady = new ArrayList<Runnable>();

    /**
     * The Google Maps API key. Fill this in with your own key if you're going to deploy this app to a remotely-accessible
     * website. The default (empty string) only works when the app is loaded from localhost.
     */
    //private final String MAP_API_KEY = "";

    public GoogleMapBootstrapper() {
        boolean sensor = true;

        ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
        loadLibraries.add(LoadLibrary.PLACES);
        loadLibraries.add(LoadLibrary.GEOMETRY);
        // Other add-ons you may want to check out:
        // loadLibraries.add(LoadLibrary.ADSENSE);
        // loadLibraries.add(LoadLibrary.DRAWING);
        // loadLibraries.add(LoadLibrary.PANORAMIO);
        // loadLibraries.add(LoadLibrary.WEATHER);
        // loadLibraries.add(LoadLibrary.VISUALIZATION);

        Runnable onLoad = new Runnable() {
            @Override
            public void run() {
                System.out.println("GoogleMaps APIs loaded. Executing " + runWhenReady.size() + " deferred tasks.");
                ready = true;
                for (Runnable r : runWhenReady) {
                    r.run();
                }
                runWhenReady.clear();
            }
        };

        LoadApi.go(onLoad, loadLibraries, sensor);
    }

    /**
     * Executes the given runnable once all required Maps APIs are loaded.
     *
     * @param r The Runnable to run when the Maps API is ready. Must not be null.
     */
    public static void whenReady(Runnable r) {
        Assert.notNull(r);
        if (ready) {
            r.run();
        }
        else {
            runWhenReady.add(r);
        }
    }
}
