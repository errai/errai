/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.widgets.rebind.widgetmappers.col;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.widgets.rebind.widgetmappers.ColMapper;

public class CheckBoxColMapper implements ColMapper<TextBox> {
  public String generateGetField(TypeOracle oracle, JClassType fromType) {
    return "wld.setValue(value.contains(wld.getText()));";
  }

  public String generateSetField(TypeOracle oracle, JClassType toType) {
    return "if (wld.getValue()) { value.add(wld.getText()); } else { value.remove(wld.getText()); }";
  }

  public String generateValueChange(TypeOracle oracle, JClassType type) {
    return "wld.addValueChangeHandler(new " + ValueChangeHandler.class.getName() + "<Boolean>() { "
        + "public void onValueChange(" + ValueChangeEvent.class.getName() + "<Boolean> booleanValueChangeEvent) { "
        + "@{varName}.setFieldValue(wld, widget.@{targetEntityField.getName()}.@{getGetter(targetEntityMember.getName())}());"
        + "}});";
  }
}
