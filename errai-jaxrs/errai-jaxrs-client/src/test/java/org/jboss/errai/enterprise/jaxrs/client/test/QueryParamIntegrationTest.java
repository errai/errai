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
        new AssertionCallback<Long>("@GET with @QueryParam failed", 1l)).getWithQueryParam(1l);
  }

  @Test
  public void testGetWithNullQueryParam() {
    call(QueryParamTestService.class,
        new AssertionCallback<String>("@GET with @QueryParam failed", "")).getWithStringQueryParam(null);
  }
  
  @Test
  public void testGetWithEncodedQueryParam() {
    String queryParamSpecialChars = "?<>!@#$%^\\&*()-+;:''\\/.,";
    call(QueryParamTestService.class,
        new AssertionCallback<String>("@GET w/ encoded @QueryParam failed", queryParamSpecialChars))
        .getWithStringQueryParam(queryParamSpecialChars);
  }

  @Test
  public void testGetWithMultipleQueryParams() {
    call(QueryParamTestService.class,
        new AssertionCallback<String>("@GET with @QueryParams failed", "1/2")).getWithMultipleQueryParams(1l, 2l);
  }

  @Test
  public void testGetWithQueryParamListOfLongs() {
    List<Long> longs = Arrays.asList(1l,2l,3l);
    call(QueryParamTestService.class,
        new AssertionCallback<List<Long>>("@GET with List<Long> as @QueryParam failed", longs)).getWithQueryParamListOfLongs(longs);
  }
  
  @Test
  public void testGetWithQueryParamListOfLongsPassingNull() {
    call(QueryParamTestService.class,
        new AssertionCallback<List<Long>>("@GET with List<Long> as @QueryParam failed", Collections.<Long>emptyList())).getWithQueryParamListOfLongs(null);
  }
  
  @Test
  public void testGetWithQueryParamListOfEnums() {
    List<EnumMapEntity.SomeEnum> enums = Arrays.asList(EnumMapEntity.SomeEnum.ENUM_VALUE);
    call(QueryParamTestService.class,
        new AssertionCallback<List<EnumMapEntity.SomeEnum>>(
                "@GET with List<Enum> as @QueryParam failed", enums)).getWithQueryParamListOfEnums(enums);
  }
  
  @Test
  public void testGetWithQueryParamSetOfStrings() {
    Set<String> strings = new HashSet<String>(Arrays.asList("1", "2", "3"));
    call(QueryParamTestService.class,
        new AssertionCallback<Set<String>>("@GET with Set<String> as @QueryParams failed", strings)).getWithQueryParamSetOfStrings(strings);
  }
  
  @Test
  public void testGetWithQueryParamListOfStrings() {
    List<String> strings = new ArrayList<String>();
    strings.add("1");
    strings.add("2");
    strings.add("3");
    call(QueryParamTestService.class,
        new AssertionCallback<List<String>>("@GET with List<String> as @QueryParams failed", strings)).getWithQueryParamListOfStrings(strings);
  }
  
  @Test
  public void testGetWithMultipleQueryParamListOfStrings() {
    List<String> list1 = Arrays.asList("1", "2", "3");
    List<String> list2 = Arrays.asList("5", "6", "7");
    List<String> expected = Arrays.asList("1", "2", "3", "4", "5", "6", "7");
    
    call(QueryParamTestService.class,
        new AssertionCallback<List<String>>("@GET with List<String> as @QueryParams failed", expected))
        .getWithMultipleQueryParamListOfStrings(list1, "4", list2);
  }
  
  @Test
  public void testGetWithMultipleQueryParamsAndListOfStrings() {
    List<String> list = Arrays.asList("2", "3", "4");
    List<String> expected = Arrays.asList("1", "2", "3", "4", "5");
    
    call(QueryParamTestService.class,
        new AssertionCallback<List<String>>("@GET with List<String> as @QueryParams failed", expected))
        .getWithMultipleQueryParamsAndListOfStrings("1", list, "5");
  }
  
  @Test
  public void testPostWithQueryParam() {
    call(QueryParamTestService.class,
        new AssertionCallback<Integer>("@POST with @QueryParam failed", 1)).postWithQueryParam("", 1);
  }

  @Test
  public void testPutWithQueryParam() {
    call(QueryParamTestService.class,
        new AssertionCallback<Double>("@PUT with @QueryParam failed", 1.0)).putWithQueryParam(1.0);
  }

  @Test
  public void testDeleteWithQueryParam() {
    call(QueryParamTestService.class,
        new AssertionCallback<Short>("@DELETE with @QueryParam failed", (short) 1)).deleteWithQueryParam((short) 1);
  }

  @Test
  public void testHeadWithQueryParam() {
    call(QueryParamTestService.class,
        new AssertionResponseCallback("@HEAD with @QueryParam failed", Response.SC_NO_CONTENT)).headWithQueryParam(1l);
  }
}
