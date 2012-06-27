package org.jboss.errai.jpa.rebind;



import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.antlr.HqlSqlTokenTypes;
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory;
import org.hibernate.hql.internal.ast.HqlParser;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.hql.internal.ast.QueryTranslatorImpl;
import org.hibernate.hql.internal.ast.tree.DotNode;
import org.hibernate.hql.internal.ast.tree.LiteralNode;
import org.hibernate.hql.internal.ast.tree.ParameterNode;
import org.hibernate.param.NamedParameterSpecification;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.type.StringRepresentableType;
import org.jboss.errai.codegen.ArithmeticOperator;
import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ArithmeticExpressionBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.common.client.framework.Comparisons;
import org.jboss.errai.jpa.client.local.AbstractEntityJsonMatcher;
import org.jboss.errai.jpa.client.local.ErraiParameter;
import org.jboss.errai.jpa.client.local.JsonUtil;
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
public class TypedQueryFactoryGenerator {

  private final String jpaQuery;
  private QueryTranslatorImpl query;
  private Class<?> resultType;

  public TypedQueryFactoryGenerator(EntityManager em, NamedQuery namedQuery) {
    this.jpaQuery = Assert.notNull(namedQuery.query());

    try {
      HqlParser parser = HqlParser.getInstance(jpaQuery);
      parser.statement();
      AST hqlAst = parser.getAST();
      parser.showAst(hqlAst, System.out);

      SessionImplementor hibernateSession = em.unwrap(SessionImplementor.class);
      ASTQueryTranslatorFactory translatorFactory = new ASTQueryTranslatorFactory();
      query = (QueryTranslatorImpl) translatorFactory.createQueryTranslator(
              namedQuery.name(), jpaQuery, java.util.Collections.EMPTY_MAP, hibernateSession.getFactory());

      query.compile(Collections.EMPTY_MAP, false);
      System.out.println("Return types: " + Arrays.toString(query.getReturnTypes()));

      if (query.getReturnTypes().length != 1) {
        throw new RuntimeException(
                "Presently Errai JPA only supports queries with 1 return type. This query has " +
                query.getReturnTypes().length + ": " + jpaQuery);
      }
      resultType = query.getReturnTypes()[0].getReturnedClass();
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
    AnonymousClassStructureBuilder generatedMatcher = ObjectBuilder.newInstanceOf(AbstractEntityJsonMatcher.class).extend();
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
    System.out.println("Query spaces are: " + query.getQuerySpaces());
    AstInorderTraversal traverser = new AstInorderTraversal(query.getSqlAST().getWalker().getAST());
    matchesMethod.append(
            Stmt.nestedCall(generate(traverser)).returnValue());
  }

  private Statement generate(AstInorderTraversal traverser) {
    while (traverser.hasNext()) {
      AST ast = traverser.next();
      switch (ast.getType()) {
      case HqlSqlTokenTypes.WHERE:
        if (ast.getNumberOfChildren() != 1) {
          throw new IllegalStateException("WHERE clause has " + ast.getNumberOfChildren() + " children (expected 1)");
        }
        return generateBooleanExpression(traverser);
      default:
        System.out.println("Skipping node: " + ast);
      }
    }
    throw new RuntimeException("Didn't find the WHERE clause in the query");
  }

  private Statement generateBooleanExpression(AstInorderTraversal traverser) {
    AST ast = traverser.next();
    switch (ast.getType()) {
    case HqlSqlTokenTypes.EQ:
      return Stmt.invokeStatic(Comparisons.class, "nullSafeEquals", generateValueExpression(traverser), generateValueExpression(traverser));
    default:
      throw new UnexpectedTokenException(ast.getType(), "Boolean expression root node");
    }
  }

  private Statement generateValueExpression(AstInorderTraversal traverser) {
    AST ast = traverser.next();
    switch (ast.getType()) {

    case HqlSqlTokenTypes.DOT:
      DotNode dotNode = (DotNode) ast;
      traverser.fastForwardToNextSiblingOf(dotNode);
      Class<?> requestedType = dotNode.getDataType().getReturnedClass();

      // normalize all numbers except longs and chars to double (literals do the same)
      // if we did not do this here, Comparisons.nullSafeEquals() would have to do it at runtime
      if (requestedType == Float.class || requestedType == float.class
              || requestedType == Integer.class || requestedType == int.class
              || requestedType == Short.class || requestedType == short.class
              || requestedType == Byte.class || requestedType == byte.class) {
        requestedType = Double.class;
      } else if (requestedType == Character.class || requestedType == char.class) {
        requestedType = String.class;
      }

      // FIXME this assumes the property reference is to the candidate entity instance (it could be to another type)
      return Stmt.invokeStatic(JsonUtil.class, "basicValueFromJson",
              Stmt.loadVariable("candidate").invoke("get", dotNode.getPropertyPath()),
              requestedType);

    case HqlSqlTokenTypes.NAMED_PARAM:
      ParameterNode paramNode = (ParameterNode) ast;
      NamedParameterSpecification namedParamSpec = (NamedParameterSpecification) paramNode.getHqlParameterSpecification();
      return Stmt.loadVariable("query").invoke("getParameterValue", namedParamSpec.getName());

    case HqlSqlTokenTypes.QUOTED_STRING:
      return Stmt.loadLiteral(SqlUtil.parseStringLiteral(ast.getText()));

    case HqlSqlTokenTypes.UNARY_MINUS:
      return ArithmeticExpressionBuilder.create(ArithmeticOperator.Subtraction, generateValueExpression(traverser));

    case HqlSqlTokenTypes.NUM_INT: {
      // all numeric literals (except longs) are generated as doubles
      LiteralNode literalNode = (LiteralNode) ast;
      return Cast.to(double.class, Stmt.loadLiteral(((StringRepresentableType<?>) literalNode.getDataType()).fromStringValue(literalNode.getText())));
    }

    case HqlSqlTokenTypes.NUM_LONG: {
      LiteralNode literalNode = (LiteralNode) ast;
      return Stmt.loadLiteral(((StringRepresentableType<?>) literalNode.getDataType()).fromStringValue(literalNode.getText()));
    }

    default:
      throw new UnexpectedTokenException(ast.getType(), "Value expression (attribute reference or named parameter)");
    }
  }

  private static class UnexpectedTokenException extends RuntimeException {
    UnexpectedTokenException(int actual, String expected) {
      super("Encountered unexpected token " +
            HqlSqlWalker._tokenNames[actual] + " (expected " + expected + ")");
    }
  }
}
