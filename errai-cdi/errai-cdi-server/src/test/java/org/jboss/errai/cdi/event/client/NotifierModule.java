/*
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

package org.jboss.errai.cdi.event.client;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.event.client.shared.Create;
import org.jboss.errai.cdi.event.client.shared.Delete;
import org.jboss.errai.cdi.event.client.shared.NotifierStartEvent;
import org.jboss.errai.cdi.event.client.shared.Server;
import org.jboss.errai.cdi.event.client.shared.TestMarshallingDto;
import org.jboss.errai.cdi.event.client.shared.Update;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Dependent
public class NotifierModule {

  @Inject
  public Event<NotifierStartEvent> event;

  public Map<Class<? extends Annotation>, List<TestMarshallingDto>> observed = new HashMap<>();

  public void observeServerCreate(@Observes @Server @Create final TestMarshallingDto dto) {
    observed.computeIfAbsent(Create.class, o -> new ArrayList<>()).add(dto);
  }

  public void observeServerUpdate(@Observes @Server @Update final TestMarshallingDto dto) {
    observed.computeIfAbsent(Update.class, o -> new ArrayList<>()).add(dto);
  }

  public void observeServerDelete(@Observes @Server @Delete final TestMarshallingDto dto) {
    observed.computeIfAbsent(Delete.class, o -> new ArrayList<>()).add(dto);
  }

}
