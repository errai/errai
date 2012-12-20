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

package org.jboss.errai.validation.client;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.junit.Ignore;

/**
 * Simple bindable model for testing purposes.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Bindable
@Portable
@Ignore
public class TestModel {

  @Min(value=100)
  private int numVal;

  @NotNull
  private String stringVal;

  public int getNumVal() {
    return numVal;
  }

  public void setNumVal(int numVal) {
    this.numVal = numVal;
  }

  public String getStringVal() {
    return stringVal;
  }

  public void setStringVal(String stringVal) {
    this.stringVal = stringVal;
  }

}