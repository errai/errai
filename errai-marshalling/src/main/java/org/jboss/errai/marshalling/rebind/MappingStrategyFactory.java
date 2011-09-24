package org.jboss.errai.marshalling.rebind;

import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.marshalling.rebind.api.MappingContext;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.api.ObjectMapper;
import org.jboss.errai.marshalling.rebind.api.impl.DefaultJavaMappingStrategy;

import java.lang.reflect.Field;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MappingStrategyFactory {

  static MappingStrategy createStrategy(final MappingContext context, final Class<?> clazz) {
    return new DefaultJavaMappingStrategy(context, clazz);
  }
}
