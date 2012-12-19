package org.jboss.errai.example.client.shared;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * This is the main business entity of the Kitchen Sink demo. It has lots of
 * annotations because it's the subject of four separate annotation-driven
 * frameworks:
 * <dl>
 *   <dt>JAXB - The Java API for XML Binding
 *   <dd>{@code @XmlRootElement} indicates that this class can be serialized as XML.
 *       This allows automatic serialization in the JAX-RS resource.
 *
 *   <dt>JPA - The Java Persistence API
 *   <dd>{@code @Entity} indicates that this class should be persisted to the
 *       server-side relational database.
 *
 *   <dt>Errai Marshalling
 *   <dd>{@code @Portable} indicates that instances of this class can be sent to clients.
 *
 *   <dt>Bean Validation
 *   <dd>{@code @NotNull, @NotEmpty, @Email, @Size} specify validation constraints on various properties of this bean.
 *       Both JPA and the GWT client understand these rules, and test instances of this class against them.
 * </dl>
 */
@Portable
@XmlRootElement
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Member implements Serializable, Comparable<Member> {
  /** Default value included to remove warning. Remove or modify at will. **/
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private Long id;

  @NotNull
  @Size(min = 1, max = 25)
  @Pattern(regexp = "[A-Za-z ]*", message = "must contain only letters and spaces")
  private String name;

  @NotNull
  @NotEmpty
  @Email
  private String email;

  @NotNull
  @Size(min = 10, max = 12)
  @Digits(fraction = 0, integer = 12)
  @Column(name = "phone_number")
  private String phoneNumber;

  @Lob
  private String picture;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }

  /**
   * Compares this member to the other member case-insensitive-alphabetically by name.
   */
  @Override
  public int compareTo(Member o) {
    if (o == null) {
      return 1;
    }
    if (this.name == null && o.name != null) {
      return -1;
    }
    if (this.name == null && o.name == null) {
      return 0;
    }
    if (this.name != null && o.name == null) {
      return 1;
    }
    return this.name.compareToIgnoreCase(o.name);
  }
}