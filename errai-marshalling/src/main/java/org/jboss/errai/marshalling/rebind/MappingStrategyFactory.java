/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.marshalling.rebind;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.codegen.framework.meta.MetaParameter;
import org.jboss.errai.marshalling.client.api.exceptions.MarshallingException;
import org.jboss.errai.marshalling.rebind.api.MappingContext;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.api.impl.DefaultJavaMappingStrategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MappingStrategyFactory {
  private static final Map<MetaClass, Class<? extends MappingStrategy>> STRATEGIES
          = new HashMap<MetaClass, Class<? extends MappingStrategy>>();


  static MappingStrategy createStrategy(final MappingContext context, final MetaClass clazz) {
    if (STRATEGIES.containsKey(clazz)) {
      return loadStrategy(STRATEGIES.get(clazz), context, clazz);
    }
    else {
      return defaultStrategy(context, clazz);
    }

  }

  private static MappingStrategy loadStrategy(final Class<? extends MappingStrategy> strategy,
                                              final MappingContext context, final MetaClass clazz) {


    Constructor[] constructors = strategy.getConstructors();
    if (constructors.length != 1) {
      throw new MarshallingException("a MappingStrategy should have exactly one constructor");
    }

    Constructor<? extends MappingStrategy> constructor = constructors[0];
    Class<?>[] parms = constructor.getParameterTypes();

    List<Object> callParameters = new ArrayList<Object>(parms.length);

    for (Class<?> parm : parms) {
      if (MetaClass.class.isAssignableFrom(parm)) {
        callParameters.add(clazz);
      }
      else if (MappingContext.class.isAssignableFrom(parm)) {
        callParameters.add(context);
      }
      else {
        throw new MarshallingException("unrecognized constuctor parameter type"
                + parm.getName() + "; for: " + strategy.getName());
      }
    }

    try {
      return constructor.newInstance(callParameters.toArray(new Object[callParameters.size()]));
    }
    catch (Throwable e) {
      throw new MarshallingException("could not instantiate mapping strategy", e);
    }
  }


  private static MappingStrategy defaultStrategy(final MappingContext context, final MetaClass clazz) {
    return new DefaultJavaMappingStrategy(context, clazz);
  }
}
