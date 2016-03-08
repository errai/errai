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

package org.jboss.errai.cdi.event.client.test;

import javax.inject.Named;

import org.jboss.errai.cdi.client.qualifier.Value;
import org.jboss.errai.cdi.client.qualifier.WithEnum;
import org.jboss.errai.cdi.client.qualifier.WithInt;
import org.jboss.errai.cdi.client.qualifier.WithMultiple;
import org.jboss.errai.cdi.event.client.FiredQualifierObserver;
import org.jboss.errai.cdi.event.client.shared.FiredQualifier;
import org.jboss.errai.cdi.event.client.shared.QualifiedMemberEventProducer;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

import com.google.gwt.user.client.Timer;

/**
 * Test sending and receiving events with qualifier members like {@code @Named("literal value")}.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class AbstractQualifierMemberIntegrationTest extends AbstractErraiCDITest {

  private static final int DURATION = 30000;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.EventObserverTestModule";
  }

  protected abstract FiredQualifierObserver getQualifierObserver();

  protected abstract QualifiedMemberEventProducer getEventProducer();

  protected abstract void setup(Runnable run);

  public void testEnumMember() throws Exception {
    delayTestFinish(DURATION);
    CDI.addPostInitTask(() -> {
      final QualifiedMemberEventProducer producer = getEventProducer();
      final FiredQualifierObserver observer = getQualifierObserver();

      assertEquals(0, observer.observedQualifiers.size());

      assertUntil(DURATION - 1000,
                  () -> producer.fireEnumOne(),
                  () -> {
                    assertEquals(1, observer.observedQualifiers.size());
                    assertEquals(WithEnum.class.getName(), observer.observedQualifiers.get(0).getAnnoType());
                    assertEquals(Value.ONE, observer.observedQualifiers.get(0).getValues().get("value"));
                  },

                  () -> {
                    observer.observedQualifiers.clear();
                    producer.fireEnumTwo();
                  },
                  () -> {
                    assertEquals(1, observer.observedQualifiers.size());
                    assertEquals(WithEnum.class.getName(), observer.observedQualifiers.get(0).getAnnoType());
                    assertEquals(Value.TWO, observer.observedQualifiers.get(0).getValues().get("value"));
                  },

                  () -> {
                    observer.observedQualifiers.clear();
                    producer.fireEnumThree();
                  },
                  () -> assertEquals(0, observer.observedQualifiers.size()));
    });
  }

  public void testIntMember() throws Exception {
    delayTestFinish(DURATION);
    CDI.addPostInitTask(() -> {
      final QualifiedMemberEventProducer producer = getEventProducer();
      final FiredQualifierObserver observer = getQualifierObserver();

      assertEquals(0, observer.observedQualifiers.size());

      assertUntil(DURATION - 1000,
                  () -> producer.fireInt0(),
                  () -> {
                    assertEquals(1, observer.observedQualifiers.size());
                    assertEquals(WithInt.class.getName(), observer.observedQualifiers.get(0).getAnnoType());
                    assertEquals(0, observer.observedQualifiers.get(0).getValues().get("value"));
                  },

                  () -> {
                    observer.observedQualifiers.clear();
                    producer.fireInt100();
                  },
                  () -> {
                    assertEquals(1, observer.observedQualifiers.size());
                    assertEquals(WithInt.class.getName(), observer.observedQualifiers.get(0).getAnnoType());
                    assertEquals(100, observer.observedQualifiers.get(0).getValues().get("value"));
                  },

                  () -> {
                    observer.observedQualifiers.clear();
                    producer.fireIntNeg1();
                  },
                  () -> assertEquals(0, observer.observedQualifiers.size()));
    });
  }

  public void testNamedEvent() throws Exception {
    delayTestFinish(DURATION);
    CDI.addPostInitTask(() -> {
      final QualifiedMemberEventProducer producer = getEventProducer();
      final FiredQualifierObserver observer = getQualifierObserver();

      assertEquals(0, observer.observedQualifiers.size());

      assertUntil(DURATION - 1000,
              () -> producer.fireNamedEvent(),
              () -> {
                assertEquals(1, observer.observedQualifiers.size());
                assertEquals(Named.class.getName(), observer.observedQualifiers.get(0).getAnnoType());
                assertEquals("foo", observer.observedQualifiers.get(0).getValues().get("value"));
              });
      });
  }

  public void testMultipleQualifiers() throws Exception {
    delayTestFinish(DURATION);
    CDI.addPostInitTask(() -> {
      final QualifiedMemberEventProducer producer = getEventProducer();
      final FiredQualifierObserver observer = getQualifierObserver();

      assertEquals(0, observer.observedQualifiers.size());

      assertUntil(DURATION - 1000,
                  () -> producer.fireEnumAndIntOne(),
                  () -> {
                    assertEquals(2, observer.observedQualifiers.size());
                    assertTrue(observer.observedQualifiers.toString(),
                               observer.observedQualifiers.get(0).getAnnoType().equals(WithEnum.class.getName())
                               || observer.observedQualifiers.get(1).getAnnoType().equals(WithEnum.class.getName()));
                    assertTrue(observer.observedQualifiers.toString(),
                               observer.observedQualifiers.get(0).getAnnoType().equals(WithInt.class.getName())
                               || observer.observedQualifiers.get(1).getAnnoType().equals(WithInt.class.getName()));

                    final FiredQualifier enumAnno = (observer.observedQualifiers.get(0).getAnnoType().equals(WithEnum.class.getName())
                            ? observer.observedQualifiers.get(0) : observer.observedQualifiers.get(1));
                    final FiredQualifier intAnno = (observer.observedQualifiers.get(0).getAnnoType().equals(WithInt.class.getName())
                            ? observer.observedQualifiers.get(0) : observer.observedQualifiers.get(1));

                    assertEquals(Value.ONE, enumAnno.getValues().get("value"));
                    assertEquals(0, intAnno.getValues().get("value"));
                  });
    });
  }

  public void testQualifierWithMultipleMembers() throws Exception {
    delayTestFinish(DURATION);
    CDI.addPostInitTask(() -> {
      final QualifiedMemberEventProducer producer = getEventProducer();
      final FiredQualifierObserver observer = getQualifierObserver();

      assertEquals(0, observer.observedQualifiers.size());

      assertUntil(DURATION - 1000,
                  () -> producer.fireMultiple1(),
                  () -> {
                    assertEquals(1, observer.observedQualifiers.size());
                    assertEquals(WithMultiple.class.getName(), observer.observedQualifiers.get(0).getAnnoType());
                    assertEquals(Value.ONE, observer.observedQualifiers.get(0).getValues().get("enumValue"));
                    assertEquals(0, observer.observedQualifiers.get(0).getValues().get("intValue"));
                    assertEquals("", observer.observedQualifiers.get(0).getValues().get("strValue"));
                  },

                  () -> {
                    observer.observedQualifiers.clear();
                    producer.fireMultiple2();
                  },
                  () -> {
                    assertEquals(1, observer.observedQualifiers.size());
                    assertEquals(WithMultiple.class.getName(), observer.observedQualifiers.get(0).getAnnoType());
                    assertEquals(Value.ONE, observer.observedQualifiers.get(0).getValues().get("enumValue"));
                    assertEquals(1, observer.observedQualifiers.get(0).getValues().get("intValue"));
                    assertEquals("", observer.observedQualifiers.get(0).getValues().get("strValue"));
                  },
                  () -> {
                    observer.observedQualifiers.clear();
                    producer.fireMultiple3();
                  },
                  () -> {
                    assertEquals(1, observer.observedQualifiers.size());
                    assertEquals(WithMultiple.class.getName(), observer.observedQualifiers.get(0).getAnnoType());
                    assertEquals(Value.ONE, observer.observedQualifiers.get(0).getValues().get("enumValue"));
                    assertEquals(0, observer.observedQualifiers.get(0).getValues().get("intValue"));
                    assertEquals("foo", observer.observedQualifiers.get(0).getValues().get("strValue"));
                  },

                  () -> {
                    observer.observedQualifiers.clear();
                    producer.fireMultipleNone();
                  },
                  () -> assertEquals(0, observer.observedQualifiers.size()));

    });
  }

  /**
   * @param runnables A list of runnables alternating between assertions and setup.
   */
  private void assertUntil(final long duration, final Runnable... runnables) {
    final long start = System.currentTimeMillis();

    setup(() -> {
      new Timer() {
        int i = 0;

        @Override
        public void run() {
          if (i < runnables.length) {
            final Timer main = this;
            final Runnable setup = runnables[i];
            final Runnable assertions = runnables[i + 1];
            try {
              setup.run();
            } catch (Throwable t) {
              throw new AssertionError("Failure running setup.", t);
            }

            new Timer() {
              @Override
              public void run() {
                boolean passed = false;
                try {
                  assertions.run();
                  passed = true;
                  i += 2;
                } catch (AssertionError ae) {
                  if (System.currentTimeMillis() - start > duration) {
                    throw new AssertionError(duration + "ms ellapsed without assertions passing." + ae.getMessage(), ae);
                  }
                  else {
                    schedule(1000);
                  }
                }
                if (passed) {
                  main.run();
                }
              }
            }.run();
            ;
          }
          else {
            finishTest();
          }
        }
      }.run();
    });
  }

}
