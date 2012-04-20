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

package org.jboss.errai.databinding.client;

import javax.enterprise.context.Dependent;

import org.jboss.errai.databinding.client.api.Bindable;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Bindable
@Dependent
public class Model {

  private String value;
  private String _name;
  private Integer _age;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    this._name = name;
  }

  public Integer getAge() {
    return _age;
  }

  public void setAge(Integer age) {
    this._age = age;
  }
}