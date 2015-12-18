/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.jpa.client.local.backend;

import java.util.Collection;

/**
 * Non-instantiable utility methods for comparing two or more values.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class Comparisons {

  /**
   * Tests two potentially null object references for equality using approximate
   * JPQL/SQL null semantics.
   * <p>
   * Specifically, this method returns true if and only if both arguments are
   * non-null and either of the following conditions are met:
   * <ol>
   * <li>{@code o1 == o2}
   * <li>{@code o1.equals(o2)}
   * </ol>
   *
   * @param o1
   *          One object to compare. Null is permitted.
   * @param o2
   *          The other object to compare. Null is permitted.
   * @return true if o1 and o2 are equal (either by reference equality or by
   *         Object.equals()); false otherwise
   */
  // MAINTAINERS BEWARE: Errai JPA generates code that uses this method.
  public static boolean nullSafeEquals(Object o1, Object o2) {
    return o1 != null && o2 != null && (o1 == o2 || o1.equals(o2));
  }

  /**
   * Tests if one potentially null object reference is greater than another.
   *
   * @param o1
   *          One object to compare. Null is permitted.
   * @param o2
   *          The other object to compare. Null is permitted.
   * @return true if {@code o1 > o2} (either by primitive comparison or by
   *         Comparable.compareTo()); false otherwise
   */
  // MAINTAINERS BEWARE: Errai JPA generates code that uses this method.
  public static boolean nullSafeGreaterThan(Object o1, Object o2) {
    if (o1 == null || o2 == null) return false;
    if (o1 instanceof Number && o2 instanceof Number) {
      return ((Number) o1).doubleValue() > ((Number) o2).doubleValue();
    }
    if (o1 instanceof Comparable<?>) {
      return ((Comparable<Object>) o1).compareTo(o2) > 0;
    }
    throw new IllegalArgumentException(
            "Can't compare an instance of " + o1.getClass() + " to an instance of " + o2.getClass());
  }

  /**
   * Tests if one potentially null object reference is greater than another.
   *
   * @param o1
   *          One object to compare. Null is permitted.
   * @param o2
   *          The other object to compare. Null is permitted.
   * @return true if {@code o1 > o2} (either by primitive comparison or by
   *         Comparable.compareTo()); false otherwise
   */
  // MAINTAINERS BEWARE: Errai JPA generates code that uses this method.
  public static boolean nullSafeGreaterThanOrEqualTo(Object o1, Object o2) {
    if (o1 == null || o2 == null) return false;
    if (o1 instanceof Number && o2 instanceof Number) {
      return ((Number) o1).doubleValue() >= ((Number) o2).doubleValue();
    }
    if (o1 instanceof Comparable<?>) {
      return ((Comparable<Object>) o1).compareTo(o2) >= 0;
    }
    throw new IllegalArgumentException(
            "Can't compare an instance of " + o1.getClass() + " to an instance of " + o2.getClass());
  }

  /**
   * Tests if one potentially null object reference is greater than another.
   *
   * @param o1
   *          One object to compare. Null is permitted.
   * @param o2
   *          The other object to compare. Null is permitted.
   * @return true if {@code o1 > o2} (either by primitive comparison or by
   *         Comparable.compareTo()); false otherwise
   */
  // MAINTAINERS BEWARE: Errai JPA generates code that uses this method.
  public static boolean nullSafeLessThan(Object o1, Object o2) {
    if (o1 == null || o2 == null) return false;
    if (o1 instanceof Number && o2 instanceof Number) {
      return ((Number) o1).doubleValue() < ((Number) o2).doubleValue();
    }
    if (o1 instanceof Comparable<?>) {
      return ((Comparable<Object>) o1).compareTo(o2) < 0;
    }
    throw new IllegalArgumentException(
            "Can't compare an instance of " + o1.getClass() + " to an instance of " + o2.getClass());
  }

  /**
   * Tests if one potentially null object reference is greater than another.
   *
   * @param o1
   *          One object to compare. Null is permitted.
   * @param o2
   *          The other object to compare. Null is permitted.
   * @return true if {@code o1 > o2} (either by primitive comparison or by
   *         Comparable.compareTo()); false otherwise
   */
  // MAINTAINERS BEWARE: Errai JPA generates code that uses this method.
  public static boolean nullSafeLessThanOrEqualTo(Object o1, Object o2) {
    if (o1 == null || o2 == null) return false;
    if (o1 instanceof Number && o2 instanceof Number) {
      return ((Number) o1).doubleValue() <= ((Number) o2).doubleValue();
    }
    if (o1 instanceof Comparable<?>) {
      return ((Comparable<Object>) o1).compareTo(o2) <= 0;
    }
    throw new IllegalArgumentException(
            "Can't compare an instance of " + o1.getClass() + " to an instance of " + o2.getClass());
  }

  /**
   * Compares one potentially null Comparable to another.
   *
   * @param c1
   *          One object to compare. Null is permitted.
   * @param c2
   *          The other object to compare. Null is permitted.
   * @return 0 if c1 and c2 are both null; -1 if c1 is null and c2 is not; 1 if
   *         c1 is not null and c2 is. Otherwise returns c1.compareTo(c2).
   */
  // MAINTAINERS BEWARE: Errai JPA generates code that uses this method.
  public static int nullSafeCompare(Comparable c1, Comparable c2) {
    if (c1 == null && c2 == null) return 0;
    if (c1 == null && c2 != null) return -1;
    if (c1 != null && c2 == null) return 1;
    return c1.compareTo(c2);
  }

  /**
   * Tests if the first argument is equal to any of the remaining arguments.
   * Equality is tested using {@link #nullSafeEquals(Object, Object)}.
   * <p>
   * <b>Special Case</b><br>
   * If the collection has only one item in it, and that item is assignable to
   * Collection, then that collection will be searched rather than being treated
   * as a single scalar value. This allows correct behaviour for a JPQL query
   * {@code SELECT x FROM MyClass x WHERE x.prop IN :param} and {@code param}
   * resolves to a collection value at runtime.
   *
   * @param thingToCompare
   *          The item to compare against the remaining arguments.
   * @param collection
   *          One or more items to test for equality with {@code thingToCompare}
   *          .
   * @return True if there is an item in {@code collection} that compares equal
   *         with {@code thingToCompare}. False otherwise.
   */
  // MAINTAINERS BEWARE: Errai JPA generates code that uses this method.
  public static boolean in(Object thingToCompare, Object[] collection) {
    // special case: if the collection only has one member and it's a collection, we unwrap it.
    // this provides the required JPQL behaviour.
    if (collection.length == 1 && collection[0] instanceof Collection) {
      for (Object o : (Collection<?>) collection[0]) {
        if (nullSafeEquals(thingToCompare, o)) {
          return true;
        }
      }
    }
    else {
      for (Object o : collection) {
        if (nullSafeEquals(thingToCompare, o)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks of the given value matches the given JPQL wildcard pattern.
   *
   * @param value
   *          The string value to test. May be null.
   * @param pattern
   *          The JPQL pattern to test against. Special characters are "{@code _}
   *          ", which matches any single character, and "{@code %}", which
   *          matches 0 or more characters.
   */
  // MAINTAINERS BEWARE: Errai JPA generates code that uses this method.
  public static Boolean like(String value, String pattern, String escapeChar) {
    if (value == null || pattern == null) {
      return null;
    }
    return value.matches(sqlWildcardToRegex(pattern, escapeChar));
  }

  private static String sqlWildcardToRegex(String pattern, String escapeChar) {
    char esc;
    if (escapeChar == null) {
      esc = 'x'; // (not used in this case)
    }
    else if (escapeChar.length() != 1) {
      throw new IllegalArgumentException("In LIKE x ESCAPE e, e must be a single-character string");
    }
    else {
      esc = escapeChar.charAt(0);
    }

    StringBuilder sb = new StringBuilder(pattern.length());
    for (int i = 0; i < pattern.length(); i++) {
      char ch = pattern.charAt(i);
      if (ch == esc) {
        // advance to next character and don't treat as wildcard
        ch = pattern.charAt(++i);
      }
      else if (ch == '_') {
        // wildcard: match any one char
        sb.append('.');
        continue;
      }
      else if (ch == '%') {
        // wildcard: match 0 or more chars
        sb.append(".*");
        continue;
      }

      // append non-jpql-wildcard char (escaping if it's a special regex char)
      sb.append(escapeRegexChar(ch));
    }
    return sb.toString();
  }

  public static String escapeRegexChar(char ch) {
    StringBuilder sb = new StringBuilder(2);
    switch (ch) {
    case '.':
    case '\\':
    case '+':
    case '*':
    case '?':
    case '[':
    case '^':
    case ']':
    case '$':
    case '(':
    case ')':
    case '{':
    case '}':
    case '=':
    case '!':
    case '<':
    case '>':
    case '|':
    case ':':
    case '-':
      sb.append('\\');
      // FALLTHROUGH

    default:
      sb.append(ch);
    }
    return sb.toString();
  }
}
