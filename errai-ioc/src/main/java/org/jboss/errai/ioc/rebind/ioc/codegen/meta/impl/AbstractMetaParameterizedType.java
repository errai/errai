package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class AbstractMetaParameterizedType implements MetaParameterizedType {
  @Override
  public boolean isAssignableFrom(MetaParameterizedType type) {
    MetaType[] compareFrom = getTypeParameters();
    MetaType[] compareTo;

    if (type == null) {
      compareTo = new MetaType[compareFrom.length];
      for (int i = 0; i < compareFrom.length; i++) {
        compareTo[i] = new MetaType() {
          public String toString() {
            return "?";
          }
        };
      }
    }
    else {
      compareTo = type.getTypeParameters();
    }

    if (compareTo.length != compareFrom.length) return false;

    for (int i = 0; i < compareTo.length; i++) {
      if (compareFrom[i].toString().equals("?")) {
        continue;
      }
      if (compareFrom[i] instanceof MetaClass && compareTo[i] instanceof MetaClass) {
        if (!((MetaClass) compareFrom[i]).isAssignableFrom((MetaClass) compareTo[i])) {
          return false;
        }
      }
    }

    return true;
  }
}
