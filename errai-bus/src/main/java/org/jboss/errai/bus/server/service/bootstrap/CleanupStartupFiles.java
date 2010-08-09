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
package org.jboss.errai.bus.server.service.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cleanup monitoring and other startup files.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 3, 2010
 */
@Deprecated
class CleanupStartupFiles implements BootstrapExecution {
    public static final Logger log = LoggerFactory.getLogger(CleanupStartupFiles.class);

    public void execute(BootstrapContext context) {
        try {
            //ConfigUtil.cleanupStartupTempFiles();
        } catch (Exception e) {
            log.error("Failed to clean startup files, ignore...");
        }
    }
}
