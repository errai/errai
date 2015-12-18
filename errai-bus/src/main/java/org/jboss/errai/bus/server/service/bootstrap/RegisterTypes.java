/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.service.bootstrap;

import org.mvel2.ConversionHandler;
import org.mvel2.DataConversion;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

class RegisterTypes implements BootstrapExecution {
  public void execute(BootstrapContext context) {
    DataConversion.addConversionHandler(Queue.class, new ConversionHandler() {
      @Override
      public Object convertFrom(Object in) {
        if (in instanceof Collection) {
          return new LinkedList((Collection) in);
        }
        return null;
      }

      @Override
      public boolean canConvertFrom(Class cls) {
        return Collection.class.isAssignableFrom(cls);
      }
    });
  }
}
