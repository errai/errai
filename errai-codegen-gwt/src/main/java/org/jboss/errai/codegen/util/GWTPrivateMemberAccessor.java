package org.jboss.errai.codegen.util;

import com.google.gwt.core.client.UnsafeNativeLong;
import org.jboss.errai.codegen.DefParameters;
import org.jboss.errai.codegen.Modifier;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.StringStatement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.builder.MethodCommentBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class GWTPrivateMemberAccessor implements PrivateMemberAccessor {

  /**
   * Annotation instance that can be passed to the code generator when generating long accessors.
   */
  private static final UnsafeNativeLong UNSAFE_NATIVE_LONG_ANNOTATION = new UnsafeNativeLong() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return UnsafeNativeLong.class;
    }
  };

  @Override
  public void createWritableField(final MetaClass type,
                                  final ClassStructureBuilder<?> classBuilder,
                                  final MetaField field,
                                  final Modifier[] modifiers)  {

    final MethodCommentBuilder<? extends ClassStructureBuilder<?>> methodBuilder =
            classBuilder.privateMethod(void.class, PrivateAccessUtil.getPrivateFieldInjectorName(field));

    if (type.getCanonicalName().equals("long")) {
      methodBuilder.annotatedWith(UNSAFE_NATIVE_LONG_ANNOTATION);
    }

    if (!field.isStatic()) {
      methodBuilder
              .parameters(DefParameters.fromParameters(Parameter.of(field.getDeclaringClass(), "instance"),
                      Parameter.of(type, "value")));
    }

    methodBuilder.modifiers(appendJsni(modifiers))
            .body()
            ._(new StringStatement(JSNIUtil.fieldAccess(field) + " = value"))
            .finish();
  }

  @Override
  public void createReadableField(final MetaClass type,
                                  final ClassStructureBuilder<?> classBuilder,
                                  final MetaField field,
                                  final Modifier[] modifiers) {

    final MethodBlockBuilder<? extends ClassStructureBuilder<?>> instance =
            classBuilder.privateMethod(type, PrivateAccessUtil.getPrivateFieldInjectorName(field));

    if (!field.isStatic()) {
      instance.parameters(DefParameters.fromParameters(Parameter.of(field.getDeclaringClass(), "instance")));
    }

    if (type.getCanonicalName().equals("long")) {
      instance.annotatedWith(UNSAFE_NATIVE_LONG_ANNOTATION);
    }

    instance.modifiers(appendJsni(modifiers))
            .body()
            ._(new StringStatement("return " + JSNIUtil.fieldAccess(field)))
            .finish();
  }

  @Override
  public void makeMethodAccessible(final ClassStructureBuilder<?> classBuilder,
                                   final MetaMethod method,
                                   final Modifier[] modifiers) {

    final List<Parameter> wrapperDefParms = new ArrayList<Parameter>();

    if (!method.isStatic()) {
      wrapperDefParms.add(Parameter.of(method.getDeclaringClass().getErased(), "instance"));
    }

    final List<Parameter> methodDefParms = DefParameters.from(method).getParameters();
    wrapperDefParms.addAll(methodDefParms);

    classBuilder.publicMethod(method.getReturnType(), PrivateAccessUtil.getPrivateMethodName(method))
            .parameters(DefParameters.fromParameters(wrapperDefParms))
            .modifiers(appendJsni(modifiers))
            .body()
            ._(new StringStatement(JSNIUtil.methodAccess(method)))
            .finish();
  }

  @Override
  public void makeConstructorAccessible(final ClassStructureBuilder<?> classBuilder,
                                        final MetaConstructor constructor) {

    final DefParameters methodDefParms = DefParameters.from(constructor);

    classBuilder.publicMethod(constructor.getReturnType(), PrivateAccessUtil.getPrivateMethodName(constructor))
            .parameters(methodDefParms)
                    .modifiers(Modifier.Static, Modifier.JSNI)
                    .body()
                    ._(new StringStatement(JSNIUtil.methodAccess(constructor)))
                    .finish();
  }

  /**
   * Returns a new array consisting of a copy of the given array, plus
   * Modifiers.JSNI as the last element.
   *
   * @param modifiers
   *         The array to copy. May be empty, but must not be null.
   *
   * @return An array of length {@code n + 1}, where {@code n} is the length of
   *         the given array. Positions 0..n-1 correspond with the respective
   *         entries in the given array, and position n contains Modifiers.JSNI.
   */
  public static Modifier[] appendJsni(Modifier[] modifiers) {
    final Modifier[] origModifiers = modifiers;
    modifiers = new Modifier[origModifiers.length + 1];
    System.arraycopy(origModifiers, 0, modifiers, 0, origModifiers.length);
    modifiers[modifiers.length - 1] = Modifier.JSNI;
    return modifiers;
  }
}
