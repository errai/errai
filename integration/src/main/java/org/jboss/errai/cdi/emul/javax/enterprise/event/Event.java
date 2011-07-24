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

package javax.enterprise.event;

import java.lang.annotation.Annotation;

import javax.enterprise.util.TypeLiteral;

/**
 * <p>Allows the application to fire events of a particular type.</p>
 * <p/>
 * <p>Beans fire events via an instance of the <tt>Event</tt>
 * interface, which may be injected:</p>
 * <p/>
 * <pre>
 * &#064;Inject &#064;Any Event&lt;LoggedInEvent&gt; loggedInEvent;
 * </pre>
 * <p/>
 * <p>The <tt>fire()</tt> method accepts an event object:</p>
 * <p/>
 * <pre>
 * public void login() {
 *    ...
 *    loggedInEvent.fire( new LoggedInEvent(user) );
 * }
 * </pre>
 * <p/>
 * <p>Any combination of qualifiers may be specified at the injection
 * point:</p>
 * <p/>
 * <pre>
 * &#064;Inject &#064;Admin Event&lt;LoggedInEvent&gt; adminLoggedInEvent;
 * </pre>
 * <p/>
 * <p>Or, the {@link javax.enterprise.inject.Any &#064;Any} qualifier may
 * be used, allowing the application to specify qualifiers dynamically:</p>
 * <p/>
 * <pre>
 * &#064;Inject &#064;Any Event&lt;LoggedInEvent&gt; loggedInEvent;
 * </pre>
 * <p/>
 * <p>For an injected <tt>Event</tt>:</p>
 * <p/>
 * <ul>
 * <li>the <em>specified type</em> is the type parameter specified at the
 * injection point, and</li>
 * <li>the <em>specified qualifiers</em> are the qualifiers specified at
 * the injection point.</li>
 * </ul>
 *
 * @param <T> the type of the event object
 * @author Gavin King
 * @author Pete Muir
 * @author David Allen
 */

public interface Event<T> {

  /**
   * <p>Fires an event with the specified qualifiers and notifies
   * observers.</p>
   *
   * @param event the event object
   * @throws IllegalArgumentException if the runtime type of the event object contains a type variable
   */
  public void fire(T event);

  /**
   * <p>Obtains a child <tt>Event</tt> for the given additional
   * required qualifiers.</p>
   *
   * @param qualifiers the additional specified qualifiers
   * @return the child <tt>Event</tt>
   * @throws IllegalArgumentException if passed two instances of the
   *                                  same qualifier type, or an instance of an annotation that is not
   *                                  a qualifier type
   */
  public Event<T> select(Annotation... qualifiers);

  /**
   * <p>Obtains a child <tt>Event</tt> for the given required type and
   * additional required qualifiers.</p>
   *
   * @param <U>        the specified type
   * @param subtype    a {@link java.lang.Class} representing the specified type
   * @param qualifiers the additional specified qualifiers
   * @return the child <tt>Event</tt>
   * @throws IllegalArgumentException if passed two instances of the
   *                                  same qualifier type, or an instance of an annotation that is not
   *                                  a qualifier type
   */
  public <U extends T> Event<U> select(Class<U> subtype, Annotation... qualifiers);
}
