package org.jboss.errai.jpa.rebind;



import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.jpa.client.local.TestingTypedQueryFactory;

/**
 * Code generator for making TypedQuery instances based on existing typesafe
 * queries or JPAQL queries.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class TypedQueryFactoryGenerator<T> {

  private Class<T> resultType;
  private String jpaQuery;

  public TypedQueryFactoryGenerator(Class<T> resultType, String jpaQuery) {
    this.resultType = resultType;
    this.jpaQuery = jpaQuery;
  }

  /**
   * Returns a statement that evaluates to a new instance of the TypedQueryFactory implementation.
   *
   * @return
   */
  public Statement generate() {
    return Stmt.newObject(TestingTypedQueryFactory.class).withParameters(Stmt.loadVariable("this"));
//    AnonymousClassStructureBuilder classBuilder = ObjectBuilder.newInstanceOf(TypedQueryFactory.class).extend();
//    BlockBuilder<?> createMethod = classBuilder
//            .publicOverridesMethod("createIfCompatible", Parameter.of(Class.class, "resultType"));
//    createMethod._(Stmt.nestedCall(Stmt.newObject(TestingTypedQueryFactory.class)).returnValue());
//    createMethod.finish();
//
//    return classBuilder.finish();
  }
}
