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

package org.jboss.errai.jpa.sync.test.client.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceContextType;

import org.jboss.errai.ioc.client.api.TestOnly;

/**
 * This is the Zen Entity: one with everything. We use it to ensure all the JPA
 * Basic types can be marshalled and demarshalled.
 * <p>
 * Actually, it's <i>almost</i> everything: java.util.Calendar is not available in GWT.
 * <p>
 *
 *
 *
 *
 * WARNING: This class was copy-and-pasted from errai-jpa-client. Are you sure this is the one you want?
 *
 *
 *
 *
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@TestOnly @Entity
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
