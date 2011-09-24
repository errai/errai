package org.jboss.errai.marshalling.rebind.api.impl;

import org.jboss.errai.bus.rebind.ScannerSingleton;
import org.jboss.errai.bus.server.service.metadata.MetaDataProcessor;
import org.jboss.errai.bus.server.service.metadata.MetaDataScanner;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.MappedOrdered;
import org.jboss.errai.marshalling.client.api.MapsTo;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.marshallers.IntegerMarshaller;
import org.jboss.errai.marshalling.client.marshallers.LongMarshaller;
import org.jboss.errai.marshalling.client.marshallers.StringMarshaller;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.api.ObjectMapper;
import org.jboss.errai.marshalling.rebind.util.MarshallingUtil;

import java.lang.reflect.Constructor;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DefaultJavaMappingStrategy implements MappingStrategy {
  private Class<?> toMap;
  private static List<Class<? extends Marshaller>> registeredMarshallers;
  private MetaDataScanner scanner;


  public DefaultJavaMappingStrategy(Class<?> toMap) {
    this.toMap = toMap;
    scanner = ScannerSingleton.getOrCreateInstance();

  }

  @Override
  public ObjectMapper getMapper() {
    if (isJavaBean(toMap)) {
      return generateJavaBeanMapper();
    }
    else {
      return generateImmutableMapper();
    }
  }

  private ObjectMapper generateImmutableMapper() {
    final ConstructorMapping mapping = findUsableConstructorMapping();

    final List<Statement> marshallers = new ArrayList<Statement>();
    for (FieldMapping m : mapping.getMappings()) {
      marshallers.add(MarshallingUtil.marshallerForString(m.getFieldName()));
    }

    return new ObjectMapper() {
      @Override
      public Statement getMarshaller() {
        return Stmt.create().newObject(toMap).withParameters(marshallers.toArray(new Object[marshallers.size()]));
      }
    };

  }

  private ObjectMapper generateJavaBeanMapper() {
    return null;
  }

  private ConstructorMapping findUsableConstructorMapping() {
    Set<Constructor<?>> constructors = new HashSet<Constructor<?>>();
    Set<FieldMapping> mappings = new HashSet<FieldMapping>();

    for (Constructor c : toMap.getConstructors()) {
      if (c.isAnnotationPresent(MappedOrdered.class)) {
        constructors.add(c);
      }
      else if (c.getParameterTypes().length != 0) {
        boolean satisifed = true;
        FieldScan:
        for (int i = 0; i < c.getParameterTypes().length; i++) {
          Annotation[] annotations = c.getParameterAnnotations()[i];
          if (annotations.length == 0) {
            satisifed = false;
            break FieldScan;
          }
          else {
            for (Annotation a : annotations) {
              if (!MapsTo.class.isAssignableFrom(a.annotationType())) {
                satisifed = false;
                break FieldScan;
              }
              else {
                mappings.add(new FieldMapping(i, ((MapsTo) a).value()));
              }
            }
          }
        }

        if (satisifed) {
          constructors.add(c);
        }
      }
    }

    return new ConstructorMapping(constructors.iterator().next(), ConstructionType.Mapped, mappings);
  }

  private static enum ConstructionType {
    Mapped, Custom
  }

  private static class ConstructorMapping {
    Constructor<?> constructor;
    ConstructionType type;
    Set<FieldMapping> mappings;

    private ConstructorMapping(Constructor<?> constructor, ConstructionType type, Set<FieldMapping> mappings) {
      this.constructor = constructor;
      this.type = type;
      this.mappings = mappings;
    }

    public Constructor<?> getConstructor() {
      return constructor;
    }

    public ConstructionType getType() {
      return type;
    }

    public Set<FieldMapping> getMappings() {
      return mappings;
    }
  }

  private static class FieldMapping {
    int index;
    String fieldName;

    private FieldMapping(int index, String fieldName) {
      this.index = index;
      this.fieldName = fieldName;
    }

    public int getIndex() {
      return index;
    }

    public String getFieldName() {
      return fieldName;
    }
  }

  private boolean isJavaBean(Class<?> toMap) {
    try {
      toMap.getConstructor();
      return true;
    }
    catch (NoSuchMethodException e) {
      return false;
    }
  }


}
