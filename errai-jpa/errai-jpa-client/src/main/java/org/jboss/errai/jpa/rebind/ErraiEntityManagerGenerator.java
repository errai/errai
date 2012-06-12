package org.jboss.errai.jpa.rebind;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.util.TypeLiteral;
import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Persistence;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.apache.commons.collections.OrderedMap;
import org.hibernate.ejb.packaging.PersistenceMetadata;
import org.hibernate.ejb.packaging.PersistenceXmlLoader;
import org.jboss.errai.codegen.BooleanExpression;
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
import org.jboss.errai.codegen.builder.MethodCommentBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.impl.gwt.GWTUtil;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.jpa.client.local.BigIntegerIdGenerator;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.client.local.ErraiEntityType;
import org.jboss.errai.jpa.client.local.ErraiPluralAttribute;
import org.jboss.errai.jpa.client.local.ErraiSingularAttribute;
import org.jboss.errai.jpa.client.local.IntIdGenerator;
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

    EntityManager em = createHibernateEntityManager();
    Metamodel mm = em.getMetamodel();

    final ClassStructureBuilder<?> classBuilder = Implementations.extend(ErraiEntityManager.class, "GeneratedErraiEntityManager");

    generatePopulateMetamodelMethod(classBuilder, mm);

    // pnqm = populate named queries method
    MethodCommentBuilder<?> pnqm = classBuilder.protectedMethod(void.class, "populateNamedQueries");
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    for (Class<?> remote : scanner.getTypesAnnotatedWith(NamedQuery.class, RebindUtils.findTranslatablePackages(context))) {
      NamedQuery namedQuery = remote.getAnnotation(NamedQuery.class);
      TypedQueryFactoryGenerator generator = new TypedQueryFactoryGenerator(remote, namedQuery.query());
      Statement generatedFactory = generator.generate();
      pnqm._(Stmt.loadVariable("super").loadField("namedQueries")
              .invoke("put",
                      Stmt.loadLiteral(namedQuery.name()),
                      generatedFactory));
//      pnqm._(new StringStatement("namedQueries.put(\"" + namedQuery.name() + "\", new ()"));
    }
    pnqm.finish();

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

  private void generatePopulateMetamodelMethod(
          final ClassStructureBuilder<?> classBuilder, Metamodel mm) {
    // pmm = "populate metamodel method"
    MethodBlockBuilder<?> pmm = classBuilder.protectedMethod(void.class, "populateMetamodel");

    for (final EntityType<?> et : mm.getEntities()) {

      // first, create a variable for the EntityType
      String entityTypeVarName = generateErraiEntityType(et, pmm);

      MethodBodyCallback methodBodyCallback = new JpaMetamodelMethodBodyCallback(classBuilder, et);

      // now, snapshot all the EntityType's attributes, adding them as we go
      List<Statement> attributes = new ArrayList<Statement>();
      for (SingularAttribute<?, ?> attrib : et.getSingularAttributes()) {
        Statement attribSnapshot = SnapshotMaker.makeSnapshotAsSubclass(
                attrib, SingularAttribute.class, ErraiSingularAttribute.class, methodBodyCallback,
                EntityType.class, ManagedType.class, Type.class);
        pmm.append(Stmt.loadVariable(entityTypeVarName).invoke("addAttribute", attribSnapshot));
      }
      for (PluralAttribute<?, ?, ?> attrib : et.getPluralAttributes()) {
        Statement attribSnapshot = SnapshotMaker.makeSnapshotAsSubclass(
                attrib, PluralAttribute.class, ErraiPluralAttribute.class, methodBodyCallback,
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
  }

  private EntityManager createHibernateEntityManager() {

    // this is a Set on purpose: two copies of the same persistence.xml will often be present
    // because GWT likes having source directories on the classpath.
    Set<String> persistenceUnits = new LinkedHashSet<String>();

    try {
      for (URL url : Collections.list(getClass().getClassLoader().getResources("META-INF/persistence.xml"))) {
        try {
          for (PersistenceMetadata pm : PersistenceXmlLoader.deploy(url, Collections.emptyMap(), null)) {
            persistenceUnits.add(pm.getName());
          }
        } catch (Exception e) {
          throw new RuntimeException("Failed to parse the persistence unit at " + url + " -- skipping it");
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to locate META-INF/persistence.xml", e);
    }

    if (persistenceUnits.isEmpty()) {
      throw new RuntimeException(
              "Can't generate an ErraiEntityManager because no META-INF/persistence.xml" +
              " resources were found on the classpath");
    }
    if (persistenceUnits.size() > 1) {
      throw new RuntimeException("Found more than one persistence unit on the classpath: " + persistenceUnits);
    }

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    properties.put("javax.persistence.validation.mode", "none");

    EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnits.iterator().next(), properties);
    EntityManager em = emf.createEntityManager();
    return em;
  }

  private String generateErraiEntityType(final EntityType<?> et, MethodBlockBuilder<?> pmm) {
    MetaClass met = MetaClassFactory.get(et.getJavaType());
    pmm.append(Stmt.codeComment(
            "**\n" +
                    "** EntityType for " + et.getJavaType().getName() + "\n" +
            "**"));
    String entityTypeVarName = entitySnapshotVarName(et.getJavaType());

    AnonymousClassStructureBuilder entityTypeSubclass =
            Stmt.newObject(MetaClassFactory.get(ErraiEntityType.class, new ParameterizedEntityType(et.getJavaType())))
            .extend();

    entityTypeSubclass.publicMethod(et.getJavaType(), "newInstance")
    .append(Stmt.nestedCall(Stmt.newObject(et.getJavaType())).returnValue())
    .finish();

    generateLifecycleEventDeliveryMethods(met, entityTypeSubclass);

    pmm.append(Stmt.declareVariable(ErraiEntityType.class).asFinal()
            .named(entityTypeVarName)
            .initializeWith(entityTypeSubclass.finish().withParameters(et.getName(), et.getJavaType())));
    return entityTypeVarName;
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

  /**
   * Determines if the given attribute is an association. This is necessary
   * because the Hibernate implementation of SingularAttribute.isAssociation
   * always returns false.
   *
   * @param attr
   *          The attribute to test for association-ness.
   * @return True iff the attribute's Java Member is annotated with
   *         {@code ManyToMany}, {@code ManyToOne}, {@code OneToMany}, or
   *         {@code OneToOne}.
   */
  private boolean isAssociation(SingularAttribute<?, ?> attr) {
    AccessibleObject member = (AccessibleObject) attr.getJavaMember();
    return (member.getAnnotation(ManyToMany.class) != null
            || member.getAnnotation(ManyToOne.class) != null
            || member.getAnnotation(OneToMany.class) != null
            || member.getAnnotation(OneToOne.class) != null);
  }

  /**
   * Returns a human-readable variable name that is unique to the given type.
   *
   * @param forType
   *          The type you want a variable name for.
   * @return A legal Java identifier that can be used to refer to the given
   *         type.
   */
  private static String entitySnapshotVarName(Class<?> forType) {
    return "et_" + forType.getCanonicalName().replace('.', '_');
  }

  /**
   * Extracts the list of cascade types from the given Java Member, which is
   * expected to be a Field or a Method.
   *
   * @param javaMember
   *          The Java Member of the attribute.
   * @return The array of CascadeType that specifies all types that should be
   *         cascaded, or null if the Member does not have any of the
   *         relationship annotations (ManyToMany, ManyToOne, OneToMany,
   *         OneToOne).
   */
  private static CascadeType[] extractCascadeTypes(Member javaMember) {
    if (!(javaMember instanceof AccessibleObject)) {
      Class<? extends Member> memberType = javaMember == null ? null : javaMember.getClass();
      throw new GenerationException(
              "Found a SingularAttribute whose Java Member is not a field or a method (it is a " + memberType + ")");
    }
    AccessibleObject member = (AccessibleObject) javaMember;

    if (member.getAnnotation(ManyToMany.class) != null) {
      ManyToMany anno = member.getAnnotation(ManyToMany.class);
      return anno.cascade();
    }
    if (member.getAnnotation(ManyToOne.class) != null) {
      ManyToOne anno = member.getAnnotation(ManyToOne.class);
      return anno.cascade();
    }
    if (member.getAnnotation(OneToMany.class) != null) {
      OneToMany anno = member.getAnnotation(OneToMany.class);
      return anno.cascade();
    }
    if (member.getAnnotation(OneToOne.class) != null) {
      OneToOne anno = member.getAnnotation(OneToOne.class);
      return anno.cascade();
    }

    // the member must not be a relationship (or at least not the owning side of one)
    return null;
  }

  /**
   * Helper callback for customized methods during the JPA Metamodel snapshot operation.
   */
  private final class JpaMetamodelMethodBodyCallback implements
  MethodBodyCallback {
    private final ClassStructureBuilder<?> classBuilder;
    private final EntityType<?> et;

    private JpaMetamodelMethodBodyCallback(
            ClassStructureBuilder<?> classBuilder, EntityType<?> et) {
      this.classBuilder = Assert.notNull(classBuilder);
      this.et = Assert.notNull(et);
    }

    @Override
    public Statement generateMethodBody(MetaMethod method, Object sourceObject,
            ClassStructureBuilder<?> containingClassBuilder) {
      // provide reference to declaring type (et) from its attributes
      if (sourceObject instanceof Attribute
              && method.getName().equals("getDeclaringType")
              && ((Attribute<?, ?>) sourceObject).getDeclaringType() == et) {
        return Stmt.loadVariable(entitySnapshotVarName(et.getJavaType())).returnValue();
      }

      // provide get method
      if (sourceObject instanceof Attribute && method.getName().equals("get")) {
        return generateAttributeGetMethod(method, sourceObject, containingClassBuilder);
      }

      // provide set method
      if (sourceObject instanceof Attribute && method.getName().equals("set")) {
        return generateAttributeSetMethod(method, sourceObject);
      }

      // provide indication of generated value annotation
      if (sourceObject instanceof SingularAttribute && method.getName().equals("isGeneratedValue")) {
        SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) sourceObject;

        return Stmt.loadLiteral(isGeneratedValue(attr.getJavaMember())).returnValue();
      }

      // provide generated value iterator
      if (sourceObject instanceof SingularAttribute && method.getName().equals("getValueGenerator")) {
        return generateGetValueGenerator(sourceObject, containingClassBuilder);
      }

      // generate isAssociation because the Hibernate implementation is broken
      if (sourceObject instanceof SingularAttribute && method.getName().equals("isAssociation")) {
        SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) sourceObject;
        return Stmt.loadLiteral(isAssociation(attr)).returnValue();
      }

      if (sourceObject instanceof Attribute && method.getName().equals("cascades")) {
        return generateCascadesMethod(method, sourceObject);
      }

      if (sourceObject instanceof Attribute && method.getName().equals("toString")) {
        Attribute<?, ?> attr = (Attribute<?, ?>) sourceObject;
        return Stmt.loadLiteral(
                attr.getPersistentAttributeType() + " attribute " +
                        attr.getDeclaringType().getJavaType().getSimpleName() +
                        "." + attr.getName()).returnValue();
      }

      if (sourceObject instanceof PluralAttribute && method.getName().equals("createEmptyCollection")) {
        return generateCreateEmptyCollectionMethod(sourceObject);
      }

      // allow SnapshotMaker default (read value and create snapshot)
      return null;
    }

    private Statement generateCreateEmptyCollectionMethod(Object sourceObject) {
      PluralAttribute<?, ?, ?> attr = (PluralAttribute<?, ?, ?>) sourceObject;

      Class<?> declaredCollectionType = attr.getJavaType();
      Class<?> collectionTypeToInstantiate;
      if (!declaredCollectionType.isInterface()) {
        collectionTypeToInstantiate = declaredCollectionType;
      }
      else if (List.class.isAssignableFrom(declaredCollectionType)) {
        collectionTypeToInstantiate = ArrayList.class;
      }
      else if (Set.class.isAssignableFrom(declaredCollectionType)) {
        collectionTypeToInstantiate = HashSet.class;
      }
      else if (OrderedMap.class.isAssignableFrom(declaredCollectionType)) {
        collectionTypeToInstantiate = LinkedHashMap.class;
      }
      else if (Map.class.isAssignableFrom(declaredCollectionType)) {
        collectionTypeToInstantiate = HashMap.class;
      }
      else if (Collection.class.isAssignableFrom(declaredCollectionType)) {
        collectionTypeToInstantiate = ArrayList.class;
      }
      else {
        throw new RuntimeException("Don't know how to instantiate a collection of " + declaredCollectionType);
      }
      return Stmt.nestedCall(Stmt.newObject(collectionTypeToInstantiate)).returnValue();
    }

    private Statement generateCascadesMethod(MetaMethod method, Object sourceObject) {
      Attribute<?, ?> attr = (Attribute<?, ?>) sourceObject;

      // grab cascade annotations from live object then generate a statement like
      // return (cascadeType == [type] || cascadeType == [type] || ...)
      CascadeType[] cascadeTypes = extractCascadeTypes(attr.getJavaMember());
      if (cascadeTypes == null) {
        return Stmt.throw_(UnsupportedOperationException.class, "Not a relationship attribute");
      }
      if (cascadeTypes.length == 0) {
        return Stmt.loadLiteral(false).returnValue();
      }

      BooleanExpression megaExpr = null;
      for (CascadeType type : cascadeTypes) {
        if (type == CascadeType.ALL) {
          // if the list includes ALL, abandon megaExpr and just return true
          return Stmt.loadLiteral(true).returnValue();
        }
        BooleanExpression comparison = Bool.equals(Stmt.loadVariable(method.getParameters()[0].getName()), Stmt.loadLiteral(type));
        if (megaExpr == null) {
          megaExpr = comparison;
        } else {
          megaExpr = Bool.or(comparison, megaExpr);
        }
      }
      return Stmt.load(megaExpr).returnValue();
    }

    private Statement generateGetValueGenerator(Object sourceObject,
            ClassStructureBuilder<?> containingClassBuilder) {
      SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) sourceObject;

      if (isGeneratedValue(attr.getJavaMember())) {

        MetaClass generatorDeclaredType;
        Class<? extends Iterator<?>> generatorType;

        if (attr.getJavaType() == Long.class || attr.getJavaType() == long.class) {
          generatorDeclaredType = MetaClassFactory.get(new TypeLiteral<Iterator<Long>>() {});
          generatorType = LongIdGenerator.class;
        }
        else if (attr.getJavaType() == Integer.class || attr.getJavaType() == int.class) {
          generatorDeclaredType = MetaClassFactory.get(new TypeLiteral<Iterator<Integer>>() {});
          generatorType = IntIdGenerator.class;
        }
        else if (attr.getJavaType() == BigInteger.class) {
          generatorDeclaredType = MetaClassFactory.get(new TypeLiteral<Iterator<BigInteger>>() {});
          generatorType = BigIntegerIdGenerator.class;
        }
        else {
          // TODO implement the missing generatable ID types (which are silly: byte, char, short)
          throw new UnsupportedOperationException("@Generated ID values of " + attr.getJavaType() + " not supported");
        }

        containingClassBuilder
        .privateField("valueGenerator", generatorDeclaredType)
        .initializesWith(Stmt.newObject(generatorType)
                .withParameters(Stmt.loadStatic(classBuilder.getClassDefinition(), "this"), Variable.get("this")))
                .finish();

        // StringStatement is a workaround: codegen says valueGenerator is out of scope when we do this properly
        return new StringStatement("return valueGenerator");

      } else {
        return Stmt.throw_(UnsupportedOperationException.class, "Not a generated attribute");
      }
    }

    private Statement generateAttributeSetMethod(MetaMethod method, Object sourceObject)
            throws AssertionError {
      Attribute<?, ?> attr = (Attribute<?, ?>) sourceObject;

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

    private Statement generateAttributeGetMethod(MetaMethod method, Object o,
            ClassStructureBuilder<?> containingClassBuilder)
                    throws AssertionError {
      Attribute<?, ?> attr = (Attribute<?, ?>) o;

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
  }

  /**
   * Represents the parameterized Java reflection type for
   * {@code ErraiEntityType<X>}, where {@code X} can be provided at runtime.
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
