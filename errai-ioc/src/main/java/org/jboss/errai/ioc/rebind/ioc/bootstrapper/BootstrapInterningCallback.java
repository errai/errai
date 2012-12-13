package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

import org.jboss.errai.codegen.AnnotationEncoder;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.InterningCallback;
import org.jboss.errai.codegen.Modifier;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.literal.ArrayLiteral;
import org.jboss.errai.codegen.literal.LiteralFactory;
import org.jboss.errai.codegen.literal.LiteralValue;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.codegen.util.Refs;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
* @author Mike Brock
*/
public class BootstrapInterningCallback implements InterningCallback {
  private final Map<Set<Annotation>, String> cachedArrays = new HashMap<Set<Annotation>, String>();
  private final MetaClass Annotation_MC = MetaClassFactory.get(Annotation.class);
  private final ClassStructureBuilder<?> classStructureBuilder;
  private final Context buildContext;

  public BootstrapInterningCallback(final ClassStructureBuilder<?> classStructureBuilder,
                                    final Context buildContext) {
    this.classStructureBuilder = classStructureBuilder;
    this.buildContext = buildContext;
  }

  @Override
  public Statement intern(final LiteralValue<?> literalValue) {
    if (literalValue.getValue() == null) {
      return null;
    }

    if (literalValue.getValue() instanceof Annotation) {
      final Annotation annotation = (Annotation) literalValue.getValue();

      final Class<? extends Annotation> aClass = annotation.annotationType();
      final String fieldName = PrivateAccessUtil.condensify(aClass.getPackage().getName()) +
          aClass.getSimpleName() + "_" + String.valueOf(literalValue.getValue().hashCode()).replaceFirst("\\-", "_");

      classStructureBuilder.privateField(fieldName, annotation.annotationType())
          .modifiers(Modifier.Final).initializesWith(AnnotationEncoder.encode(annotation))
          .finish();

      return Refs.get(fieldName);
    }
    else if (literalValue.getType().isArray()
        && Annotation_MC.isAssignableFrom(literalValue.getType().getOuterComponentType())) {

      final Set<Annotation> annotationSet
          = new HashSet<Annotation>(Arrays.asList((Annotation[]) literalValue.getValue()));

      if (cachedArrays.containsKey(annotationSet)) {
        return Refs.get(cachedArrays.get(annotationSet));
      }

      final MetaClass type = literalValue.getType().getOuterComponentType();
      final String fieldName = "arrayOf" + PrivateAccessUtil.condensify(type.getPackageName()) +
          type.getName().replaceAll("\\.", "_") + "_"
          + String.valueOf(literalValue.getValue().hashCode()).replaceAll("\\-", "_");

      // force rendering of literals in this array first.
      for (final Annotation a : annotationSet) {
        LiteralFactory.getLiteral(a).generate(buildContext);
      }

      classStructureBuilder.privateField(fieldName, literalValue.getType())
          .modifiers(Modifier.Final).initializesWith(new Statement() {
        @Override
        public String generate(final Context context) {
          return new ArrayLiteral(literalValue.getValue()).getCanonicalString(context);
        }

        @Override
        public MetaClass getType() {
          return literalValue.getType();
        }
      }).finish();

      cachedArrays.put(annotationSet, fieldName);

      return Refs.get(fieldName);
    }

    return null;
  }
}
