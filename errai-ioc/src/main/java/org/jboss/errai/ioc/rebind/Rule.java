package org.jboss.errai.ioc.rebind;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class Rule {
  public static List<IOCProcessorFactory.RuleDef> before(Class<? extends Annotation>... annos) {

    List<IOCProcessorFactory.RuleDef> rules = new ArrayList<IOCProcessorFactory.RuleDef>();
    for (Class<? extends Annotation> cls : annos) {
      rules.add(new IOCProcessorFactory.RuleDef(cls, RelativeOrder.Before));
    }

    return rules;
  }

  public static List<IOCProcessorFactory.RuleDef> after(Class<? extends Annotation>... annos) {

    List<IOCProcessorFactory.RuleDef> rules = new ArrayList<IOCProcessorFactory.RuleDef>();
    for (Class<? extends Annotation> cls : annos) {
      rules.add(new IOCProcessorFactory.RuleDef(cls, RelativeOrder.After));
    }

    return rules;
  }
}
