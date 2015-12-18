/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.scheduler.client;

import org.jboss.errai.common.client.util.TimeUnit;
import org.jboss.errai.ioc.client.api.Timed;
import org.jboss.errai.ioc.client.api.TimerType;

import javax.enterprise.context.Dependent;

/**
 * @author Mike Brock
 */
@Dependent
public class BeanWithTimedMethod {
  private int count;
  private int delayedCount;

  @Timed(timeUnit = TimeUnit.MILLISECONDS, interval = 500, type = TimerType.REPEATING)
  public void foo() {
    System.out.println("<<" + count + ">>");
    count++;
  }

  @Timed(timeUnit = TimeUnit.MILLISECONDS, interval = 500, type = TimerType.DELAYED)
  public void bar() {
    delayedCount++;
  }
  
  public int getCount() {
    return count;
  }
  
  public int getDelayedCount() {
    return delayedCount;
  }
}
