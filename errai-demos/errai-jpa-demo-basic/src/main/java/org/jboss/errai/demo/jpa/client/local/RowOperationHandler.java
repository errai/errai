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

package org.jboss.errai.demo.jpa.client.local;

/**
 * Callback interface for registering operation handlers for various operations on the {@link AlbumTable}.
 *
 * @param <T> The model type represented by the row.
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface RowOperationHandler<T> {

    /**
     * Handle the event by doing whatever you want!
     */
    void handle(T modelObject);

}
