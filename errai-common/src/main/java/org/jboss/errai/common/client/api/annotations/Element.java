/**
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.common.client.api.annotations;

import jsinterop.annotations.JsType;

/**
 * Marks a native {@link JsType} as a wrapper for a DOM element. {@link Element#value()} are the tag names of the
 * element. Errai IoC will generate code calling {@code document.createElement} for injecting instances of types
 * annotated with {@linkplain Element}. When multiple tag names are specified, injection sites must use {@code javax.inject.Named} to
 * remove ambiguity at injection sites.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public @interface Element {

  String[] value();

}
