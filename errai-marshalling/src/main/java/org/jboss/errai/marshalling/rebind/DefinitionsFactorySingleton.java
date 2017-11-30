/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.marshalling.rebind;

import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.config.MetaClassFinder;

/**
 * @author Mike Brock
 */
public class DefinitionsFactorySingleton {

  private static DefinitionsFactory factory;

  public static synchronized DefinitionsFactory get(final ErraiConfiguration erraiConfiguration,
          final MetaClassFinder metaClassFinder) {

    if (factory == null) {
      try {
        factory = newInstance(erraiConfiguration, metaClassFinder);
      } catch (Exception e) {
        // This exception will probably be swallowed by the VM, which is why we print the stack trace here.
        System.err.println("Failed to bootstrap errai marshalling system!");
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }

    return factory;
  }

  public static void reset() {
    factory = null;
  }

  public static DefinitionsFactory newInstance(final ErraiConfiguration erraiConfiguration,
          final MetaClassFinder metaClassFinder) {
    return new DefinitionsFactoryImpl(erraiConfiguration, metaClassFinder);
  }
}
