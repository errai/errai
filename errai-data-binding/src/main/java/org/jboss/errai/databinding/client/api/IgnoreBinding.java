/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.databinding.client.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.errai.databinding.rebind.BindableProxyGenerator;

/**
 * Indicates that a property will not be bindable to the to UI components 
 * or trigger model update checks wen put on non-accessor methods
 * 
 * When put on a property getter, causes Errai to ignore that accessors. Property will not be bindable. 
 * When put on a property setter, causes the property to become read-only.
 * When put on a non-accessor, causes Errai not to proxy that method. Normally Errai proxies non-accessor 
 * methods so that we can check for model updates that happen outside of getter and setter calls.
 * 
 * @author Sašo Petrovič <saso.petrovic@gmail.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface IgnoreBinding {

}
