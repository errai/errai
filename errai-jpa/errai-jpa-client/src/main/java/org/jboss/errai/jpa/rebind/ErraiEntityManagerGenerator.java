package org.jboss.errai.jpa.rebind;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.util.TypeLiteral;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Persistence;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.jboss.errai.codegen.Modifier;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.SnapshotMaker;
import org.jboss.errai.codegen.SnapshotMaker.MethodBodyCallback;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.StringStatement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.impl.gwt.GWTUtil;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.client.local.ErraiEntityType;
import org.jboss.errai.jpa.client.local.ErraiSingularAttribute;
import org.jboss.errai.jpa.client.local.LongIdGenerator;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

public class ErraiEntityManagerGenerator extends Generator {

  private static final List<Class<? extends Annotation>> LIFECYCLE_EVENT_TYPES;
  static {
    List<Class<? extends Annotation>> l = new ArrayList<Class<? extends Annotation>>();
    l.add(PrePersist.class);
    l.add(PostPersist.class);
    l.add(PreUpdate.class);
    l.add(PostUpdate.class);
    l.add(PreRemove.class);
    l.add(PostRemove.class);
    l.add(PostLoad.class);
    LIFECYCLE_EVENT_TYPES = Collections.unmodifiableList(l);
  }

  @Override
  public String generate(TreeLogger logger, GeneratorContext context,
      String typeName) throws UnableToCompleteException {

    GWTUtil.populateMetaClassFactoryFromTypeOracle(context, logger);

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    properties.put("javax.persistence.validation.mode", "none");
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("ErraiJpaClientTests", properties);
    EntityManager em = emf.createEntityManager();
    Metamodel mm = em.getMetamodel();

    final ClassStructureBuilder<?> classBuilder = Implementations.extend(ErraiEntityManager.class, "GeneratedErraiEntityManager");

    // pmm = "populate metamodel method"
    MethodBlockBuilder<?> pmm = classBuilder.protectedMethod(void.class, "populateMetamodel");

    for (final EntityType<?> et : mm.getEntities()) {
      MetaClass met = MetaClassFactory.get(et.getJavaType());

      // first, create a variable for the EntityType
      pmm.append(Stmt.codeComment(
          "**\n" +
          "** EntityType for " + et.getJavaType().getName() + "\n" +
          "**"));
      String entityTypeVarName = entitySnapshotVarName(et.getJavaType());

      AnonymousClassStructureBuilder entityTypeSubclass =
              Stmt.newObject(MetaClassFactory.get(ErraiEntityType.class, new ParameterizedEntityType(et.getJavaType())))
              .extend();

      generateLifecycleEventDeliveryMethods(met, entityTypeSubclass);

      pmm.append(Stmt.declareVariable(ErraiEntityType.class).asFinal()
          .named(entityTypeVarName)
          .initializeWith(entityTypeSubclass.finish().withParameters(et.getName(), et.getJavaType())));

      MethodBodyCallback methodBodyCallback = new MethodBodyCallback() {

        @Override
        public Statement generateMethodBody(MetaMethod method, Object o,
                ClassStructureBuilder<?> containingClassBuilder) {
          // provide reference to declaring type (et) from its attributes
          if (o instanceof SingularAttribute
                  && method.getName().equals("getDeclaringType")
                  && ((SingularAttribute<?, ?>) o).getDeclaringType() == et) {
            return Stmt.loadVariable(entitySnapshotVarName(et.getJavaType())).returnValue();
          }

          // provide get method
          if (o instanceof SingularAttribute
                  && method.getName().equals("get")) {
            SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) o;

            String entityInstanceParam = method.getParameters()[0].getName();

            if (attr.getJavaMember() instanceof Field) {

              // First we need to generate an accessor for the field.
              MetaField field = MetaClassFactory.get((Field) attr.getJavaMember());
              PrivateAccessUtil.addPrivateAccessStubs(PrivateAccessType.Both, true, containingClassBuilder, field, new Modifier[] {});

              // Now generate a call to the private accessor method for the field in question.
              return Stmt.loadVariable("this")
                      .invoke(PrivateAccessUtil.getPrivateFieldInjectorName(field),
                              Stmt.castTo(et.getJavaType(), Stmt.loadVariable(entityInstanceParam)))
                      .returnValue();
            }
            else if (attr.getJavaMember() instanceof Method) {
              return Stmt.loadVariable(entityInstanceParam).invoke(attr.getJavaMember().getName()).returnValue();
            }
            else {
              throw new AssertionError(
                      "JPA properties should only be Field or Method, but this one is " +
                      attr.getJavaMember() == null ? "null" : attr.getJavaMember().getClass());
            }
          }

          // provide set method
          if (o instanceof SingularAttribute
                  && method.getName().equals("set")) {
            SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) o;

            String entityInstanceParam = method.getParameters()[0].getName();
            String newValueParam = method.getParameters()[1].getName();

            if (attr.getJavaMember() instanceof Field) {

              // The write accessor for the field was defined while generating the get() method.
              // Now generate a call to the private accessor method for the field in question.
              MetaField field = MetaClassFactory.get((Field) attr.getJavaMember());
              return Stmt.loadVariable("this")
                      .invoke(PrivateAccessUtil.getPrivateFieldInjectorName(field),
                              Stmt.castTo(et.getJavaType(), Stmt.loadVariable(entityInstanceParam)),
                              Stmt.castTo(MetaClassFactory.get(attr.getJavaType()).asBoxed(), Stmt.loadVariable(newValueParam)));
            }
            else if (attr.getJavaMember() instanceof Method) {
              return Stmt.loadVariable(entityInstanceParam).invoke(attr.getJavaMember().getName()).returnValue();
            }
            else {
              throw new AssertionError(
                      "JPA properties should only be Field or Method, but this one is " +
                      attr.getJavaMember() == null ? "null" : attr.getJavaMember().getClass());
            }
          }

          // provide indication of generated value annotation
          if (o instanceof SingularAttribute
                  && method.getName().equals("isGeneratedValue")) {
            SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) o;

            return Stmt.loadLiteral(isGeneratedValue(attr.getJavaMember())).returnValue();
          }

          // provide generated value iterator
          if (o instanceof SingularAttribute
                  && method.getName().equals("getValueGenerator")) {
            SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) o;

            if (isGeneratedValue(attr.getJavaMember())) {

              // TODO support generated types other than Long
              if (attr.getJavaType() != Long.class) {
                throw new UnsupportedOperationException("ID generation for types other than Long not yet supported");
              }

              containingClassBuilder
                .privateField("valueGenerator", MetaClassFactory.get(new TypeLiteral<Iterator<Long>>() {}))
                .initializesWith(Stmt.newObject(LongIdGenerator.class)
                    .withParameters(Stmt.loadStatic(classBuilder.getClassDefinition(), "this"), Variable.get("this")))
                .finish();

              // StringStatement is a workaround: codegen says valueGenerator is out of scope when we do this properly
              return new StringStatement("return valueGenerator");

            } else {
              return Stmt.throw_(UnsupportedOperationException.class, "Not a generated attribute");
            }
          }

