/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.jpa.test.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceContextType;

import org.jboss.errai.ioc.client.api.TestOnly;

/**
 * This is the Zen Entity: one with everything. We use it to ensure all the JPA
 * Basic types can be marshalled and demarshalled.
 * <p>
 * Actually, it's <i>almost</i> everything: java.util.Calendar is not available in GWT.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@SuppressWarnings("JpaQlInspection")
@TestOnly @Entity
@NamedQueries({

  // LITERAL VALUES
  // --------------
  @NamedQuery(name="zentityPrimitiveBoolean", query="SELECT z FROM Zentity z WHERE z.primitiveBool = :b"),
  @NamedQuery(name="zentityString", query="SELECT z FROM Zentity z WHERE z.string = :s"),
  @NamedQuery(name="zentityLiteralString", query="SELECT z FROM Zentity z WHERE z.string = 'D''oh!'"),

  // non-standard date literal syntax (Hibernate doesn't support JPA2 date literals)
  @NamedQuery(name="zentityLiteralDate", query="SELECT z FROM Zentity z WHERE z.date = '2012-06-22'"),

  @NamedQuery(name="zentityLiteralLong", query="SELECT z FROM Zentity z WHERE z.primitiveLong = 11223344L"),
  @NamedQuery(name="zentityLiteralInt", query="SELECT z FROM Zentity z WHERE z.primitiveInt = -55443322"),
  @NamedQuery(name="zentityLiteralShort", query="SELECT z FROM Zentity z WHERE z.primitiveShort = -1234"),

  // note that char-valued attributes will only compare equal to string literals, not numeric literals.
  // this restriction also exists with Hibernate-on-HSQLDB (so we're consistent with server side behaviour).
  @NamedQuery(name="zentityLiteralChar", query="SELECT z FROM Zentity z WHERE z.primitiveChar = 'c'"),

  @NamedQuery(name="zentityLiteralByte", query="SELECT z FROM Zentity z WHERE z.primitiveByte = -5"),
  @NamedQuery(name="zentityLiteralDouble", query="SELECT z FROM Zentity z WHERE z.primitiveDouble = 123.45"),
  @NamedQuery(name="zentityLiteralFloat", query="SELECT z FROM Zentity z WHERE z.primitiveFloat = -1234.5f"),

  // the attribute is a double, but we are comparing it to an integer literal. I believe this will be common.
  @NamedQuery(name="zentityLiteralDoubleToInt", query="SELECT z FROM Zentity z WHERE z.primitiveDouble = 12345"),

  @NamedQuery(name="zentityLiteralBoolTrue", query="SELECT z FROM Zentity z WHERE z.primitiveBool = true"),
  @NamedQuery(name="zentityLiteralBoolFalse", query="SELECT z FROM Zentity z WHERE z.primitiveBool = false"),

  @NamedQuery(name="zentityLiteralNull", query="SELECT z FROM Zentity z WHERE z.string = null"),
  @NamedQuery(name="zentityLiteralNotNull", query="SELECT z FROM Zentity z WHERE z.string != null"),

  // this is primarily a test for enum literals
  // but it's also an oblique test for sticking a query for type A on an entity of type B
  @NamedQuery(name="albumLiteralEnum",
              query="SELECT a FROM Album a WHERE a.format = org.jboss.errai.jpa.test.entity.Format.SINGLE"),



  // BOOLEAN EXPRESSIONS
  // -------------------

  @NamedQuery(name="zentityAnd",
              query="SELECT z FROM Zentity z WHERE z.string = 'hello' AND z.primitiveInt = 555"),
  @NamedQuery(name="zentityOr",
              query="SELECT z FROM Zentity z WHERE z.string = 'hello' OR z.primitiveInt = 555"),
  @NamedQuery(name="zentityNot",
              query="SELECT z FROM Zentity z WHERE NOT z.string = 'hello'"),
  @NamedQuery(name="zentityNestedBooleanLogic",
              query="SELECT z FROM Zentity z WHERE z.string = 'hello' AND z.primitiveInt = 555 OR z.primitiveByte = 1"),



  // COMPARISON OPERATORS
  // --------------------

  // Thoroughness dictates that we should test all combinations of numeric types (int > double, byte > double, ...)
  // but the equals tests already do that. We'll just assume that coercing everything to double works for inequalities too.
  @NamedQuery(name="zentityGreaterThan",          query="SELECT z FROM Zentity z WHERE z.primitiveInt > 555"),
  @NamedQuery(name="zentityGreaterThanOrEqualTo", query="SELECT z FROM Zentity z WHERE z.primitiveInt >= 555"),
  @NamedQuery(name="zentityLessThan",             query="SELECT z FROM Zentity z WHERE z.primitiveInt < 555"),
  @NamedQuery(name="zentityLessThanOrEqualTo",    query="SELECT z FROM Zentity z WHERE z.primitiveInt <= 555"),

  // These should ensure inequalities work for Java Comparable<?> types
  @NamedQuery(name="zentityStringGreaterThan", query="SELECT z FROM Zentity z WHERE z.string > 'hello'"),
  @NamedQuery(name="zentityStringGreaterThanOrEqualTo", query="SELECT z FROM Zentity z WHERE z.string >= 'hello'"),
  @NamedQuery(name="zentityStringLessThan", query="SELECT z FROM Zentity z WHERE z.string < 'hello'"),
  @NamedQuery(name="zentityStringLessThanOrEqualTo", query="SELECT z FROM Zentity z WHERE z.string <= 'hello'"),

  @NamedQuery(name="zentityBetween", query="SELECT z FROM Zentity z WHERE z.boxedDouble BETWEEN 2.0 AND 4.0"),
  @NamedQuery(name="zentityNotBetween", query="SELECT z FROM Zentity z WHERE z.boxedDouble NOT BETWEEN 2.0 AND 4.0"),

  @NamedQuery(name="zentityInLiteral", query="SELECT z FROM Zentity z WHERE z.string IN ('foo', 'bar', 'baz')"),
  @NamedQuery(name="zentityInSingleValuedParams", query="SELECT z FROM Zentity z WHERE z.string IN (:in1, :in2, :in3)"),
  @NamedQuery(name="zentityInCollectionParam", query="SELECT z FROM Zentity z WHERE z.string IN :inList"),
  @NamedQuery(name="zentityNotInLiteral", query="SELECT z FROM Zentity z WHERE z.string NOT IN ('foo', 'bar', 'baz')"),

  @NamedQuery(name="zentityLike", query="SELECT z FROM Zentity z WHERE z.string LIKE :str"),
  @NamedQuery(name="zentityNotLike", query="SELECT z FROM Zentity z WHERE z.string NOT LIKE :str"),
  @NamedQuery(name="zentityLikeWithEscapeChar", query="SELECT z FROM Zentity z WHERE z.string LIKE :str ESCAPE 'a' AND 1 = 1"),



  // ORDER BY
  // --------

  @NamedQuery(name="zentityOrderByPrimitiveInt", query="SELECT z FROM Zentity z ORDER BY z.primitiveInt"),
  @NamedQuery(name="zentityOrderByPrimitiveIntDesc", query="SELECT z FROM Zentity z ORDER BY z.primitiveInt DESC"),

  @NamedQuery(name="zentityOrderByBoxedFloat", query="SELECT z FROM Zentity z ORDER BY z.boxedFloat"),

  // because of the way the AST works, all three of these tests are needed to protect the code generator from regressions
  @NamedQuery(name="zentityOrderByStringDescThenInt", query="SELECT z FROM Zentity z ORDER BY z.string DESC, z.primitiveInt"),
  @NamedQuery(name="zentityOrderByStringAscThenInt", query="SELECT z FROM Zentity z ORDER BY z.string ASC, z.primitiveInt"),
  @NamedQuery(name="zentityOrderByStringThenInt", query="SELECT z FROM Zentity z ORDER BY z.string, z.primitiveInt"),



  // STRING FUNCTIONS
  // ----------------

  @NamedQuery(name="zentityLowercaseFunction", query="SELECT z FROM Zentity z WHERE 'foo' = lower(z.string)"),
  @NamedQuery(name="zentityUppercaseFunction", query="SELECT z FROM Zentity z WHERE upper(z.string) = 'FOO'"),

  // regression test: hibernate's parser does not guess at the expected type of :str in this case (was causing NPE)
  @NamedQuery(name="zentityParamNestedInFunction", query="SELECT z FROM Zentity z WHERE lower(:str) = z.string"),

  @NamedQuery(name="zentityConcatFunction", query="SELECT z FROM Zentity z WHERE 'foo' = concat(z.string, 'o', 'o')"),
  @NamedQuery(name="zentitySubstringFunctionOneArg", query="SELECT z FROM Zentity z WHERE z.string = substring(:bigStr, :startPos)"),
  @NamedQuery(name="zentitySubstringFunctionTwoArgs", query="SELECT z FROM Zentity z WHERE z.string = substring(:bigStr, :startPos, :length)"),
  @NamedQuery(name="zentityTrimFunction", query="SELECT z FROM Zentity z WHERE trim(z.string) = 'foo'"),
  @NamedQuery(name="zentityTrimLeadingFunction", query="SELECT z FROM Zentity z WHERE trim(LEADING FROM z.string) = 'foo'"),
  @NamedQuery(name="zentityTrimTrailingFunction", query="SELECT z FROM Zentity z WHERE trim(TRAILING FROM z.string) = 'foo'"),
  @NamedQuery(name="zentityTrimTrailingWithCustomPadFunction", query="SELECT z FROM Zentity z WHERE trim(TRAILING 'o' FROM z.string) = 'f'"),
  @NamedQuery(name="zentityLengthFunction", query="SELECT z FROM Zentity z WHERE length(z.string) = 3"),
  @NamedQuery(name="zentityLocateFunction2Args", query="SELECT z FROM Zentity z WHERE locate(:lookFor, z.string) = 2"),
  @NamedQuery(name="zentityLocateFunction3Args", query="SELECT z FROM Zentity z WHERE locate(:lookFor, z.string, 3) > 0"),



  // OTHER
  // -----

  @NamedQuery(name="zentityNoWhereClause", query="SELECT z FROM Zentity z"),
})
public class Zentity {

  @GeneratedValue @Id
  private Long id;

  private boolean primitiveBool;
  private Boolean boxedBool;

  private byte primitiveByte;
  private Byte boxedByte;
  private byte[] primitiveByteArray;
  private Byte[] boxedByteArray;

  private char primitiveChar;
  private Character boxedChar;
  private char[] primitiveCharArray;
  private Character[] boxedCharArray;

  private short primitiveShort;
  private Short boxedShort;

  private int primitiveInt;
  private Integer boxedInt;

  private long primitiveLong;
  private Long boxedLong;

  private float primitiveFloat;
  private Float boxedFloat;

  private double primitiveDouble;
  private Double boxedDouble;

  private String string;

  private BigInteger bigInteger;
  private BigDecimal bigDecimal;

  private Date date;
  private java.sql.Date sqlDate;
  private Time sqlTime;
  private Timestamp sqlTimestamp;

  private PersistenceContextType enumeration;

  /**
   * Default constructor required by JPA.
   */
  public Zentity() {

  }

  /**
   * Constructor that sets all values except the id, which is managed by JPA.
   */
  public Zentity(boolean primitiveBool, Boolean boxedBool, byte primitiveByte,
          Byte boxedByte, byte[] primitiveByteArray, Byte[] boxedByteArray,
          char primitiveChar, Character boxedChar, char[] primitiveCharArray,
          Character[] boxedCharArray, short primitiveShort, Short boxedShort,
          int primitiveInt, Integer boxedInt, long primitiveLong,
          Long boxedLong, float primitiveFloat, Float boxedFloat,
          double primitiveDouble, Double boxedDouble, String string,
          BigInteger bigInteger, BigDecimal bigDecimal, Date date,
          java.sql.Date sqlDate, Time sqlTime,
          Timestamp sqlTimestamp, PersistenceContextType enumeration) {
    this.primitiveBool = primitiveBool;
    this.boxedBool = boxedBool;
    this.primitiveByte = primitiveByte;
    this.boxedByte = boxedByte;
    this.primitiveByteArray = primitiveByteArray;
    this.boxedByteArray = boxedByteArray;
    this.primitiveChar = primitiveChar;
    this.boxedChar = boxedChar;
    this.primitiveCharArray = primitiveCharArray;
    this.boxedCharArray = boxedCharArray;
    this.primitiveShort = primitiveShort;
    this.boxedShort = boxedShort;
    this.primitiveInt = primitiveInt;
    this.boxedInt = boxedInt;
    this.primitiveLong = primitiveLong;
    this.boxedLong = boxedLong;
    this.primitiveFloat = primitiveFloat;
    this.boxedFloat = boxedFloat;
    this.primitiveDouble = primitiveDouble;
    this.boxedDouble = boxedDouble;
    this.string = string;
    this.bigInteger = bigInteger;
    this.bigDecimal = bigDecimal;
    this.date = date;
    this.sqlDate = sqlDate;
    this.sqlTime = sqlTime;
    this.sqlTimestamp = sqlTimestamp;
    this.enumeration = enumeration;
  }

  public Long getId() {
    return id;
  }

  public boolean getPrimitiveBool() {
    return primitiveBool;
  }

  public void setPrimitiveBool(boolean primitiveBool) {
    this.primitiveBool = primitiveBool;
  }

  public Boolean getBoxedBool() {
    return boxedBool;
  }

  public void setBoxedBool(Boolean boxedBool) {
    this.boxedBool = boxedBool;
  }

  public byte getPrimitiveByte() {
    return primitiveByte;
  }

  public void setPrimitiveByte(byte primitiveByte) {
    this.primitiveByte = primitiveByte;
  }

  public Byte getBoxedByte() {
    return boxedByte;
  }

  public void setBoxedByte(Byte boxedByte) {
    this.boxedByte = boxedByte;
  }

  public byte[] getPrimitiveByteArray() {
    return primitiveByteArray;
  }

  public void setPrimitiveByteArray(byte[] primitiveByteArray) {
    this.primitiveByteArray = primitiveByteArray;
  }

  public Byte[] getBoxedByteArray() {
    return boxedByteArray;
  }

  public void setBoxedByteArray(Byte[] boxedByteArray) {
    this.boxedByteArray = boxedByteArray;
  }

  public char getPrimitiveChar() {
    return primitiveChar;
  }

  public void setPrimitiveChar(char primitiveChar) {
    this.primitiveChar = primitiveChar;
  }

  public Character getBoxedChar() {
    return boxedChar;
  }

  public void setBoxedChar(Character boxedChar) {
    this.boxedChar = boxedChar;
  }

  public char[] getPrimitiveCharArray() {
    return primitiveCharArray;
  }

  public void setPrimitiveCharArray(char[] primitiveCharArray) {
    this.primitiveCharArray = primitiveCharArray;
  }

  public Character[] getBoxedCharArray() {
    return boxedCharArray;
  }

  public void setBoxedCharArray(Character[] boxedCharArray) {
    this.boxedCharArray = boxedCharArray;
  }

  public short getPrimitiveShort() {
    return primitiveShort;
  }

  public void setPrimitiveShort(short primitiveShort) {
    this.primitiveShort = primitiveShort;
  }

  public Short getBoxedShort() {
    return boxedShort;
  }

  public void setBoxedShort(Short boxedShort) {
    this.boxedShort = boxedShort;
  }

  public int getPrimitiveInt() {
    return primitiveInt;
  }

  public void setPrimitiveInt(int primitiveInt) {
    this.primitiveInt = primitiveInt;
  }

  public Integer getBoxedInt() {
    return boxedInt;
  }

  public void setBoxedInt(Integer boxedInt) {
    this.boxedInt = boxedInt;
  }

  public long getPrimitiveLong() {
    return primitiveLong;
  }

  public void setPrimitiveLong(long primitiveLong) {
    this.primitiveLong = primitiveLong;
  }

  public Long getBoxedLong() {
    return boxedLong;
  }

  public void setBoxedLong(Long boxedLong) {
    this.boxedLong = boxedLong;
  }

  public float getPrimitiveFloat() {
    return primitiveFloat;
  }

  public void setPrimitiveFloat(float primitiveFloat) {
    this.primitiveFloat = primitiveFloat;
  }

  public Float getBoxedFloat() {
    return boxedFloat;
  }

  public void setBoxedFloat(Float boxedFloat) {
    this.boxedFloat = boxedFloat;
  }

  public double getPrimitiveDouble() {
    return primitiveDouble;
  }

  public void setPrimitiveDouble(double primitiveDouble) {
    this.primitiveDouble = primitiveDouble;
  }

  public Double getBoxedDouble() {
    return boxedDouble;
  }

  public void setBoxedDouble(Double boxedDouble) {
    this.boxedDouble = boxedDouble;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public BigInteger getBigInteger() {
    return bigInteger;
  }

  public void setBigInteger(BigInteger bigInteger) {
    this.bigInteger = bigInteger;
  }

  public BigDecimal getBigDecimal() {
    return bigDecimal;
  }

  public void setBigDecimal(BigDecimal bigDecimal) {
    this.bigDecimal = bigDecimal;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public java.sql.Date getSqlDate() {
    return sqlDate;
  }

  public void setSqlDate(java.sql.Date sqlDate) {
    this.sqlDate = sqlDate;
  }

  public Time getSqlTime() {
    return sqlTime;
  }

  public void setSqlTime(Time sqlTime) {
    this.sqlTime = sqlTime;
  }

  public Timestamp getSqlTimestamp() {
    return sqlTimestamp;
  }

  public void setSqlTimestamp(Timestamp sqlTimestamp) {
    this.sqlTimestamp = sqlTimestamp;
  }

  public PersistenceContextType getEnumeration() {
    return enumeration;
  }

  public void setEnumeration(PersistenceContextType enumeration) {
    this.enumeration = enumeration;
  }

  @Override
  public String toString() {
    return "Zentity [id=" + id + ", primitiveBool=" + primitiveBool
            + ", boxedBool=" + boxedBool + ", primitiveByte=" + primitiveByte
            + ", boxedByte=" + boxedByte + ", primitiveByteArray="
            + Arrays.toString(primitiveByteArray) + ", boxedByteArray="
            + Arrays.toString(boxedByteArray) + ", primitiveChar="
            + primitiveChar + ", boxedChar=" + boxedChar
            + ", primitiveCharArray=" + Arrays.toString(primitiveCharArray)
            + ", boxedCharArray=" + Arrays.toString(boxedCharArray)
            + ", primitiveShort=" + primitiveShort + ", boxedShort="
            + boxedShort + ", primitiveInt=" + primitiveInt + ", boxedInt="
            + boxedInt + ", primitiveLong=" + primitiveLong + ", boxedLong="
            + boxedLong + ", primitiveFloat=" + primitiveFloat
            + ", boxedFloat=" + boxedFloat + ", primitiveDouble="
            + primitiveDouble + ", boxedDouble=" + boxedDouble + ", string="
            + string + ", bigInteger=" + bigInteger + ", bigDecimal="
            + bigDecimal + ", date=" + date + ", sqlDate=" + sqlDate + ", sqlTime=" + sqlTime
            + ", sqlTimestamp=" + sqlTimestamp + ", enumeration=" + enumeration
            + "]";
  }

}
