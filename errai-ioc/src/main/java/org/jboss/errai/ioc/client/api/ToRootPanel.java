/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.client.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An {@link com.google.gwt.user.client.ui.Panel} object annotated with this annotation will be automatically added
 * to the RootPanel or the document root of the DOM in the application. It generally should only be used on the main
 * outer container, as each annotated class will simply be passed to <tt>RootPanel.get().add(...)</tt>
 *
 * Example:
 * <pre><code>
 * @ToRootPanel
 * public class MyRootPanel extends VerticalPanel {
 *      public MyRootPanel() {
 *          // setup panel ...
 *      }
 * }
 * </code></pre>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ToRootPanel {
}
