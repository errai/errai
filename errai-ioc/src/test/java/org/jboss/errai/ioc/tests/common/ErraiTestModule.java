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

package org.jboss.errai.ioc.tests.common;

import org.jboss.errai.common.configuration.ErraiModule;
import org.jboss.errai.ioc.tests.wiring.client.res.AlternativeBeanA;
import org.jboss.errai.ioc.tests.wiring.client.res.BlacklistedBean;
import org.jboss.errai.ioc.tests.wiring.client.res.OverridingAltCommonInterfaceBImpl;
import org.jboss.errai.ioc.tests.wiring.client.res.sub.BlacklistedPackageBean;
import org.jboss.tests.errai.ioc.wiring.client.res.WhitelistedAndBlacklistedBean;
import org.jboss.tests.errai.ioc.wiring.client.res.WhitelistedBean;
import org.jboss.tests.errai.ioc.wiring.client.res.sub.WhitelistedPackageBean;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiModule(iocAlternatives = { AlternativeBeanA.class, OverridingAltCommonInterfaceBImpl.class },
             iocWhitelist = { WhitelistedBean.class,
                              WhitelistedAndBlacklistedBean.class,
                              WhitelistedPackageBean.class },
             iocBlacklist = { BlacklistedBean.class,
                              BlacklistedPackageBean.class,
                              WhitelistedAndBlacklistedBean.class,
                              WhitelistedPackageBean.class })
public class ErraiTestModule {
}