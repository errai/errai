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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;

/**
 * This annotation may only be used in subclasses of {@link Composite} that has been annotated with {@link Templated},
 * or in a super-class of said {@link Composite} types.
 * <p>
 * Indicates that a given field, method parameter, or constructor parameter in a {@link Templated} {@link Composite}
 * component corresponds to the HTML template {@link Element} with <code>data-field</code> attribute matching the
 * annotated element.
 * <p>
 * By default, the name of the corresponding <code>data-field</code> will be defined by the name of the field, method
 * parameter, or constructor parameter name, but the name may also be overridden using the {@link #value()} attribute of
 * this annotation.
 * <p>
 * When used on a method or constructor parameter, this must be accompanied by a corresponding {@link Inject}
 * annotation; however, when used on a field, construction may be performed manually or via {@link Inject}.
 * 
 * <p>
 * <b>See also:</b> {@link Templated}, {@link EventHandler}, {@link Bound}
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Inherited
@Documented
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface DataField
{

   /**
    * Specify the name of the <code>data-field</code> in the corresponding HTML template, which the annotated element
    * represents.
    */
   String value() default "";

}
