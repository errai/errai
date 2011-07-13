package org.jboss.errai.ioc.rebind;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class Rule {
  public static List<ProcessorFactory.RuleDef> before(Class<? extends Annotation>... annos) {

    List<ProcessorFactory.RuleDef> rules = new ArrayList<ProcessorFactory.RuleDef>();
    for (Class<? extends Annotation> cls : annos) {
      rules.add(new ProcessorFactory.RuleDef(cls, RelativeOrder.Before));
    }

    return rules;
  }

  public static List<ProcessorFactory.RuleDef> after(Class<? extends Annotation>... annos) {

    List<ProcessorFactory.RuleDef> rules = new ArrayList<ProcessorFactory.RuleDef>();
    for (Class<? extends Annotation> cls : annos) {
      rules.add(new ProcessorFactory.RuleDef(cls, RelativeOrder.After));
    }

    return rules;
  }
}
