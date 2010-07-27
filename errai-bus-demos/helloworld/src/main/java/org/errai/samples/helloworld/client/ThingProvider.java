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

package org.errai.samples.helloworld.client;

import com.google.gwt.user.client.Window;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;

/**
 * User: christopherbrock
 * Date: 27-Jul-2010
 * Time: 3:26:55 PM
 */

public class ThingProvider implements ContextualTypeProvider<Thing> {
    public Thing provide(final Class... typeargs) {
        return new Thing() {
            public void doThing(Object o) {
                Window.alert("Weeee! " + typeargs[0]);
            }
        };
    }
}
