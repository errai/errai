package org.jboss.errai.jpa.test.entity.inherit;

import javax.persistence.Entity;

@Entity
public class IdTestingEntity2 extends ParentAbstractEntity {

  // id is inherited

  private int childField;

  public int getChildField() {
    return childField;
  }

  public void setChildField(int childField) {
    this.childField = childField;
  }

  @Override
  public String toString() {
    return "ChildOfAbstractParentEntity [id=" + id + ", privateParentField=" + getPrivateParentField()
            + ", protectedParentField=" + protectedParentField + ", packagePrivateParentField="
            + packagePrivateParentField + ", publicParentField=" + publicParentField + ", childField=" + childField
            + "]";
  }
}
