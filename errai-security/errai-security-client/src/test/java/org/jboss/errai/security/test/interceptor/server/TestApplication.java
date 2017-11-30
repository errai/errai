/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jboss.errai.security.test.interceptor.server;

import com.google.gwt.dev.util.collect.HashSet;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Set;

/**
 * TODO: Workaround for Jetty 8 (bundled with GWT 2.8-beta1). This should not be needed once upgraded to GWT 2.8.RC1,
 * which uses Jetty 9 (See https://docs.jboss.org/resteasy/docs/3.0.16.Final/userguide/html/Installation_Configuration.html#d4e113
 * for more info how to setup standalone RestEasy 3.x in Servlet 3.0+ containers).
 */
@ApplicationPath("/")
public class TestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<>(Arrays.asList(
                SecureRestServiceImpl.class
        ));
    }
}
