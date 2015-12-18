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

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.errai.enterprise.jaxrs.client.shared.JacksonTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.BigNumberEntity;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.ByteArrayTestWrapper;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.EnumMapEntity;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.NumberEntity;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.User;

/**
 * Implementation of {@link JacksonTestService}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JacksonTestServiceImpl implements JacksonTestService {

  @Override
  public String postJackson(String jackson) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      User user = mapper.readValue(jackson, User.class);
      return mapper.writeValueAsString(user);
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String postJacksonList(String jackson) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      @SuppressWarnings("unchecked")
      List<User> users = mapper.readValue(jackson, List.class);
      return mapper.writeValueAsString(users);
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String postJacksonListOfBytes(String jackson) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      @SuppressWarnings("unchecked")
      List<Byte> users = mapper.readValue(jackson, List.class);
      return mapper.writeValueAsString(users);
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String postJacksonPortableWithByteArray(String jackson) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      ByteArrayTestWrapper entity = mapper.readValue(jackson, ByteArrayTestWrapper.class);
      return mapper.writeValueAsString(entity);
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String postJacksonMap(String jackson) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      @SuppressWarnings("unchecked")
      Map<String, User> users = mapper.readValue(jackson, Map.class);
      return mapper.writeValueAsString(users);
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String postJacksonPortableWithBigDecimal(String jackson) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      BigNumberEntity entity = mapper.readValue(jackson, BigNumberEntity.class);
      return mapper.writeValueAsString(entity);
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String postJacksonPortableWithEnumMapEntity(String jackson) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      EnumMapEntity entity = mapper.readValue(jackson, EnumMapEntity.class);
      return mapper.writeValueAsString(entity);
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String postJacksonPortableWithAllNumberTypes(String jackson) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      NumberEntity entity = mapper.readValue(jackson, NumberEntity.class);
      return mapper.writeValueAsString(entity);
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String getWithQueryParamListOfStrings(List<String> ids) {
    if (ids.size() != 3)
      throw new IllegalArgumentException("Expected 3 id parameters");

    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(ids);
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public User postJacksonIntercepted(String jackson) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      User user = mapper.readValue(jackson, User.class);
      user.setJacksonRep(mapper.writeValueAsString(user));
      return user;
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
