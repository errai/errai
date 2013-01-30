package org.jboss.errai.demo.grocery.client.local;

import javax.enterprise.context.Dependent;

import org.jboss.errai.demo.grocery.client.shared.Department;
import org.jboss.errai.ioc.client.api.LoadAsync;
import org.jboss.errai.ui.client.widget.HasModel;

import com.google.gwt.user.client.ui.Label;

@LoadAsync
@Dependent
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
