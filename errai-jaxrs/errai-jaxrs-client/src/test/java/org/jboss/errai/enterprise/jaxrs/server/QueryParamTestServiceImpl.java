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

package org.jboss.errai.enterprise.jaxrs.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.errai.enterprise.jaxrs.client.shared.QueryParamTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.EnumMapEntity.SomeEnum;

/**
 * Implementation of {@link QueryParamTestService} returning test data.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class QueryParamTestServiceImpl implements QueryParamTestService {

  @Override
  public Long getWithQueryParam(long id) {
    return id;
  }

  @Override
  public String getWithStringQueryParam(String id) {
    return id;
  }

  @Override
  public String getWithMultipleQueryParams(long id1, long id2) {
    return "" + id1 + "/" + id2;
  }

  @Override
  public List<Long> getWithQueryParamListOfLongs(List<Long> id) {
    return id;
  }

  @Override
  public Set<String> getWithQueryParamSetOfStrings(Set<String> id) {
    return id;
  }

  @Override
  public Set<String> getWithQueryParamSetOfStringsTreeSet(Set<String> id) {
    return new TreeSet<>(id);
  }

  @Override
  public List<String> getWithQueryParamListOfStrings(List<String> id) {
    return id;
  }

  @Override
  public List<String> getWithMultipleQueryParamListOfStrings(List<String> id1, String id, List<String> id2) {
     List<String> list = new ArrayList<String>();
     list.addAll(id1);
     list.add(id);
     list.addAll(id2);
     
     return list;
  }
  
  @Override
  public List<String> getWithMultipleQueryParamsAndListOfStrings(String id1, List<String> id2, String id3) {
    List<String> list = new ArrayList<String>();
    list.add(id1);
    list.addAll(id2);
    list.add(id3);
    
    return list;
  }

  @Override
  public int postWithQueryParam(String entity, int id) {
    return id;
  }

  @Override
  public Double putWithQueryParam(Double id) {
    return id;
  }

  @Override
  public short deleteWithQueryParam(short id) {
    return id;
  }

  @Override
  public void headWithQueryParam(long id) {}

  @Override
  public List<SomeEnum> getWithQueryParamListOfEnums(List<SomeEnum> id) {
    return id;
  }

}
