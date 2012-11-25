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

package org.jboss.errai.ioc.rebind.ioc.extension;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class Rule {
  public static List<RuleDef> before(Collection<Class<? extends Annotation>>... annos) {
    List<RuleDef> rules = new ArrayList<RuleDef>(10);
    for (Collection<Class<? extends Annotation>> a : annos) {
      for (Class<? extends Annotation> cls : a) {
        rules.add(new RuleDef(cls, RelativeOrder.Before));
      }
    }

    return rules;
  }

  public static List<RuleDef> before(Class<? extends Annotation>... annos) {
    return before(Arrays.asList(annos));
  }

  public static List<RuleDef> after(Collection<Class<? extends Annotation>>... annos) {
    List<RuleDef> rules = new ArrayList<RuleDef>(10);
    for (Collection<Class<? extends Annotation>> a : annos) {
      for (Class<? extends Annotation> cls : a) {
        rules.add(new RuleDef(cls, RelativeOrder.After));
      }
    }

    return rules;
  }

  public static List<RuleDef> after(Class<? extends Annotation>... annos) {
    return after(Arrays.asList(annos));
  }
}
