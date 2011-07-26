/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.enterprise.context;

import javax.enterprise.context.NormalScope;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>Specifies that a bean is session scoped.</p>
 * <p/>
 * <p>The session scope is active:</p>
 * <p/>
 * <ul>
 * <li>during the <tt>service()</tt> method of any servlet
 * in the web application, during the doFilter() method of
 * any servlet filter and when the container calls any
 * <tt>HttpSessionListener</tt>, <tt>AsyncListener</tt> or
 * <tt>ServletRequestListener</tt>.</li>
 * </ul>
 * <p/>
 * <p>The session context is shared between all servlet
 * requests that occur in the same HTTP session. The session
 * context is destroyed when the HTTPSession times out,
 * after all <tt>HttpSessionListeners</tt> have been called,
 * and at the very end of any request in which
 * <tt>invalidate()</tt> was called, after all filters and
 * <tt>ServletRequestListeners</tt> have been called.</p>
 *
 * @author Gavin King
 * @author Pete Muir
 */

@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
@Documented
@NormalScope(passivating = true)
@Inherited
public @interface SessionScoped {
}
