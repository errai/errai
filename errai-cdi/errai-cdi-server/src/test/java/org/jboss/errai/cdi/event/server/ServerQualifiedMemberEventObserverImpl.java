/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.event.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.cdi.common.client.qualifier.Value;
import org.jboss.errai.cdi.common.client.qualifier.WithClazz;
import org.jboss.errai.cdi.common.client.qualifier.WithEnum;
import org.jboss.errai.cdi.common.client.qualifier.WithInt;
import org.jboss.errai.cdi.common.client.qualifier.WithMultiple;
import org.jboss.errai.cdi.event.client.shared.FiredQualifier;
import org.jboss.errai.cdi.event.client.shared.PortableEvent;
import org.jboss.errai.cdi.event.client.shared.ServerQualifierMemberEventObserver;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@ApplicationScoped
@Service
public class ServerQualifiedMemberEventObserverImpl implements ServerQualifierMemberEventObserver {

  @Inject
  private Event<FiredQualifier> event;

  private boolean active = false;

  @Override
  public void setActive(final boolean active) {
    this.active = active;
  }

  public void observeWithClazz(@Observes @WithClazz(Object.class) PortableEvent value) {
    if (active)
      event.fire(new FiredQualifier(WithClazz.class.getName(), Collections.singletonMap("value", Object.class.getName())));
  }

  public void observeWithEnumOne(@Observes @WithEnum(Value.ONE) PortableEvent value) {
    if (active)
      event.fire(new FiredQualifier(WithEnum.class.getName(), Collections.singletonMap("value", Value.ONE)));
  }

  public void observeWithEnumTwo(@Observes @WithEnum(Value.TWO) PortableEvent value) {
    if (active)
      event.fire(new FiredQualifier(WithEnum.class.getName(), Collections.singletonMap("value", Value.TWO)));
  }

  public void observeWithInt0(@Observes @WithInt(0) PortableEvent value) {
    if (active)
      event.fire(new FiredQualifier(WithInt.class.getName(), Collections.singletonMap("value", 0)));
  }

  public void observeWithInt100(@Observes @WithInt(100) PortableEvent value) {
    if (active)
      event.fire(new FiredQualifier(WithInt.class.getName(), Collections.singletonMap("value", 100)));
  }

  public void observesNamed(@Observes @Named("foo") PortableEvent value) {
    if (active)
      event.fire(new FiredQualifier(Named.class.getName(), Collections.singletonMap("value", "foo")));
  }

  public void observeWithMultiple1(
          @Observes @WithMultiple(enumValue = Value.ONE, intValue = 0, strValue = "") PortableEvent value) {
    if (active) {
      final Map<String, Object> values = new HashMap<>();
      values.put("enumValue", Value.ONE);
      values.put("intValue", 0);
      values.put("strValue", "");

      event.fire(new FiredQualifier(WithMultiple.class.getName(), values));
    }
  }

  public void observeWithMultiple2(
          @Observes @WithMultiple(enumValue = Value.ONE, intValue = 1, strValue = "") PortableEvent value) {
    if (active) {
      final Map<String, Object> values = new HashMap<>();
      values.put("enumValue", Value.ONE);
      values.put("intValue", 1);
      values.put("strValue", "");

      event.fire(new FiredQualifier(WithMultiple.class.getName(), values));
    }
  }

  public void observeWithMultiple3(
          @Observes @WithMultiple(enumValue = Value.ONE, intValue = 0, strValue = "foo") PortableEvent value) {
    if (active) {
      final Map<String, Object> values = new HashMap<>();
      values.put("enumValue", Value.ONE);
      values.put("intValue", 0);
      values.put("strValue", "foo");

      event.fire(new FiredQualifier(WithMultiple.class.getName(), values));
    }
  }

}
