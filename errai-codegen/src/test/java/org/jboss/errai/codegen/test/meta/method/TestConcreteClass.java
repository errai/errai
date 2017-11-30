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

package org.jboss.errai.codegen.test.meta.method;

import java.util.List;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
class TestConcreteClass<Y> extends TestAbstractClass<String, Long, Y> implements TestConcreteInterface {

  TestConcreteClass(final String s) {
    super(s);
  }

  TestConcreteClass(final Long aLong) {
    super(aLong);
  }

  TestConcreteClass(final String[] strings) {
    super(strings);
  }

  TestConcreteClass(final String s, final Y y) {
    super(s, y);
  }

  TestConcreteClass(final String s, final Long l, final Y y) {
    super(s, l, y);
  }

  TestConcreteClass(final Y[] y) {
    super("s");
  }

  @Override
  public String foo() {
    return "foo";
  }

  public <X> X bar() {
    return null;
  }

  public <X> X[] genericArrayReturn() {
    return null;
  }

  public <X extends Long> X[] boundedGenericArrayReturn() {
    return null;
  }

  public <X extends Long> X boundedBar() {
    return null;
  }

  public List<?> wildcardBar() {
    return null;
  }

  public List<String> concreteBar() {
    return null;
  }

  public void par(final String string) {
  }

  public <X> void unboundedPar(final String string, final X foo) {
  }

  public <X> void genericArrayParameter(X[] xs) {
  }

  public <X extends Long> void boundedGenericArrayParameter(X[] xs) {
  }

  public <X extends Long> void boundedPar(final String string, final X foo) {
  }
}