          // allow SnapshotMaker default (read value and create snapshot)
          return null;
        }
      };

      // now, snapshot all the EntityType's attributes, adding them as we go
      List<Statement> attributes = new ArrayList<Statement>();
      for (SingularAttribute<?, ?> attrib : et.getSingularAttributes()) {
        Statement attribSnapshot = SnapshotMaker.makeSnapshotAsSubclass(
            attrib, SingularAttribute.class, ErraiSingularAttribute.class, methodBodyCallback,
            EntityType.class, ManagedType.class, Type.class);
        pmm.append(Stmt.loadVariable(entityTypeVarName).invoke("addAttribute", attribSnapshot));
      }

      // XXX using StringStatement because this gives OutOfScopeException for metamodel:
      // pmm.append(Stmt.loadClassMember("metamodel").invoke("addEntityType", Variable.get(entityTypeVarName)));
      pmm.append(new StringStatement("metamodel.addEntityType(" + entityTypeVarName + ")"));
      System.out.println("singular attributes of " + et + ": " + attributes);
    }

    // XXX using StringStatement because this gives OutOfScopeException for metamodel:
    // pmm.append(Stmt.loadClassMember("metamodel").invoke("freeze"));
    pmm.append(new StringStatement("metamodel.freeze()"));

    pmm.finish();

    String out = classBuilder.toJavaString();

    if (Boolean.getBoolean("errai.codegen.printOut")) {
      System.out.println("---ErraiEntityManager-->");
      System.out.println(out);
      System.out.println("<--ErraiEntityManager---");
    }

    PrintWriter printWriter = context.tryCreate(
        logger,
        classBuilder.getClassDefinition().getPackageName(),
        classBuilder.getClassDefinition().getName());

    // printWriter is null if code has already been generated.
    if (printWriter != null) {
      printWriter.append(out);
      context.commit(logger, printWriter);
    }

    return classBuilder.getClassDefinition().getFullyQualifiedName();
  }

  /**
   * Generates the event delivery methods for the given JPA Entity type.
   *
   * @param entityType
   *          The metaclass representing the entity type.
   * @param classBuilder
   *          The target builder to receive the generated methods. For the
   *          generated code to be valid, this should be a builder of a subclass
   *          of {@link ErraiEntityType}.
   */
  protected void generateLifecycleEventDeliveryMethods(
          MetaClass entityType,
          AnonymousClassStructureBuilder classBuilder) {

    for (Class<? extends Annotation> eventType : LIFECYCLE_EVENT_TYPES) {
      BlockBuilder<AnonymousClassStructureBuilder> methodBuilder =
              classBuilder.publicMethod(
                      Void.TYPE,
                      "deliver" + eventType.getSimpleName(),
                      Parameter.of(entityType, "targetEntity"));

      // TODO also scan standalone listener types mentioned in class-level annotation

      for (MetaMethod callback : entityType.getMethodsAnnotatedWith(eventType)) {
        if (!callback.isPublic()) {
          PrivateAccessUtil.addPrivateAccessStubs(true, classBuilder, callback, new Modifier[] {});
          methodBuilder.append(
                  Stmt.loadVariable("this")
                  .invoke(PrivateAccessUtil.getPrivateMethodName(callback), Stmt.loadVariable("targetEntity")));
        }
        else {
          methodBuilder.append(Stmt.loadVariable("targetEntity").invoke(callback));
        }
      }
      methodBuilder.finish();
    }
  }

  /**
   * Returns true if the given Java member is annotated as a JPA generated value.
   * <p>
   * TODO: support this determination for XML-configured entities.
   *
   * @param javaMember the Java member for the attribute in question
   */
  protected boolean isGeneratedValue(Member javaMember) {
    if (javaMember instanceof Field) {
      MetaField field = MetaClassFactory.get((Field) javaMember);
      return field.isAnnotationPresent(GeneratedValue.class);
    }
    else if (javaMember instanceof Method) {
      MetaMethod method = MetaClassFactory.get((Method) javaMember);
      return method.isAnnotationPresent(GeneratedValue.class);
    }
    throw new IllegalArgumentException("Given member is a "
            + javaMember.getClass().getName()
            + " but JPA attributes can only be a Field or a Method.");
  }

  // TODO check what the other code generators do for class->method names
  static String entitySnapshotVarName(Class<?> forType) {
    return "et_" + forType.getCanonicalName().replace('.', '_');
  }

  /**
   * Represents the parameterized Java reflection type for
   * {@code ErraiEntityType<X>}, where {@code X} can be provided at runtime.
   *
   * @author Jonathan Fuerth <jfuerth@gmail.com>
   */
  static final class ParameterizedEntityType implements ParameterizedType {

    private final java.lang.reflect.Type entityType;

    public ParameterizedEntityType(java.lang.reflect.Type entityType) {
      this.entityType = Assert.notNull(entityType);
    }

    @Override
    public java.lang.reflect.Type[] getActualTypeArguments() {
      return new java.lang.reflect.Type[] { entityType };
    }

    @Override
    public java.lang.reflect.Type getRawType() {
      return ErraiEntityType.class;
    }

    @Override
    public java.lang.reflect.Type getOwnerType() {
      return null;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
              + ((entityType == null) ? 0 : entityType.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ParameterizedEntityType other = (ParameterizedEntityType) obj;
      if (entityType == null) {
        if (other.entityType != null)
          return false;
      }
      else if (!entityType.equals(other.entityType))
        return false;
      return true;
    }
  }
}
