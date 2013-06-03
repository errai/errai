package org.jboss.errai.validation.rebind;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.gwt.validation.client.GwtValidation;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.reflections.Reflections;
import org.jboss.errai.reflections.scanners.FieldAnnotationsScanner;
import org.jboss.errai.reflections.scanners.TypeAnnotationsScanner;
import org.jboss.errai.reflections.util.ClasspathHelper;
import org.jboss.errai.reflections.util.ConfigurationBuilder;

import javax.validation.Constraint;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Generates the GWT {@link Validator} interface based on validation annotations.
 *
 * @author Johannes Barop <jb@barop.de>
 */
class GwtValidatorGenerator {

  class ValidationScanner extends Reflections {

    ValidationScanner() {
      super(new ConfigurationBuilder()
              .setUrls(ClasspathHelper.forClassLoader())
              .setScanners(new TypeAnnotationsScanner(), new FieldAnnotationsScanner())
      );
      scan();
    }

  }

  private final ValidationScanner scanner;

  GwtValidatorGenerator() {
    this.scanner = new ValidationScanner();
  }

  ClassStructureBuilder<?> generate() {
    final Set<Class<?>> validationAnnotations = scanner.getTypesAnnotatedWith(Constraint.class);
    final SetMultimap<Class<?>, Annotation> validationConfig = getValidationConfig(validationAnnotations);
    final Set<Class<?>> beans = validationConfig.keySet();
    final Set<Class<?>> groups = extractValidationGroups(validationConfig);

    ClassStructureBuilder<?> builder = ClassBuilder
            .define("Gwt" + Validator.class.getSimpleName())
            .publicScope()
            .interfaceDefinition()
            .implementsInterface(Validator.class)
            .body();

    builder.getClassDefinition().addAnnotation(new GwtValidation() {
      @Override
      public Class<?>[] value() {
        return beans.toArray(new Class<?>[beans.size()]);
      }

      @Override
      public Class<?>[] groups() {
        return groups.toArray(new Class<?>[groups.size()]);
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return GwtValidation.class;
      }
    });

    return builder;
  }

  private SetMultimap<Class<?>, Annotation> getValidationConfig(Set<Class<?>> validationAnnotations) {
    SetMultimap<Class<?>, Annotation> beans = HashMultimap.create();
    for (Class<?> annotation : validationAnnotations) {
      for (Field field : scanner.getFieldsAnnotatedWith((Class<? extends Annotation>) annotation)) {
        beans.put(field.getDeclaringClass(), field.getAnnotation((Class<? extends Annotation>) annotation));
      }
    }

    return beans;
  }

  private Set<Class<?>> extractValidationGroups(SetMultimap<Class<?>, Annotation> validationConfig) {
    Set<Class<?>> groups = new HashSet<Class<?>>();

    for (Annotation annotation : validationConfig.values()) {
      try {
        Method method = annotation.getClass().getMethod("groups", null);
        Class<?>[] ret = (Class<?>[]) method.invoke(annotation, null);
        if (ret.length != 0) {
          groups.addAll(Arrays.asList(ret));
        } else {
          groups.add(Default.class);
        }
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("Error finding groups() parameter in " + annotation.getClass().getName(), e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException("Error invoking groups() parameter in " + annotation.getClass().getName(), e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Error invoking groups() parameter in " + annotation.getClass().getName(), e);
      }
    }

    return groups;
  }

}
