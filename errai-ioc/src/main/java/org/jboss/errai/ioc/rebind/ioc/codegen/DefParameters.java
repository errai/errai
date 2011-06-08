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

package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaConstructor;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefParameters extends AbstractStatement {
  private List<Parameter> parameters;

  public DefParameters(List<Parameter> parameters) {
    this.parameters = parameters;
  }

  public static DefParameters from(MetaMethod method) {
    List<Parameter> parameters = new ArrayList<Parameter>();
    for (MetaParameter parm : method.getParameters()) {
      parameters.add(Parameter.of(parm.getType(), parm.getName()));
    }
    return new DefParameters(parameters);
  }

  public static DefParameters from(MetaConstructor constructor) {
    List<Parameter> parameters = new ArrayList<Parameter>();
    for (MetaParameter parm : constructor.getParameters()) {
      parameters.add(Parameter.of(parm.getType(), parm.getName()));
    }
    return new DefParameters(parameters);
  }

  public static DefParameters fromStatements(Parameter... statements) {
    return new DefParameters(Arrays.asList(statements));
  }

  public static CallParameters none() {
    return new CallParameters(Collections.<Statement>emptyList());
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  public String generate(Context context) {
    StringBuilder buf = new StringBuilder("(");
    for (int i = 0; i < parameters.size(); i++) {
      buf.append(parameters.get(i).generate(context));

      if (i + 1 < parameters.size()) {
        buf.append(", ");
      }
    }
    return buf.append(")").toString();
  }
}
