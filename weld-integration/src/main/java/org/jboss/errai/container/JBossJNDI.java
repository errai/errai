/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.container;

import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

import org.jboss.util.naming.NonSerializableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple helper that relies on {@link NonSerializableFactory}
 * 
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 29, 2010
 */
class JBossJNDI {

  private static final Logger log = LoggerFactory.getLogger("Errai");

  public static void rebind(String jndiName, Object service) {
    try {
      InitialContext rootCtx = new InitialContext();
      Name fullName = rootCtx.getNameParser("").parse(jndiName);
      log.info("Bound to " + fullName);
      NonSerializableFactory.rebind(fullName, service, true);
    } catch (NamingException e) {
      throw new RuntimeException("Failed to bind " + service, e);
    }
  }

  public static void unbind(String jndiName) {
    try {
      InitialContext rootCtx = new InitialContext();
      rootCtx.unbind(jndiName);
      NonSerializableFactory.unbind(jndiName);
    } catch (NamingException e) {
      log.error("Failed to unbind map", e);
    }
  }
}
