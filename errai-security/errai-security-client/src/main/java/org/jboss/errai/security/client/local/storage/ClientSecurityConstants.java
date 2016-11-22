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

/**
 * Provides accessors to the JSON security context and the contained user JSON.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class ClientSecurityConstants {

    public static native JavaScriptObject getSecurityContextObject()/*-{
      return $wnd.errai_security_context;
    }-*/;

    public static native JavaScriptObject getSecurityContextUserObject()/*-{
      return $wnd.errai_security_context.user;
    }-*/;

}
