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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MapCreator implements Statement {
  private Map<Object, Object> modelMap;

  public MapCreator(Map<Object, Object> modelMap) {
    this.modelMap = modelMap;
  }

  public String generate(Context context) {
    StringBuffer buf = new StringBuffer();
    ContextBuilder ctx = ContextBuilder.create(context);
    ctx.declareVariable("map", HashMap.class)
            .initializeWith(HashMap.class);

    buf.append(ctx.toJavaString());

    for (Map.Entry<Object, Object> entry : modelMap.entrySet()) {
      //buf.append("map.put(")
    }

    return buf.toString();
  }

  public MetaClass getType() {
    return null;
  }
}
