/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.tests.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.ioc.client.api.Bootstrapper;
import org.jboss.errai.ioc.tests.client.res.SimpleBean;
import org.jboss.errai.ioc.tests.client.res.SimpleBean2;

/**
 * User: christopherbrock
 * Date: 14-Aug-2010
 * Time: 6:49:53 PM
 */
public class BasicIOCTest extends GWTTestCase {
    @Override
    public String getModuleName() {
        return "org.jboss.errai.ioc.tests.IOCTests";
    }

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();

        Bootstrapper bootstrapper = GWT.create(Bootstrapper.class);
        bootstrapper.bootstrapContainer();
    }

    public void testBasicInjectionScenarios() {

        SimpleBean simpleBean = SimpleBean.TEST_INSTANCE;
        assertNotNull(simpleBean);

        assertEquals(ErraiBus.get(), simpleBean.getBus());
        assertEquals(ErraiBus.get(), simpleBean.getBus2());
        assertEquals(ErraiBus.get(), simpleBean.getBus3());
        assertEquals(ErraiBus.get(), simpleBean.getBus4());

        assertNotNull(simpleBean.getDispatcher());
        assertNotNull(simpleBean.getDispatcher2());
        assertNotNull(simpleBean.getDispatcher3());
        assertNotNull(simpleBean.getDispatcher4());
    }

    public void testInjectionFromProvider() {
        SimpleBean2 simpleBean2 = SimpleBean2.TEST_INSTANCE;
        assertEquals("FOO", simpleBean2.getMessage());
    }

    public void testInjectionFromProviderContextual() {
        SimpleBean2 simpleBean2 = SimpleBean2.TEST_INSTANCE;
        assertEquals("FOO", simpleBean2.getMessage());
        assertEquals("java.lang.String", simpleBean2.getbSvc().get());
    }
}
