package org.jboss.errai.cdi.injection.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mike Brock
 */
public class PostConstructTestUtil {
  private static final List<String> order = new ArrayList<String>();

  public static void reset() {
    order.clear();
  }

  public static void record(final String fired) {
    order.add(fired);
  }

  public static List<String> getOrderOfFiring() {
    return Collections.unmodifiableList(order);
  }
}
