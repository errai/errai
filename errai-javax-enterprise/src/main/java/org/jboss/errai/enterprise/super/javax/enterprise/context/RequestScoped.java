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
 * <p>Specifies that a bean is request scoped.</p>
 * <p/>
 * <p>The request scope is active:</p>
 * <p/>
 * <ul>
 * <li>during the <tt>service()</tt> method of any servlet in the web
 * application, during the <tt>doFilter()</tt> method of any servlet
 * filter and when the container calls any <tt>ServletRequestListener</tt>
 * or <tt>AsyncListener</tt>,</li>
 * <li>during any Java EE web service invocation,</li>
 * <li>during any remote method invocation of any EJB, during
 * any asynchronous method invocation of any EJB, during any
 * call to an EJB timeout method and during message delivery
 * to any EJB message-driven bean, and</li>
 * <li>during any message delivery to a MessageListener for
 * a JMS topic or queue obtained from the Java EE component
 * environment.</li>
 * </ul>
 * <p/>
 * <p>The request context is destroyed:</p>
 * <p/>
 * <ul>
 * <li>at the end of the servlet request, after the <tt>service()</tt>
 * method, all <tt>doFilter()</tt> methods, and all <tt>requestDestroyed()</tt>
 * and <tt>onComplete()</tt> notifications return,</li>
 * <li>after the web service invocation completes,</li>
 * <li>after the EJB remote method invocation, asynchronous
 * method invocation, timeout or message delivery completes, or</li>
 * <li>after the message delivery to the <tt>MessageListener</tt>
 * completes.</li>
 * </ul>
 *
 * @author Gavin King
 * @author Pete Muir
 */

@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
@Documented
@NormalScope
@Inherited
public @interface RequestScoped {
}
