/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.bus.tests.errai103;

import junit.framework.TestCase;
import org.jboss.errai.bus.server.io.JSONDecoder;
import org.jboss.errai.bus.server.io.JSONEncoder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Aug 11, 2010
 */
public class Errai103Test extends TestCase
{
  public void testMarshalling() {
    SimpleEntity entity = new SimpleEntity();
    entity.setId(System.currentTimeMillis());
    entity.setLogin("username");
    entity.setPassword("password");

    entity.setNumber("docnumer-1233455");
    entity.setCreateDate(new Date());
    entity.setLastModifyDate(new Date());
    entity.setSelected(true);

    System.out.println("type  :" + entity);

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("SimpleEntity", entity);

    String json = JSONEncoder.encode(vars);

    System.out.println("---");
    System.out.println("json:" + json);
    System.out.println("----");

    Map<String, Object> result = (Map<String, Object>) JSONDecoder.decode(json);

    SimpleEntity rEntity = (SimpleEntity) result.get("SimpleEntity");


    System.out.println("rEntity:" + rEntity);

    assertEquals("Unmarshalling failed. Instances are not equal.",entity, rEntity);
  }
}
