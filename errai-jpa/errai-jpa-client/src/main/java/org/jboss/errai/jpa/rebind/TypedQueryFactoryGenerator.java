package org.jboss.errai.jpa.rebind;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import javax.persistence.TypedQuery;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.antlr.HqlSqlTokenTypes;
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory;
import org.hibernate.hql.internal.ast.HqlParser;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.hql.internal.ast.QueryTranslatorImpl;
import org.hibernate.hql.internal.ast.tree.DotNode;
import org.hibernate.hql.internal.ast.tree.IdentNode;
import org.hibernate.hql.internal.ast.tree.ParameterNode;
import org.hibernate.hql.internal.ast.tree.SqlNode;
import org.hibernate.param.NamedParameterSpecification;
import org.hibernate.param.ParameterSpecification;
import org.jboss.errai.codegen.ArithmeticOperator;
import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.Modifier;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.StringStatement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ArithmeticExpressionBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Arith;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.Implementations.StringBuilderBuilder;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.common.client.framework.Comparisons;
import org.jboss.errai.jpa.client.local.ErraiAttribute;
import org.jboss.errai.jpa.client.local.ErraiMetamodel;
import org.jboss.errai.jpa.client.local.ErraiParameter;
import org.jboss.errai.jpa.client.local.ErraiTypedQuery;
import org.jboss.errai.jpa.client.local.JsonUtil;
import org.jboss.errai.jpa.client.local.TypedQueryFactory;
import org.mvel2.MVEL;

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
  private final QueryTranslatorImpl query;
  private final Class<?> resultType;
  private final AtomicInteger uniqueNumber = new AtomicInteger();

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
  public Statement generate(Statement entityManager, Context context) {
    // anonQueryClassBuilder comes out as a statement that looks like this:
    // new ErraiTypedQuery(entityManager, actualResultType, parameters) {
    //   public void matches(JSONObject object) { ... }
    //   public void sort(List<T> resultList) { ... }
    // }
    AnonymousClassStructureBuilder anonQueryClassBuilder = ObjectBuilder.newInstanceOf(ErraiTypedQuery.class, context).extend(
            Stmt.loadVariable("entityManager"),
            Stmt.loadVariable("actualResultType"),
            Stmt.loadVariable("parameters"));
    appendMatchesMethod(anonQueryClassBuilder);
    appendComparatorMethod(anonQueryClassBuilder, context);

    AnonymousClassStructureBuilder factoryBuilder = ObjectBuilder.newInstanceOf(TypedQueryFactory.class, context).extend(
            entityManager,
            Stmt.loadLiteral(resultType),
            Stmt.newArray(ErraiParameter.class).initialize((Object[]) generateQueryParamArray()));

    BlockBuilder<AnonymousClassStructureBuilder> createQueryMethod =
            factoryBuilder.protectedMethod(TypedQuery.class, "createQuery").body();
    createQueryMethod.append(Stmt.nestedCall(anonQueryClassBuilder.finish()).returnValue());
    createQueryMethod.finish();

    return factoryBuilder.finish();
  }

  /**
   * Creates an array of statements that generates code for the array of named parameters in the query.
   */
  private Statement[] generateQueryParamArray() {
    System.out.println("Named parameters: " + query.getParameterTranslations().getNamedParameterNames());
    @SuppressWarnings("unchecked")
    List<ParameterSpecification> parameterSpecifications = query.getSqlAST().getWalker().getParameters();
    Statement generatedParamList[] = new Statement[parameterSpecifications.size()];
    for (int i = 0; i < parameterSpecifications.size(); i++) {
      NamedParameterSpecification ps = (NamedParameterSpecification) parameterSpecifications.get(i);

      // invoking ErraiParameter(String name, Integer position, Class<T> type)

      Class<?> paramType;
      if (ps.getExpectedType() != null) {
        paramType = ps.getExpectedType().getReturnedClass();
      }
      else {
        paramType = Object.class;
      }

      generatedParamList[i] = Stmt.newObject(ErraiParameter.class).withParameters(
              ps.getName(),
              Integer.valueOf(i),
              paramType);
    }
    return generatedParamList;
  }

  /**
   * Adds the public override method {@code matches(JSONObject candidate)} to
   * the given class builder. The matching logic is, of course, generated based
   * on the JPA query this generator was created with.
   *
   * @param classBuilder
   *          The class builder to append the generated matcher method to.
   */
  private void appendMatchesMethod(AnonymousClassStructureBuilder classBuilder) {

    AstInorderTraversal traverser = new AstInorderTraversal(query.getSqlAST().getWalker().getAST());
    AST whereClause = traverser.fastForwardTo(HqlSqlTokenTypes.WHERE);

    BlockBuilder<?> matchesMethod = classBuilder
            .publicOverridesMethod("matches", Parameter.of(JSONObject.class, "candidate"));

    Statement matchesStmt;
    if (whereClause != null) {
      matchesStmt = generateExpression(traverser, new JsonDotNodeResolver(), matchesMethod);
    }
    else {
      matchesStmt = Stmt.loadLiteral(true);
    }

    matchesMethod.append(Stmt.nestedCall(matchesStmt).returnValue());
    matchesMethod.finish();
  }

  /**
   * Adds the {@code getComparator()} method to the given class builder.
   *
   * @param classBuilder
   *          The class builder to add the method to. Should be a builder for a
   *          subclass of ErraiTypedQuery.
   */
  private void appendComparatorMethod(AnonymousClassStructureBuilder classBuilder, Context context) {
    AstInorderTraversal traverser = new AstInorderTraversal(query.getSqlAST().getWalker().getAST());
    final AST orderByParentNode = traverser.fastForwardTo(HqlSqlTokenTypes.ORDER);

    Statement comparator;
    if (orderByParentNode == null) {
      comparator = Stmt.loadLiteral(null);
    }
    else {
      AnonymousClassStructureBuilder comparatorClassBuilder = ObjectBuilder.newInstanceOf(Comparator.class, context).extend();
      BlockBuilder<AnonymousClassStructureBuilder> compareMethod = comparatorClassBuilder
              .publicOverridesMethod("compare", Parameter.of(Object.class, "o1"), Parameter.of(Object.class, "o2"));

      // create "lhs" and "rhs" local vars of the query's result type; cast and assign Object args
      compareMethod
              .append(Stmt.declareFinalVariable("lhs", resultType, Cast.to(resultType, Stmt.loadVariable("o1"))))
              .append(Stmt.declareFinalVariable("rhs", resultType, Cast.to(resultType, Stmt.loadVariable("o2"))));

      // Create resolvers that will generate Statements based on the "lhs" and "rhs" vars
      JavaDotNodeResolver lhsResolver = new JavaDotNodeResolver("lhs", comparatorClassBuilder);
      JavaDotNodeResolver rhsResolver = new JavaDotNodeResolver("rhs", null);

      // orderNode is the iteration variable that points to the current ORDER BY subclause
      AST orderNode = traverser.next();

      // result variable to hold the comparison result of each ORDER BY subclause
      compareMethod.append(Stmt.declareVariable("result", int.class));

      while (traverser.context().contains(orderByParentNode)) {
        Statement lhs = Stmt.castTo(Comparable.class, generateExpression(new AstInorderTraversal(orderNode), lhsResolver, compareMethod));
        Statement rhs = Stmt.castTo(Comparable.class, generateExpression(new AstInorderTraversal(orderNode), rhsResolver, compareMethod));

        // Determine if this subclause is marked ASCENDING or DESCENDING, and if so, skip over that node
        traverser.fastForwardToNextSiblingOf(orderNode);
        AST nextNode = traverser.hasNext() ? traverser.next() : null;
        ArithmeticOperator ascDescOperator;
        if (nextNode != null && nextNode.getType() == HqlSqlTokenTypes.DESCENDING) {
          ascDescOperator = ArithmeticOperator.Subtraction;
          nextNode = traverser.hasNext() ? traverser.next() : null;
        }
        else if (nextNode != null && nextNode.getType() == HqlSqlTokenTypes.ASCENDING) {
          ascDescOperator = ArithmeticOperator.Addition;
          nextNode = traverser.hasNext() ? traverser.next() : null;
        }
        else {
          ascDescOperator = ArithmeticOperator.Addition;
        }

        compareMethod
            .append(Stmt.loadVariable("result").assignValue(Stmt.invokeStatic(Comparisons.class, "nullSafeCompare", lhs, rhs)))
            .append(Stmt.if_(Bool.notEquals(Stmt.loadVariable("result"), 0))
                .append(Stmt.nestedCall(Arith.expr(ascDescOperator, Stmt.loadVariable("result"))).returnValue())
                .finish());

        orderNode = nextNode;
      }

      // everything compared equal. return 0.
      compareMethod.append(Stmt.loadLiteral(0).returnValue());

      comparator = compareMethod.finish().finish();
    }

    classBuilder.protectedMethod(Comparator.class, "getComparator")
      .append(Stmt.nestedCall(comparator).returnValue())
      .finish();
  }


  /**
   * Consumes the next token from the traverser and returns the equivalent Java
   * expression, recursing if necessary.
   *
   * @param traverser
   *          The traverser that walks through the nodes of the Hibernate
   *          second-level AST in order. When this method returns, the traverser
   *          will have completely walked the subtree under the starting node.
   *          The traverser will be left on the next node.
   * @param dotNodeResolver
   *          the mechanism for resolving a DotNode (that is, a JPQL property
   *          reference in the query) into an Errai codegen Statement.
   * @param containingMethod
   *          the builder for the method that will eventually receive the
   *          returned statement. If the code emitted by this generator depends
   *          on some variable declaration, the variable declaration statement
   *          will be appended to this block.
   * @return a statement that evaluates to the subtree rooted at the current
   *         position of {@code traverser}.
   */
  private Statement generateExpression(AstInorderTraversal traverser, DotNodeResolver dotNodeResolver, BlockBuilder<?> containingMethod) {
    AST ast = traverser.next();
    switch (ast.getType()) {

    //
    // BOOLEAN EXPRESSIONS
    //

    case HqlSqlTokenTypes.EQ:
      return Stmt.invokeStatic(
              Comparisons.class, "nullSafeEquals",
              generateExpression(traverser, dotNodeResolver, containingMethod), generateExpression(traverser, dotNodeResolver, containingMethod));

    case HqlSqlTokenTypes.NE:
      return Bool.notExpr(Stmt.invokeStatic(
              Comparisons.class, "nullSafeEquals",
              generateExpression(traverser, dotNodeResolver, containingMethod), generateExpression(traverser, dotNodeResolver, containingMethod)));

    case HqlSqlTokenTypes.GT:
      return Stmt.invokeStatic(
              Comparisons.class, "nullSafeGreaterThan",
              generateExpression(traverser, dotNodeResolver, containingMethod), generateExpression(traverser, dotNodeResolver, containingMethod));

    case HqlSqlTokenTypes.GE:
      return Stmt.invokeStatic(
              Comparisons.class, "nullSafeGreaterThanOrEqualTo",
              generateExpression(traverser, dotNodeResolver, containingMethod), generateExpression(traverser, dotNodeResolver, containingMethod));

    case HqlSqlTokenTypes.LT:
      return Stmt.invokeStatic(
              Comparisons.class, "nullSafeLessThan",
              generateExpression(traverser, dotNodeResolver, containingMethod), generateExpression(traverser, dotNodeResolver, containingMethod));

    case HqlSqlTokenTypes.LE:
      return Stmt.invokeStatic(
              Comparisons.class, "nullSafeLessThanOrEqualTo",
              generateExpression(traverser, dotNodeResolver, containingMethod), generateExpression(traverser, dotNodeResolver, containingMethod));

    case HqlSqlTokenTypes.BETWEEN: {
      Statement middle = generateExpression(traverser, dotNodeResolver, containingMethod);
      Statement small = generateExpression(traverser, dotNodeResolver, containingMethod);
      Statement big = generateExpression(traverser, dotNodeResolver, containingMethod);
      return Bool.and(
              Stmt.invokeStatic(Comparisons.class, "nullSafeLessThanOrEqualTo", small, middle),
              Stmt.invokeStatic(Comparisons.class, "nullSafeLessThanOrEqualTo", middle, big));
    }

    case HqlSqlTokenTypes.NOT_BETWEEN: {
      Statement outside = generateExpression(traverser, dotNodeResolver, containingMethod);
      Statement small = generateExpression(traverser, dotNodeResolver, containingMethod);
      Statement big = generateExpression(traverser, dotNodeResolver, containingMethod);
      return Bool.or(
              Stmt.invokeStatic(Comparisons.class, "nullSafeLessThan", outside, small),
              Stmt.invokeStatic(Comparisons.class, "nullSafeGreaterThan", outside, big));
    }

    case HqlSqlTokenTypes.NOT_IN:
    case HqlSqlTokenTypes.IN: {
      final boolean notIn = ast.getType() == HqlSqlTokenTypes.NOT_IN;
      Statement thingToTest = generateExpression(traverser, dotNodeResolver, containingMethod);
      ast = traverser.next();
      if (ast.getType() != HqlSqlTokenTypes.IN_LIST) {
        throw new GenerationException("Expected IN_LIST node but found " + ast.getText());
      }

      List<Statement> collection = new ArrayList<Statement>(ast.getNumberOfChildren());
      for (int i = 0; i < ast.getNumberOfChildren(); i++) {
        collection.add(Cast.to(Object.class, generateExpression(traverser, dotNodeResolver, containingMethod)));
      }
      Statement callToComparisonsIn = Stmt.invokeStatic(Comparisons.class, "in", thingToTest, collection.toArray());
      return notIn ? Bool.notExpr(callToComparisonsIn) : callToComparisonsIn;
    }

    case HqlSqlTokenTypes.NOT_LIKE:
    case HqlSqlTokenTypes.LIKE: {
      Statement valueExpr = Cast.to(String.class, generateExpression(traverser, dotNodeResolver, containingMethod));
      Statement patternExpr = Cast.to(String.class, generateExpression(traverser, dotNodeResolver, containingMethod));
      Statement escapeCharExpr = Cast.to(String.class, Stmt.loadLiteral(null));
      if (ast.getNumberOfChildren() == 3) {
        traverser.next();
        escapeCharExpr = Cast.to(String.class, generateExpression(traverser, dotNodeResolver, containingMethod));
      }
      Statement likeStmt = Stmt.invokeStatic(
              Comparisons.class, "like", valueExpr, patternExpr, escapeCharExpr);
      return ast.getType() == HqlSqlTokenTypes.LIKE ? likeStmt : Bool.notExpr(likeStmt);
    }

    case HqlSqlTokenTypes.IS_NULL:
      return Bool.isNull(generateExpression(traverser, dotNodeResolver, containingMethod));

    case HqlSqlTokenTypes.IS_NOT_NULL:
      return Bool.isNotNull(generateExpression(traverser, dotNodeResolver, containingMethod));

    case HqlSqlTokenTypes.OR:
      return Bool.or(generateExpression(traverser, dotNodeResolver, containingMethod), generateExpression(traverser, dotNodeResolver, containingMethod));

    case HqlSqlTokenTypes.AND:
      return Bool.and(generateExpression(traverser, dotNodeResolver, containingMethod), generateExpression(traverser, dotNodeResolver, containingMethod));

    case HqlSqlTokenTypes.NOT:
      return Bool.notExpr(generateExpression(traverser, dotNodeResolver, containingMethod));

    //
    // VALUE EXPRESSIONS
    //

    case HqlSqlTokenTypes.DOT:
      DotNode dotNode = (DotNode) ast;
      traverser.fastForwardToNextSiblingOf(dotNode);
      return dotNodeResolver.resolve(dotNode);

    case HqlSqlTokenTypes.NAMED_PARAM:
      ParameterNode paramNode = (ParameterNode) ast;
      NamedParameterSpecification namedParamSpec = (NamedParameterSpecification) paramNode.getHqlParameterSpecification();
      return Stmt.loadVariable("this").invoke("getParameterValue", namedParamSpec.getName());

    case HqlSqlTokenTypes.QUOTED_STRING:
      return Stmt.loadLiteral(SqlUtil.parseStringLiteral(ast.getText()));

    case HqlSqlTokenTypes.UNARY_MINUS:
      return ArithmeticExpressionBuilder.create(ArithmeticOperator.Subtraction, generateExpression(traverser, dotNodeResolver, containingMethod));

    case HqlSqlTokenTypes.NUM_INT:
    case HqlSqlTokenTypes.NUM_DOUBLE:
    case HqlSqlTokenTypes.NUM_FLOAT:
      // all numeric literals (except longs) are generated as doubles
      // (and correspondingly, all "dot nodes" (entity attributes) are retrieved as doubles)
      // this allows us to compare almost any numeric type to any other numeric type
      // (long and char are the exceptions)
      return Stmt.loadLiteral(Double.valueOf(ast.getText()));

    case HqlSqlTokenTypes.NUM_LONG:
      return Stmt.loadLiteral(Long.valueOf(ast.getText()));

    case HqlSqlTokenTypes.TRUE:
    case HqlSqlTokenTypes.FALSE:
      return Stmt.loadLiteral(Boolean.parseBoolean(ast.getText()));

    case HqlSqlTokenTypes.JAVA_CONSTANT:
      return Stmt.loadLiteral(MVEL.eval(ast.getText()));

    case HqlSqlTokenTypes.METHOD_CALL:
      IdentNode methodNameNode = (IdentNode) traverser.next();
      SqlNode exprList = (SqlNode) traverser.next();
      Statement[] args = new Statement[exprList.getNumberOfChildren()];
      for (int i = 0; i < args.length; i++) {
        args[i] = generateExpression(traverser, dotNodeResolver, containingMethod);
      }
      if ("lower".equals(methodNameNode.getOriginalText())) {
        return Stmt.castTo(String.class, Stmt.load(args[0])).invoke("toLowerCase");
      }
      else if ("upper".equals(methodNameNode.getOriginalText())) {
        return Stmt.castTo(String.class, Stmt.load(args[0])).invoke("toUpperCase");
      }
      else if ("concat".equals(methodNameNode.getOriginalText())) {
        StringBuilderBuilder sb = Implementations.newStringBuilder();
        for (Statement s : args) {
          sb.append(s);
        }
        return Stmt.load(sb).invoke("toString");
      }
      else if ("substring".equals(methodNameNode.getOriginalText())) {
        int uniq = uniqueNumber.incrementAndGet();
        containingMethod.append(Stmt.declareFinalVariable("substrOrig" + uniq, String.class,
                Cast.to(String.class, args[0])));
        containingMethod.append(Stmt.declareFinalVariable("substrStart" + uniq, int.class,
                Arith.expr(Cast.to(Integer.class, args[1]), ArithmeticOperator.Subtraction, 1)));
        if (args.length == 2) {
          return Stmt.loadVariable("substrOrig" + uniq).invoke("substring", Stmt.loadVariable("substrStart" + uniq));
        }
        else if (args.length == 3) {
          containingMethod.append(Stmt.declareFinalVariable("substrEnd" + uniq, int.class,
                  Arith.expr(Cast.to(Integer.class, args[2]), ArithmeticOperator.Addition, Stmt.loadVariable("substrStart" + uniq))));
          return Stmt.loadVariable("substrOrig" + uniq).invoke(
                  "substring", Stmt.loadVariable("substrStart" + uniq), Stmt.loadVariable("substrEnd" + uniq));
        }
        else {
          throw new GenerationException("Found " + args.length + " arguments to concat() function. Expected 2 or 3.");
        }
      }
      throw new UnsupportedOperationException("The JPQL function " + methodNameNode.getOriginalText() + " is not supported");

    default:
      throw new UnexpectedTokenException(ast.getType(), "an expression (boolean, literal, JPQL path, method call, or named parameter)");
    }

    // I keep feeling like this will be useful, but so far it has turned out to be unnecessary:
//    LiteralNode literalNode = (LiteralNode) ast;
//    return Stmt.loadLiteral(((StringRepresentableType<?>) literalNode.getDataType()).fromStringValue(literalNode.getText()));
  }

  private static class UnexpectedTokenException extends RuntimeException {
    UnexpectedTokenException(int actual, String expected) {
      super("Encountered unexpected token " +
            HqlSqlWalker._tokenNames[actual] + " (expected " + expected + ")");
    }
  }

  /**
   * Implementations of this interface provide the ability to resolve a HQL/JPQL
   * DotNode into an Errai codegen Statement that evaluates to an actual value
   * at runtime.
   */
  private interface DotNodeResolver {
    Statement resolve(DotNode dotNode);
  }

  /**
   * Resolves a DotNode to a value by dereferencing a property from a
   * JSONObject. The returned Statement depends on a JSONObject named
   * "candidate" being in the local scope.
   */
  private static class JsonDotNodeResolver implements DotNodeResolver {

    @Override
    public Statement resolve(DotNode dotNode) {
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

      return Stmt.invokeStatic(JsonUtil.class, "basicValueFromJson",
              Stmt.loadVariable("candidate").invoke("get", dotNode.getPropertyPath()),
              requestedType);
    }
  }

  /**
   * Resolves a DotNode to an actual value by obtaining an ErraiAttribute from
   * the ErraiEntityManager and invoking {@link ErraiAttribute#get(Object)} on
   * it. The returned Statement relies on an instance of the Entity type being
   * in the local scope (the name is your choice; pass it to the constructor).
   * Additionally, the returned Statement may rely on one or more variable
   * declarations in its local scope. You are responsible for retrieving these
   * variable declarations (using the {@link #getRequiredLocalVariables()}
   * method) and inserting them in your code somewhere before the returned
   * Statement.
   */
  private static class JavaDotNodeResolver implements DotNodeResolver {

    private final String variableName;
    private final Set<String> generatedClassVariables = new HashSet<String>();
    private final AnonymousClassStructureBuilder containingClass;

    /**
     * Creates a new java-object-based Dot Node resolver that assumes the
     * relative dot node path begins from an in-scope variable with the given
     * name.
     *
     * @param variableName
     *          The name of the variable (which is a reference to a JPA managed
     *          type) to resolve dot nodes from.
     * @param containingClass
     *          The class that will contain the statements generated by this
     *          resolver. As a side effect of calling {@link #resolve(DotNode)},
     *          private final fields may be added to this class (these fields
     *          will be referenced by the returned statement).
     *          <p>
     *          Can be null, in which case calls to {@link #resolve(DotNode)}
     *          will have no side effects. This is useful in the case where you
     *          have two or more resolvers generating statements for different
     *          variables but the same DotNodes in the same class.
     */
    public JavaDotNodeResolver(String variableName, AnonymousClassStructureBuilder containingClass) {
      this.variableName = Assert.notNull(variableName);
      this.containingClass = containingClass;
    }

    @Override
    public Statement resolve(DotNode dotNode) {
      Class<?> lhsType = dotNode.getLhs().getDataType().getReturnedClass();

      // ensure the attribute is available as a field of the comparator
      String attrVarName = dotNode.getPath().replace('.', '_') + "_attr";
      if (containingClass != null && !generatedClassVariables.contains(attrVarName)) {
        generatedClassVariables.add(attrVarName);
        containingClass.privateField(attrVarName, ErraiAttribute.class)
          .modifiers(Modifier.Final)
          .initializesWith(
                  Stmt.nestedCall(new StringStatement("getMetamodel()", MetaClassFactory.get(ErraiMetamodel.class)))
                  .invoke("entity", Stmt.loadLiteral(lhsType))
                  .invoke("getAttribute", dotNode.getPropertyPath()))
          .finish();
      }

      // XXX need a StringStatement here because codegen can't see fields of anonymous inner classes. (ERRAI-363)
      return Stmt.nestedCall(new StringStatement(attrVarName, MetaClassFactory.get(ErraiAttribute.class)))
              .invoke("get", Stmt.loadVariable(variableName));
    }
  }
}
