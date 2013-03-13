/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.databinding.rebind;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.ui.shared.api.annotations.Bound;

/**
 * Represents a bound data field and holds all relevant metadata.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BoundDataField {

  private final Bound bound;
  private final Statement widgetStatement;
  private final String name;

  /**
   * Creates a new bound data field.
   * 
   * @param bound
   *          a reference to the {@link Bound} annotation on the data field. Must not be null.
   * @param widgetStatement
   *          the {@link Statement} to access the widget to bind. Must not be null.
   * @param name
   *          the name of the {@link DataField}. Must not be null.
   */
  public BoundDataField(Bound bound, Statement widgetStatement, String name) {
    this.bound = Assert.notNull(bound);
    this.widgetStatement = Assert.notNull(widgetStatement);
    this.name = Assert.notNull(name);
  }

  public Bound getBound() {
    return bound;
  }

  public Statement getWidgetStatement() {
    return widgetStatement;
  }
  
  public String getName() {
    return name;
  }
  
}