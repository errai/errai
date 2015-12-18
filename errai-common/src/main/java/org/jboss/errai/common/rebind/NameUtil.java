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

package org.jboss.errai.common.rebind;

/**
 * Utility methods for naming generated classes.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class NameUtil {

  /**
   * @return The given fully qualified class name with underscores replacing
   *         character not allowed in identifiers.
   */
  public static String derivedIdentifier(final String fullyQualifiedClassName) {
    return fullyQualifiedClassName.replace('.', '_').replace('$', '_');
  }

  /**
   * @param derivdedIdentifier A value returned from {@link #derivedIdentifier(String)}.
   * @return The derivedIdentifier with package parts shortened to initials.
   */
  public static String shortenDerivedIdentifier(final String derivdedIdentifier) {
    return shortenDerivedIdentifier(derivdedIdentifier, "_");
  }

  private static String shortenDerivedIdentifier(final String derivedIdentifier, final String delimiter) {
    final String[] parts = derivedIdentifier.split(delimiter);
    final StringBuilder builder = new StringBuilder();
    boolean haveSeenUpperCase = false;
    for (final String part : parts) {
      if (haveSeenUpperCase || Character.isUpperCase(part.charAt(0))) {
        builder.append(part);
        haveSeenUpperCase = true;
      }
      else {
        builder.append(part.charAt(0));
      }
      builder.append('_');
    }
    builder.delete(builder.length() - 1, builder.length());

    return builder.toString();
  }

}
