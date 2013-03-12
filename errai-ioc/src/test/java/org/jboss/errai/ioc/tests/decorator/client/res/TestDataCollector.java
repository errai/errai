package org.jboss.errai.ioc.tests.decorator.client.res;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class TestDataCollector {
  private static final Map<String, Integer> beforeInvoke = new HashMap<String, Integer>();
  private static final Map<String, Integer> afterInvoke = new HashMap<String, Integer>();

  public static void beforeInvoke(String a, Integer b) {
    beforeInvoke.put(a, b);
  }

  public static void afterInvoke(String a, Integer b) {
    afterInvoke.put(a, b);
  }

  public static Map<String, Integer> getBeforeInvoke() {
    return beforeInvoke;
  }

  public static Map<String, Integer> getAfterInvoke() {
    return afterInvoke;
  }
}
