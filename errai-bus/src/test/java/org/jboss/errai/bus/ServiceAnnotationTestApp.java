/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus;

import org.jboss.errai.bus.client.tests.support.BuilderEntity;
import org.jboss.errai.bus.client.tests.support.pkg.PortableType1;
import org.jboss.errai.bus.client.tests.support.pkg.subpkg.NonSerializable;
import org.jboss.errai.common.configuration.ErraiApp;
import org.jboss.errai.common.configuration.ErraiModule;
import org.jboss.errai.marshalling.ErraiMarshallingModule;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiApp(gwtModuleName = "org.jboss.errai.bus.ServiceAnnotationTestModule",
          local = true,
          modules = { ErraiMarshallingModule.class, ErraiBusModule.class, ServiceAnnotationTestApp.class })
@ErraiModule(serializableTypes = { PortableType1.class },
             nonSerializableTypes = { NonSerializable.class, BuilderEntity.NonPortableNestedClass.class })
public class ServiceAnnotationTestApp {
}
