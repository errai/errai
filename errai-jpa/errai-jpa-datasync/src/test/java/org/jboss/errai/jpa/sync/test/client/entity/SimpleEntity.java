package org.jboss.errai.jpa.sync.test.client.entity;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class SimpleEntity implements Cloneable {

  @Id @GeneratedValue
  private Long id;

  private String string;
  private Integer integer;

  private Timestamp date;

  public Long getId() {
    return id;
  }
  public String getString() {
    return string;
  }
  public void setString(String string) {
    this.string = string;
  }
  public Integer getInteger() {
    return integer;
  }
  public void setInteger(Integer integer) {
    this.integer = integer;
  }
  public Timestamp getDate() {
    return date;
  }
  public void setDate(Timestamp date) {
    this.date = date;
  }

  /**
   * Sets the ID on the given SimpleEntity.
   * <p>
   * This static method is used instead of a setId() method because I want to be
   * sure the datasync system still works when there's no actual ID setter method.
   *
   * @param instance the SimpleEntity instance whose ID to set
   * @param id the new ID value
   */
  public static void setId(SimpleEntity instance, Long id) {
    instance.id = id;
  }

  @Override
  public SimpleEntity clone() {
    try {
      return (SimpleEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

  @Override
  public String toString() {
    // Warning: tests rely on this toString() fully representing the state of the object
    DateFormat df = SimpleDateFormat.getDateTimeInstance();
    return "SimpleEntity [id=" + id + ", string=" + string + ", integer=" + integer + ", date=" + df.format(date) + "]";
  }

}
