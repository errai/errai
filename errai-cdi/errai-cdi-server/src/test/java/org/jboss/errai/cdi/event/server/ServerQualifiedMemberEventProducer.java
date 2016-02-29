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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.cdi.client.qualifier.Value;
import org.jboss.errai.cdi.client.qualifier.WithEnum;
import org.jboss.errai.cdi.client.qualifier.WithInt;
import org.jboss.errai.cdi.client.qualifier.WithMultiple;
import org.jboss.errai.cdi.event.client.shared.PortableEvent;
import org.jboss.errai.cdi.event.client.shared.QualifiedMemberEventProducer;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@ApplicationScoped
@Service
public class ServerQualifiedMemberEventProducer implements QualifiedMemberEventProducer {

  private final Event<PortableEvent> enumOne;
  private final Event<PortableEvent> enumTwo;
  private final Event<PortableEvent> enumThree;
  private final Event<PortableEvent> int0;
  private final Event<PortableEvent> int100;
  private final Event<PortableEvent> intNeg1;
  private final Event<PortableEvent> enumAndIntOne;
  private final Event<PortableEvent> multiple1;
  private final Event<PortableEvent> multiple2;
  private final Event<PortableEvent> multiple3;
  private final Event<PortableEvent> multipleNone;
  private final Event<PortableEvent> namedEvent;

  @Inject
  public ServerQualifiedMemberEventProducer(@WithEnum(Value.ONE) Event<PortableEvent> enumOne,
                                @WithEnum(Value.TWO) Event<PortableEvent> enumTwo,
                                @WithEnum(Value.THREE) Event<PortableEvent> enumThree,
                                @WithInt(0) Event<PortableEvent> int0,
                                @WithInt(100) Event<PortableEvent> int100,
                                @WithInt(-1) Event<PortableEvent> intNeg1,
                                @WithEnum(Value.ONE) @WithInt(0) Event<PortableEvent> enumAndIntOne,
                                @Named("foo") Event<PortableEvent> namedEvent,
                                @WithMultiple(enumValue = Value.ONE, intValue = 0, strValue = "") Event<PortableEvent> multiple1,
                                @WithMultiple(enumValue = Value.ONE, intValue = 1, strValue = "") Event<PortableEvent> multiple2,
                                @WithMultiple(enumValue = Value.ONE, intValue = 0, strValue = "foo") Event<PortableEvent> multiple3,
                                @WithMultiple(enumValue = Value.TWO, intValue = 0, strValue = "") Event<PortableEvent> multipleNone) {
                                  this.enumOne = enumOne;
                                  this.enumTwo = enumTwo;
                                  this.enumThree = enumThree;
                                  this.int0 = int0;
                                  this.int100 = int100;
                                  this.intNeg1 = intNeg1;
                                  this.enumAndIntOne = enumAndIntOne;
                                  this.namedEvent = namedEvent;
                                  this.multiple1 = multiple1;
                                  this.multiple2 = multiple2;
                                  this.multiple3 = multiple3;
                                  this.multipleNone = multipleNone;
  }

  @Override
  public void fireEnumOne() {
    enumOne.fire(new PortableEvent());
  }

  @Override
  public void fireEnumTwo() {
    enumTwo.fire(new PortableEvent());
  }

  @Override
  public void fireEnumThree() {
    enumThree.fire(new PortableEvent());
  }

  @Override
  public void fireInt0() {
    int0.fire(new PortableEvent());
  }

  @Override
  public void fireInt100() {
    int100.fire(new PortableEvent());
  }

  @Override
  public void fireIntNeg1() {
    intNeg1.fire(new PortableEvent());
  }

  @Override
  public void fireEnumAndIntOne() {
    enumAndIntOne.fire(new PortableEvent());
  }

  @Override
  public void fireNamedEvent() {
    namedEvent.fire(new PortableEvent());
  }

  @Override
  public void fireMultiple1() {
    multiple1.fire(new PortableEvent());
  }

  @Override
  public void fireMultiple2() {
    multiple2.fire(new PortableEvent());
  }

  @Override
  public void fireMultiple3() {
    multiple3.fire(new PortableEvent());
  }

  @Override
  public void fireMultipleNone() {
    multipleNone.fire(new PortableEvent());
  }

}
