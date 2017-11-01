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

package org.jboss.errai.common.apt.localapps;

import org.jboss.errai.common.apt.localapps.localapp1.module1.TestModule1;
import org.jboss.errai.common.apt.localapps.localapp2.module2.TestModule2;
import org.jboss.errai.common.configuration.ErraiApp;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiApp(gwtModuleName = "", local = true, modules = { TestModule1.class, TestModule2.class })
public class TestLocalAppWithTwoSubErraiApps {
}
