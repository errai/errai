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

package org.jboss.errai.demo.grocery.client.shared;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.jboss.errai.demo.grocery.client.shared.qual.New;
import org.jboss.errai.demo.grocery.client.shared.qual.Removed;
import org.jboss.errai.demo.grocery.client.shared.qual.Updated;
import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * A translator that receives JPA entity lifecycle events and refires them as CDI events.
 * <p>
 * Ideally there would be no need for this class: Errai's EntityManager could just fire these qualified CDI events when it's
 * firing the less-usable-from-CDI JPA events.
 *
 * @author jfuerth
 */
@EntryPoint
public class EventTranslator {

    private static EventTranslator INSTANCE;

    @PostConstruct
    private void initInstance() {
        INSTANCE = this;
    }

    // ========= Item ==========

    @Inject
    private @New Event<Item> newItemEvent;

    @Inject
    private @Updated Event<Item> updatedItemEvent;

    @Inject
    private @Removed Event<Item> removedItemEvent;

    void fireNewItemEvent(Item i) {
        newItemEvent.fire(i);
    }

    void fireUpdatedItemEvent(Item i) {
        updatedItemEvent.fire(i);
    }

    void fireRemovedItemEvent(Item i) {
        removedItemEvent.fire(i);
    }

    public static class ItemLifecycleListener {
        @PostPersist
        private void onPostPersist(Item i) {
            EventTranslator.INSTANCE.fireNewItemEvent(i);
        }

        @PostUpdate
        private void onPostUpdate(Item i) {
            EventTranslator.INSTANCE.fireUpdatedItemEvent(i);
        }

        @PostRemove
        private void onPostRemove(Item i) {
            EventTranslator.INSTANCE.fireRemovedItemEvent(i);
        }
    }
}
