/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.client.local.storage;

import org.jboss.errai.security.shared.api.SecurityConstants;

import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Provides accessors to the JSON security context and the contained user JSON.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@JsType(isNative = true)
public abstract class ClientSecurityConstants {

    @JsProperty(namespace = JsPackage.GLOBAL, name = SecurityConstants.ERRAI_SECURITY_CONTEXT_DICTIONARY)
    public static JavaScriptObject securityContextObject;

    @JsProperty(namespace = SecurityConstants.ERRAI_SECURITY_CONTEXT_DICTIONARY, name = SecurityConstants.DICTIONARY_USER)
    public static JavaScriptObject securityContextUserObject;

}
