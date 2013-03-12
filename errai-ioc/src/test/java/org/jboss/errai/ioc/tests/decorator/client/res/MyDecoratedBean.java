package org.jboss.errai.ioc.tests.decorator.client.res;


import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
@Singleton
public class MyDecoratedBean {
  private final Map<String, Integer> testMap = new HashMap<String, Integer>();

  @LogCall
  public void someMethod(final String text, final Integer blah) {
    testMap.put(text, blah);
  }

  public Map<String, Integer> getTestMap() {
    return testMap;
  }
}
