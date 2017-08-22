/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.codegen.apt.test;

import com.google.testing.compile.CompilationRule;
import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.codegen.test.AbstractCodegenTest;
import org.junit.Before;
import org.junit.Rule;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public abstract class ErraiAptTest extends AbstractCodegenTest {

  @Rule
  public CompilationRule rule = new CompilationRule();

  protected Elements elements;
  protected Types types;

  @Before
  public void before() {
    elements = rule.getElements();
    types = rule.getTypes();
    APTClassUtil.init(types, elements);
  }

  protected TypeElement getTypeElement(final Class<?> clazz) {
    return elements.getTypeElement(clazz.getCanonicalName());
  }

}
