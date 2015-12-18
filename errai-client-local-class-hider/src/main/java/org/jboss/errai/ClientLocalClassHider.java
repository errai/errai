/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.regex.Pattern;

/**
 * A class file "transformer" that ignores the original class definition,
 * instead generating a class that is completely empty.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class ClientLocalClassHider implements ClassFileTransformer {

  private final Pattern hiddenClassNamePattern;
  private final boolean debug;

  public ClientLocalClassHider(Pattern hiddenClassNamePattern, boolean debug) {
    this.hiddenClassNamePattern = hiddenClassNamePattern;
    this.debug = debug;

    if (debug) {
      System.out.println("client-local-class-hider: hiding classes that match the regular expression: " + hiddenClassNamePattern);
    }
  }

  /**
   * Generates a class with a no-args public constructor.
   *
   * @param loader
   *          ignored.
   * @param className
   *          The generated class has this package and name.
   * @param classBeingRedefined
   *          ignored.
   * @param protectionDomain
   *          ignored.
   * @param classfileBuffer
   *          ignored.
   * @return bytecode for an empty class whose package and class name are
   *         determined by the {@code className} parameter.
   */
  public byte[] transform(ClassLoader loader, String className,
      Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
      byte[] classfileBuffer) throws IllegalClassFormatException {

    if (!hiddenClassNamePattern.matcher(className).matches()) {
      if (debug) {
        System.out.println("client-local-class-hider: not hiding " + className);
      }
      return null;
    }

    if (debug) {
      System.out.println("client-local-class-hider: hiding " + className);
    }

    return EmtpyClassGenerator.generateEmptyClass(className);
  }
}
