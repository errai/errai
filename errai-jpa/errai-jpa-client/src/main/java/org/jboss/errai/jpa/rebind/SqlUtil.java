package org.jboss.errai.jpa.rebind;

public class SqlUtil {

  private SqlUtil() {}

  /**
   * "Unquotes" the given SQL String literal, and transforms the {@code ''}
   * escape sequence to {@code '}.
   *
   * @param quotedString
   *          A string literal from a SQL/HQL/JPQL query. The first and last
   *          characters must be {@code '} (single quote).
   * @return the given string with quotes removed and escape sequences removed.
   */
  public static String parseStringLiteral(String quotedString) {
    if ( ! (quotedString.charAt(0) == '\'' && quotedString.charAt(quotedString.length() - 1) == '\'')) {
      throw new IllegalArgumentException("Not a valid quoted string literal: [" + quotedString + "]");
    }
    return quotedString.substring(1, quotedString.length() - 1).replace("''", "'");
  }
}
