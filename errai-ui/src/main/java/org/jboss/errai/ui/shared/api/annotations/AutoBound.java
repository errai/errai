/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.ui.shared.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.errai.databinding.client.api.DataBinder;

import com.google.gwt.user.client.ui.Composite;

/**
 * This annotation may only be used in subclasses of {@link Composite} that have been annotated with
 * {@link Templated}, or in a super-class of said {@link Composite} types. It is only meaningful on
 * an injected {@link DataBinder} field or constructor/method parameter.
 * <p>
 * Indicates that the annotated {@link DataBinder} is used to automatically bind all enclosing
 * {@link DataField}s to properties of the corresponding data model (the model instance associated
 * with the data binder instance). The enclosing data fields are all fields annotated with both
 * {@link Bound} and {@link DataField} of the class that defines the {@link DataBinder} and all its
 * super classes.
 * <p>
 * There can only be one auto bound {@link DataBinder} per {@link Templated} class.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoBound {

}
