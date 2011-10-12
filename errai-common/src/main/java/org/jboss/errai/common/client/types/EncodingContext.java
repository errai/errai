package org.jboss.errai.common.client.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class EncodingContext {  
  private HashMap<Object, Integer> refs;
  private int escapeMode;

  public boolean isEncoded(Object o) {
    return !(o instanceof  String || o instanceof Number || o instanceof Boolean || o instanceof Character)
            && (refs != null && refs.containsKey(o));
  }
    
  public String markRef(Object o) {
    if (o instanceof  String || o instanceof Number || o instanceof Boolean || o instanceof Character) return null;
    if (refs == null) {
      refs = new HashMap<Object, Integer>();
    }
    else if (refs.containsKey(o)) {
      return refs.get(o).toString();
    }

    Integer id;
    refs.put(o, id = refs.size() + 1);
    return id.toString();
  }

  public boolean isEscapeMode() {
    return escapeMode != 0;
  }

  public void setEscapeMode() {
    escapeMode++;
  }

  public void unsetEscapeMode() {
    escapeMode--;
  }
}
