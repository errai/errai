/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.demo.grocery.client.local;

import javax.enterprise.inject.Alternative;

import org.jboss.errai.demo.grocery.client.shared.Department;
import org.jboss.errai.ui.client.widget.HasModel;

import com.google.gwt.user.client.ui.Label;

@Alternative
public class DepartmentWidget extends Label implements HasModel<Department> {

    private Department department;

    public DepartmentWidget() {
        setModel(null);
    }

    @Override
    public Department getModel() {
        return department;
    }

    @Override
    public void setModel(Department model) {
        department = model;
        String text;
        if (department == null) {
            text = "UNINITIALIZED DEPARTMENT WIDGET";
        }
        else if (department.getName() == null || department.getName().trim().length() == 0) {
            text = "Unnamed Department";
        }
        else {
            text = department.getName();
        }
        setText(text);
    }
}
