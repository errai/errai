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

package org.jboss.errai.demo.grocery.client.local.producer;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.geometrylib.SphericalUtils;
import org.jboss.errai.demo.grocery.client.local.map.LocationProvider;
import org.jboss.errai.demo.grocery.client.shared.Store;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
@ApplicationScoped
public class StoreListProducer {

    @Inject
    private EntityManager entityManager;

    @Inject
    private LocationProvider locationProvider;

    @Inject
    protected Event<StoreChangedEvent> storeListChanged;

    // TODO eliminate this after making a bridge from JPA lifecycle events to CDI events
    private static StoreListProducer INSTANCE;
    private List<Store> stores;
    private LatLng here;

    @PostConstruct
    private void initInstance() {
        INSTANCE = this;
        updateStoreList();
        locationProvider.watchPosition(new LocationProvider.LocationCallback() {
            @Override
            public void onSuccess(final LatLng here) {
                StoreListProducer.this.here = here;
                sortStoreListOnLocation();
                fireStoreChangeEvent();
            }
        });
    }

    private void sortStoreListOnLocation() {
        Collections.sort(stores, new Comparator<Store>() {
            @Override
            public int compare(Store one, Store two) {
                LatLng storeOneLocation = LatLng.newInstance(one.getLatitude(), one.getLongitude());
                LatLng storeTwoLocation = LatLng.newInstance(two.getLatitude(), two.getLongitude());

                double distanceOne = SphericalUtils.computeDistanceBetween(storeOneLocation, here);
                double distanceTwo = SphericalUtils.computeDistanceBetween(storeTwoLocation, here);

                return distanceOne < distanceTwo ? -1 : distanceOne == distanceTwo ? 0 : 1;
            }
        });
    }

    private void updateStoreList() {
        stores = entityManager.createNamedQuery("allStores", Store.class).getResultList();
        if (here != null) {
            sortStoreListOnLocation();
        }
    }

    @PreDestroy
    private void deInitInstance() {
        INSTANCE = null;
    }

    // TODO make a bridge from JPA lifecycle events to CDI events
    public static class StoreListener {
        @PostPersist
        @PostUpdate
        @PostRemove
        public void onStoreListChange(Store s) {
            if (INSTANCE != null) {
                INSTANCE.updateStoreList();
                INSTANCE.fireStoreChangeEvent();
            }
        }
    }

    private void fireStoreChangeEvent() {
        storeListChanged.fire(new StoreChangedEvent(stores));
    }

    public List<Store> getStoresSortedOnDistance() {
        return stores;
    }

    public static class StoreChangedEvent {
        private final List<Store> stores;

        public StoreChangedEvent(List<Store> stores) {
            this.stores = stores;
        }

        public List<Store> getStores() {
            return stores;
        }
    }
}
