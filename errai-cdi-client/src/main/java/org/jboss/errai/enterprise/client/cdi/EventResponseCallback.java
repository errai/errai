/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.enterprise.client.cdi;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class EventResponseCallback<T> {
  private TestConstraint<T> constraint;
  private TestHarness harness;

  protected EventResponseCallback() {
  }

  public void setConstraint(TestConstraint<T> constraint) {
    this.constraint = constraint;
  }

  public void setHarness(TestHarness harness) {
    this.harness = harness;
  }

  public void runConditionally(T obj) {
    switch (constraint.processConstraint(obj)) {
      case Run:
        run();
        break;
      case Defer:
        break;
      case Failure:
        harness.registerUnexpected(new UnexpectedEvent(null, "did not expect event: " + obj));
    }
  }

  public abstract void run();
}
