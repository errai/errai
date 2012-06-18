package org.jboss.errai.jpa.client.local;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.jboss.errai.common.client.framework.Assert;

import com.google.common.collect.ImmutableBiMap;

/**
 * Base implementation of the JPA TypedQuery interface for Errai. This class is
 * expected to be subclassed by generated code that is created during the GWT
 * compiler's rebind phase.
 *
 * @param <X> The result type of this query
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class ErraiTypedQuery<X> implements TypedQuery<X> {

  private int maxResults = Integer.MAX_VALUE;
  private int firstResult = 0;
  private Map<String, Object> hints = new HashMap<String, Object>();

  /**
   * The parameters that are defined for this query.
   */
  private final ImmutableBiMap<String, Parameter<?>> parameters;

  /**
   * The values of the parameters defined for this query.
   */
  private final Map<Parameter<?>, Object> paramValues = new HashMap<Parameter<?>, Object>();

  private FlushModeType flushMode = FlushModeType.AUTO;
  private LockModeType lockMode = LockModeType.OPTIMISTIC_FORCE_INCREMENT;
  private final ErraiEntityManager em;
  private final Class<X> resultType;
  private final EntityJsonMatcher matcher;

  /**
   *
   * @param entityManager
   *          The EntityManager within whose scope this query will be executed.
   * @param actualResultType
   *          The result type of this query. Must be an entity type known to
   *          {@code entityManager}.
   * @param matcher
   *          The matcher that chooses which entity instances to accept.
   * @param parameters
   *          The parameters of this query. The iteration order of the
   *          parameters in the map must be their numeric order in the query
   *          (ImmutableMap has a stable iteration order).
   */
  protected ErraiTypedQuery(
          ErraiEntityManager entityManager,
          Class<X> actualResultType,
          EntityJsonMatcher matcher,
          ImmutableBiMap<String, Parameter<?>> parameters) {
    this.em = Assert.notNull(entityManager);
    this.resultType = Assert.notNull(actualResultType);
    this.matcher = Assert.notNull(matcher);
    this.parameters = Assert.notNull(parameters);
  }

  /**
   * Works like {@link #getParameter(String)}, but throws an exception if the
   * parameter does not exist.
   *
   * @param name
   * @return the Parameter of this query that has the given name. Never null.
   * @throws IllegalArgumentException
   *           if this query doesn't have a parameter with the given name.
   */
  private Parameter<?> getExistingParameter(String name) {
    Parameter<?> param = getParameter(name);
    if (param == null) {
      throw new IllegalArgumentException("Unknown query parameter \"" + name + "\"");
    }
    return param;
  }

  /**
   * Verifies that the given parameter type is assignable to the given expected
   * type.
   *
   * @param expectedType
   *          The expected parameter type of the parameter.
   * @param parameter
   *          The parameter itself. Null is permitted.
   * @return {@code parameter}. Will be null if {@code parameter} was null.
   * @throws IllegalArgumentException
   *           if the expected type is not assignable from the actual type of
   *           the given parameter.
   */
  private <T> Parameter<T> verifyTypeCompatibility(Parameter<?> parameter, Class<T> expectedType) {
    if (parameter == null) return null;
    if (!expectedType.isAssignableFrom(parameter.getParameterType())) {
      throw new IllegalArgumentException(
              "Requested type \"" + expectedType + "\" is not assignable from actual type \"" +
              parameter.getParameterType() + "\" in parameter \"" + parameter.getName() + "\"");
    }
    @SuppressWarnings("unchecked") Parameter<T> checkedParam = (Parameter<T>) parameter;
    return checkedParam;
  }

  // ========= JPA API below this line

  @Override
  public int executeUpdate() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public int getMaxResults() {
    return maxResults;
  }

  @Override
  public int getFirstResult() {
    return firstResult;
  }

  @Override
  public Map<String, Object> getHints() {
    return hints;
  }

  @Override
  public Set<Parameter<?>> getParameters() {
    return parameters.values();
  }

  @Override
  public Parameter<?> getParameter(String name) {
    return parameters.get(name);
  }

  @Override
  public <T> Parameter<T> getParameter(String name, Class<T> type) {
    Parameter<?> parameter = parameters.get(name);
    return verifyTypeCompatibility(parameter, type);
  }

  @Override
  public Parameter<?> getParameter(int position) {
    return parameters.values().asList().get(position);
  }

  @Override
  public <T> Parameter<T> getParameter(int position, Class<T> type) {
    return verifyTypeCompatibility(getParameter(position), type);
  }

  @Override
  public boolean isBound(Parameter<?> param) {
    return paramValues.containsKey(param);
  }

  @SuppressWarnings("unchecked") // this only safe if clients of this class have no type warnings
  @Override
  public <T> T getParameterValue(Parameter<T> param) {
    return (T) paramValues.get(param);
  }

  @Override
  public Object getParameterValue(String name) {
    Parameter<?> parameter = getExistingParameter(name);
    return getParameterValue(parameter);
  }

  @Override
  public Object getParameterValue(int position) {
    return getParameterValue(getParameter(position));
  }

  @Override
  public FlushModeType getFlushMode() {
    return flushMode;
  }

  @Override
  public LockModeType getLockMode() {
    return lockMode;
  }

  @Override
  public <T> T unwrap(Class<T> cls) {
    throw new PersistenceException("Can't unwrap to " + cls);
  }

  @Override
  public List<X> getResultList() {
    return em.findAll(em.getMetamodel().entity(resultType), matcher);
  }

  @Override
  public X getSingleResult() {
    List<X> resultList = getResultList();
    if (resultList.isEmpty()) {
      throw new NoResultException();
    } else if (resultList.size() > 1) {
      throw new NonUniqueResultException("Query produced " + resultList.size() + " results (expected 1)");
    }
    return resultList.get(0);
  }

  @Override
  public TypedQuery<X> setMaxResults(int maxResult) {
    maxResults = maxResult;
    return this;
  }

  @Override
  public TypedQuery<X> setFirstResult(int startPosition) {
    firstResult = startPosition;
    return this;
  }

  @Override
  public TypedQuery<X> setHint(String hintName, Object value) {
    hints.put(hintName, value);
    return this;
  }

  @Override
  public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
    paramValues.put(param, value);
    return this;
  }

  @Override
  public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value,
          TemporalType temporalType) {
    paramValues.put(param, value);
    return this;
  }

  @Override
  public TypedQuery<X> setParameter(Parameter<Date> param, Date value,
          TemporalType temporalType) {
    paramValues.put(param, value);
    return this;
  }

  @Override
  public TypedQuery<X> setParameter(String name, Object value) {
    Parameter<?> param = getExistingParameter(name);
    paramValues.put(param, value);
    return this;
  }

  @Override
  public TypedQuery<X> setParameter(String name, Calendar value,
          TemporalType temporalType) {
    Parameter<?> param = getExistingParameter(name);
    paramValues.put(param, value);
    return this;
  }

  @Override
  public TypedQuery<X> setParameter(String name, Date value,
          TemporalType temporalType) {
    Parameter<?> param = getExistingParameter(name);
    paramValues.put(param, value);
    return this;
  }

  @Override
  public TypedQuery<X> setParameter(int position, Object value) {
    paramValues.put(getParameter(position), value);
    return this;
  }

  @Override
  public TypedQuery<X> setParameter(int position, Calendar value,
          TemporalType temporalType) {
    paramValues.put(getParameter(position), value);
    return this;
  }

  @Override
  public TypedQuery<X> setParameter(int position, Date value,
          TemporalType temporalType) {
    paramValues.put(getParameter(position), value);
    return this;
  }

  @Override
  public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
    this.flushMode = flushMode;
    return this;
  }

  @Override
  public TypedQuery<X> setLockMode(LockModeType lockMode) {
    this.lockMode = lockMode;
    return this;
  }

}
