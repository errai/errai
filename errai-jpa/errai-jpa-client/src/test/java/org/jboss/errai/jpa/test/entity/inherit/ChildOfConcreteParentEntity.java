package org.jboss.errai.jpa.test.entity.inherit;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;

@Entity
@NamedQuery(
        name="childOfParentConcreteEntity",
        query="SELECT cpce FROM ChildOfConcreteParentEntity cpce WHERE cpce.protectedParentField >= :protectedFieldAtLeast AND cpce.protectedParentField <= :protectedFieldAtMost")
public class ChildOfConcreteParentEntity extends ParentConcreteEntity {

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
    return "ChildOfConcreteParentEntity [id=" + id + ", privateParentField=" + getPrivateParentField()
            + ", protectedParentField=" + protectedParentField + ", packagePrivateParentField="
            + packagePrivateParentField + ", publicParentField=" + publicParentField + ", childField=" + childField
            + "]";
  }
}
