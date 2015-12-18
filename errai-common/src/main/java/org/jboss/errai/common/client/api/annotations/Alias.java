/*
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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Mark annotation as an alias. This will recursively add the annotations that are on the annotation for example:
 * <pre>
 * {@code @Alias}
 * {@code @Inject}
 * {@code @Bound}
 * {@code @DataField}
 *  public {@code @interface} UiProperty {
 *  }
 * </pre>
 *
 * The Alias in the example above will make sure that UiProperty is the same as specifying Inject, Bound and
 * DataField on a field.
 *
 * @author edewit@redhat.com
 */
@Deprecated
@Target(TYPE)
@Retention(RUNTIME)
public @interface Alias {
}
