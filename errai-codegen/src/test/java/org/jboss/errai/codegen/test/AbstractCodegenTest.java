/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.codegen.test;

import com.google.testing.compile.CompilationRule;
import org.jboss.errai.codegen.builder.impl.StatementBuilder;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.codegen.util.GenUtil;
import org.junit.Before;
import org.junit.Rule;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Base class for all {@link StatementBuilder} tests.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractCodegenTest {
  @Rule
  public CompilationRule rule = new CompilationRule();

  protected Elements elements;
  protected Types types;

  @Before
  public void onBefore() {
    GenUtil.setPermissiveMode(false);
    MetaClassFactory.getMetaClassCache().clear();
    elements = rule.getElements();
    types = rule.getTypes();
    APTClassUtil.init(types, elements);
  }

  /**
   * assertEquals, less sensitive to indentation differences.
   * TODO compare parsed ASTs instead?
   *
   * @param expected
   * @param actual
   */
  protected static void assertEquals(String expected, String actual) {
    org.junit.Assert.assertEquals(expected.replaceAll("\\s+", " ").trim(),
        actual.replaceAll("\\s+", " ").trim());
  }

  protected static void assertEquals(String message, String expected, String actual) {
    org.junit.Assert.assertEquals(message, expected == null ? null : expected.replaceAll("\\s+", " ").trim(),
        actual == null ? null : actual.replaceAll("\\s+", " ").trim());
  }
}
