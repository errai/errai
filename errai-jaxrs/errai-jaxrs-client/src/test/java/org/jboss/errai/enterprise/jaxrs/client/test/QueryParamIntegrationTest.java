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

package org.jboss.errai.enterprise.jaxrs.client.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.QueryParamTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.EnumMapEntity;
import org.junit.Test;

import com.google.gwt.http.client.Response;

/**
 * Testing query parameters.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class QueryParamIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testGetWithQueryParam() {
    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@GET with @QueryParam failed", 1l)).getWithQueryParam(1l);
  }

  @Test
  public void testGetWithNullQueryParam() {
    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@GET with @QueryParam failed", "")).getWithStringQueryParam(null);
  }

  @Test
  public void testGetWithEncodedQueryParam() {
    final String queryParamSpecialChars = "?<>!@#$%^\\&*()-+;:''\\/.,";
    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@GET w/ encoded @QueryParam failed", queryParamSpecialChars))
        .getWithStringQueryParam(queryParamSpecialChars);
  }

  @Test
  public void testGetWithMultipleQueryParams() {
    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@GET with @QueryParams failed", "1/2")).getWithMultipleQueryParams(1l, 2l);
  }

  @Test
  public void testGetWithQueryParamListOfLongs() {
    final List<Long> longs = Arrays.asList(1l,2l,3l);
    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@GET with List<Long> as @QueryParam failed", longs)).getWithQueryParamListOfLongs(longs);
  }

  @Test
  public void testGetWithQueryParamListOfLongsPassingNull() {
    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@GET with List<Long> as @QueryParam failed", Collections.<Long>emptyList())).getWithQueryParamListOfLongs(null);
  }

  @Test
  public void testGetWithQueryParamListOfEnums() {
    final List<EnumMapEntity.SomeEnum> enums = Arrays.asList(EnumMapEntity.SomeEnum.ENUM_VALUE);
    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>(
                "@GET with List<Enum> as @QueryParam failed", enums)).getWithQueryParamListOfEnums(enums);
  }

  @Test
  public void testGetWithQueryParamSetOfStrings() {
    final Set<String> strings = new HashSet<>(Arrays.asList("1", "2", "3"));
    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@GET with Set<String> as @QueryParams failed", strings)).getWithQueryParamSetOfStrings(strings);
  }

  @Test
  public void testGetWithQueryParamSetOfStringsTreeSet() {
    final Set<String> strings = new TreeSet<>(Arrays.asList("1", "2", "3"));
    call(QueryParamTestService.class,
            new SimpleAssertionCallback<>("@GET with Set<String> as @QueryParams failed", strings)).getWithQueryParamSetOfStringsTreeSet(strings);
  }

  @Test
  public void testGetWithQueryParamListOfStrings() {
    final List<String> strings = new ArrayList<>();
    strings.add("1");
    strings.add("2");
    strings.add("3");
    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@GET with List<String> as @QueryParams failed", strings)).getWithQueryParamListOfStrings(strings);
  }

  @Test
  public void testGetWithMultipleQueryParamListOfStrings() {
    final List<String> list1 = Arrays.asList("1", "2", "3");
    final List<String> list2 = Arrays.asList("5", "6", "7");
    final List<String> expected = Arrays.asList("1", "2", "3", "4", "5", "6", "7");

    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@GET with List<String> as @QueryParams failed", expected))
        .getWithMultipleQueryParamListOfStrings(list1, "4", list2);
  }

  @Test
  public void testGetWithMultipleQueryParamsAndListOfStrings() {
    final List<String> list = Arrays.asList("2", "3", "4");
    final List<String> expected = Arrays.asList("1", "2", "3", "4", "5");

    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@GET with List<String> as @QueryParams failed", expected))
        .getWithMultipleQueryParamsAndListOfStrings("1", list, "5");
  }

  @Test
  public void testPostWithQueryParam() {
    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@POST with @QueryParam failed", 1)).postWithQueryParam("", 1);
  }

  @Test
  public void testPutWithQueryParam() {
    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@PUT with @QueryParam failed", 1.0)).putWithQueryParam(1.0);
  }

  @Test
  public void testDeleteWithQueryParam() {
    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@DELETE with @QueryParam failed", (short) 1)).deleteWithQueryParam((short) 1);
  }

  @Test
  public void testHeadWithQueryParam() {
    call(QueryParamTestService.class,
        new SimpleAssertionCallback<>("@HEAD with @QueryParam failed", null)).headWithQueryParam(1l);
  }
}
