package org.jboss.errai.jpa.rebind;



import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory;
import org.hibernate.hql.internal.ast.HqlParser;
import org.hibernate.hql.internal.ast.QueryTranslatorImpl;
import org.hibernate.hql.internal.ast.util.NodeTraverser;
import org.hibernate.hql.internal.ast.util.NodeTraverser.VisitationStrategy;
import org.hibernate.param.NamedParameterSpecification;
import org.hibernate.param.ParameterSpecification;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.jpa.client.local.EntityJsonMatcher;
import org.jboss.errai.jpa.client.local.ErraiParameter;
import org.jboss.errai.jpa.client.local.TypedQueryFactory;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

import com.google.gwt.json.client.JSONObject;

/**
 * Code generator for making TypedQuery instances based on existing typesafe
 * queries or JPAQL queries.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class TypedQueryFactoryGenerator<T> {

  private final Class<T> resultType;
  private final String jpaQuery;
  private QueryTranslatorImpl query;

  public TypedQueryFactoryGenerator(EntityManager em, Class<T> resultType, String queryName, String jpaQuery) {
    this.resultType = Assert.notNull(resultType);
    this.jpaQuery = Assert.notNull(jpaQuery);

    try {
      HqlParser parser = HqlParser.getInstance(jpaQuery);
      parser.statement();
      AST hqlAst = parser.getAST();
      parser.showAst(hqlAst, System.out);

      CodeGeneratingVisitationStrategy visitor = new CodeGeneratingVisitationStrategy();
      NodeTraverser walker = new NodeTraverser(visitor);
      walker.traverseDepthFirst( hqlAst );

      SessionImplementor hibernateSession = em.unwrap(SessionImplementor.class);
      ASTQueryTranslatorFactory translatorFactory = new ASTQueryTranslatorFactory();
      query = (QueryTranslatorImpl) translatorFactory.createQueryTranslator(
              queryName, jpaQuery, java.util.Collections.EMPTY_MAP, hibernateSession.getFactory());

      query.compile(Collections.EMPTY_MAP, false);
      System.out.println("Return types: " + Arrays.toString(query.getReturnTypes()));
      org.hibernate.hql.internal.ast.tree.Statement sqlAST = query.getSqlAST();

      System.out.println("Second level parse tree:");
      sqlAST.getWalker().getASTPrinter().showAst(sqlAST.getWalker().getAST(), System.out);
    } catch (RecognitionException e) {
      throw new RuntimeException("Failed to parse JPQL query: " + jpaQuery);
    } catch (TokenStreamException e) {
      throw new RuntimeException("Failed to parse JPQL query: " + jpaQuery);
    }
  }

  /**
   * Returns a statement that evaluates to a new instance of the TypedQueryFactory implementation.
   */
  public Statement generate(Statement entityManager) {

    // build the matcher (anonymous inner class)
    AnonymousClassStructureBuilder generatedMatcher = ObjectBuilder.newInstanceOf(EntityJsonMatcher.class).extend();
    BlockBuilder<?> matchesMethod = generatedMatcher
            .publicOverridesMethod("matches", Parameter.of(JSONObject.class, "candidate"));
    fillInMatchesMethod(matchesMethod);
    matchesMethod.finish();

    // build the param list
    System.out.println("Named parameters: " + query.getParameterTranslations().getNamedParameterNames());
    @SuppressWarnings("unchecked")
    List<ParameterSpecification> parameterSpecifications = query.getSqlAST().getWalker().getParameters();
    Statement generatedParamList[] = new Statement[parameterSpecifications.size()];
    for (int i = 0; i < parameterSpecifications.size(); i++) {
      NamedParameterSpecification ps = (NamedParameterSpecification) parameterSpecifications.get(i);

      // invoking ErraiParameter(String name, Integer position, Class<T> type)

      generatedParamList[i] = Stmt.newObject(ErraiParameter.class).withParameters(
              ps.getName(),
              Integer.valueOf(i),
              ps.getExpectedType().getReturnedClass());
    }

    // return the query factory
    return Stmt.newObject(TypedQueryFactory.class).withParameters(
            entityManager,
            Stmt.loadLiteral(resultType),
            generatedMatcher.finish(),
            Stmt.newArray(ErraiParameter.class).initialize((Object[]) generatedParamList));
  }

  /**
   * Fills in the given method with the logic for matching its argument (a
   * JSONObject called "candidate") against the where clause in the JPQL query.
   *
   * @param matchesMethod
   */
  private void fillInMatchesMethod(BlockBuilder<?> matchesMethod) {
    matchesMethod.append(
            Stmt.nestedCall(
                    Stmt.loadLiteral("Let It Be").invoke("equals", Stmt.loadVariable("candidate").invoke("get", "name").invoke("isString").invoke("stringValue"))
                    ).returnValue());
  }

  private class CodeGeneratingVisitationStrategy implements VisitationStrategy {

    @Override
    public void visit(AST node) {
      System.out.println("Visiting " + node + "(type " + HqlParser._tokenNames[node.getType()] + ")");
    }

  }
}
