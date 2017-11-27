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

package org.jboss.errai.databinding;

import org.jboss.errai.common.configuration.ErraiApp;
import org.jboss.errai.common.configuration.ErraiModule;
import org.jboss.errai.databinding.client.TestModelWithoutBindableAnnotation;
import org.jboss.errai.databinding.client.nonbindablepkg.TestModelInNonBindablePkg;
import org.jboss.errai.databinding.client.scan.TestModelBindable;
import org.jboss.errai.databinding.client.scan.TestModelWithoutBindableA;
import org.jboss.errai.databinding.client.scan.TestModelWithoutBindableB;
import org.jboss.errai.databinding.client.scan.TestModelWithoutBindableC;
import org.jboss.errai.ioc.ErraiIocModule;
import org.jboss.errai.marshalling.ErraiMarshallingModule;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiApp(gwtModuleName = "org.jboss.errai.databinding.DataBindingTestModule",
          modules = { ErraiDataBindingTestModule.class,
                      ErraiMarshallingModule.class,
                      ErraiDataBindingModule.class,
                      ErraiIocModule.class })
@ErraiModule(bindableTypes = { TestModelBindable.class,
                               TestModelWithoutBindableA.class,
                               TestModelWithoutBindableB.class,
                               TestModelWithoutBindableC.class,
                               TestModelWithoutBindableAnnotation.class },
             nonBindableTypes = TestModelInNonBindablePkg.class)
public class ErraiDataBindingTestModule {
}
