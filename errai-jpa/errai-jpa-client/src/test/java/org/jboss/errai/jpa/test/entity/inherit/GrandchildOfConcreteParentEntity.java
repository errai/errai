package org.jboss.errai.jpa.test.entity.inherit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ConcreteGrandchild")
public class GrandchildOfConcreteParentEntity extends ChildOfConcreteParentEntity {

  // id is inherited

  private int grandchildField;

  public int getGrandchildField() {
    return grandchildField;
  }

  public void setGrandchildField(int grandchildField) {
    this.grandchildField = grandchildField;
  }

  @Override
  public String toString() {
    return "GrandchildOfConcreteParentEntity [id=" + id + ", privateParentField=" + getPrivateParentField()
            + ", protectedParentField=" + protectedParentField + ", packagePrivateParentField="
            + packagePrivateParentField + ", publicParentField=" + publicParentField + ", childField="
            + getChildField() + ", grandchildField=" + grandchildField + "]";
  }
}
