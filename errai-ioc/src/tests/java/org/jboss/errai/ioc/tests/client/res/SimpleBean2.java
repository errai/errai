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

package org.jboss.errai.ioc.tests.client.res;

import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * User: christopherbrock
 * Date: 15-Aug-2010
 * Time: 8:26:12 PM
 */
@EntryPoint
public class SimpleBean2 {
    public static SimpleBean2 TEST_INSTANCE;

    @Inject
    private FooService svc;

    public String getMessage() {
        return svc.getMessage().toUpperCase();
    }

    public FooService getSvc() {
        return svc;
    }

    public void setSvc(FooService svc) {
        this.svc = svc;
    }

    @PostConstruct
    public void doPostConstruct() {
        TEST_INSTANCE = this;
    }
}
