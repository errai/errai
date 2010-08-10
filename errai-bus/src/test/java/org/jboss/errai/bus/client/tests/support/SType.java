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

package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

import java.util.*;

@ExposeEntity
public class SType {
    private String fieldOne;
    private String fieldTwo;
    private Date startDate;
    private Date endDate;
    private Boolean active;
    private List<SType> listOfStypes;
    private Map<String, SType> mapofStypes;
    private Place place;

    private long longValue;
    private int intValue;
    private short shortValue;
    private double doubleValue;
    private float floatValue;
    private byte byteValue;
    private char charValue;

    @ExposeEntity
    public enum Place {
        FIRST, SECOND, THIRD
    }

    public String getFieldOne() {
        return fieldOne;
    }

    public void setFieldOne(String fieldOne) {
        this.fieldOne = fieldOne;
    }

    public String getFieldTwo() {
        return fieldTwo;
    }

    public void setFieldTwo(String fieldTwo) {
        this.fieldTwo = fieldTwo;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setListOfStypes(List<SType> listOfStypes) {
        this.listOfStypes = listOfStypes;
    }

    public List<SType> getListOfStypes() {
        return listOfStypes;
    }

    public Map<String, SType> getMapofStypes() {
        return mapofStypes;
    }

    public void setMapofStypes(Map<String, SType> mapofStypes) {
        this.mapofStypes = mapofStypes;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public short getShortValue() {
        return shortValue;
    }

    public void setShortValue(short shortValue) {
        this.shortValue = shortValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    public byte getByteValue() {
        return byteValue;
    }

    public void setByteValue(byte byteValue) {
        this.byteValue = byteValue;
    }

    public char getCharValue() {
        return charValue;
    }

    public void setCharValue(char charValue) {
        this.charValue = charValue;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SType sType = (SType) o;

        if (byteValue != sType.byteValue) return false;
        if (charValue != sType.charValue) return false;
        if (Double.compare(sType.doubleValue, doubleValue) != 0) return false;
        if (Float.compare(sType.floatValue, floatValue) != 0) return false;
        if (intValue != sType.intValue) return false;
        if (longValue != sType.longValue) return false;
        if (shortValue != sType.shortValue) return false;
        if (active != null ? !active.equals(sType.active) : sType.active != null) return false;
        if (endDate != null ? !endDate.equals(sType.endDate) : sType.endDate != null) return false;
        if (fieldOne != null ? !fieldOne.equals(sType.fieldOne) : sType.fieldOne != null) return false;
        if (fieldTwo != null ? !fieldTwo.equals(sType.fieldTwo) : sType.fieldTwo != null) return false;
        if (listOfStypes != null ? !listOfStypes.equals(sType.listOfStypes) : sType.listOfStypes != null) return false;
        if (mapofStypes != null ? !mapofStypes.equals(sType.mapofStypes) : sType.mapofStypes != null) return false;
        if (place != sType.place) return false;
        if (startDate != null ? !startDate.equals(sType.startDate) : sType.startDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = fieldOne != null ? fieldOne.hashCode() : 0;
        result = 31 * result + (fieldTwo != null ? fieldTwo.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        result = 31 * result + (listOfStypes != null ? listOfStypes.hashCode() : 0);
        result = 31 * result + (mapofStypes != null ? mapofStypes.hashCode() : 0);
        result = 31 * result + (place != null ? place.hashCode() : 0);
        result = 31 * result + (int) (longValue ^ (longValue >>> 32));
        result = 31 * result + intValue;
        result = 31 * result + (int) shortValue;
        temp = doubleValue != +0.0d ? new Double(doubleValue).longValue() : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (floatValue != +0.0f ? new Float(floatValue).intValue() : 0);
        result = 31 * result + (int) byteValue;
        result = 31 * result + (int) charValue;
        return result;
    }

    @Override
    public String toString() {
        return fieldOne + "|" + fieldTwo + "|" + startDate + "|" + endDate + "|" + active + "|" + place + "|" + listOfStypes;
    }

    public static SType create(RandomProvider random) {
        final SType sType1 = randomLeafCreate(random);
        sType1.setActive(true);
        sType1.setEndDate(new Date(System.currentTimeMillis()));
        sType1.setStartDate(new Date(System.currentTimeMillis() - 10000));
        sType1.setFieldOne("One!");
        sType1.setFieldTwo("Two!!");
        sType1.setPlace(Place.FIRST);

        List<SType> listOfStypes = new ArrayList<SType>();

        final SType sType2 = randomLeafCreate(random);
        sType2.setActive(true);
        sType2.setEndDate(new Date(System.currentTimeMillis() + 1393));
        sType2.setStartDate(new Date(System.currentTimeMillis() - 3443));
        sType2.setFieldOne("Hrmm");
        sType2.setFieldTwo("Haaa");
        sType2.setPlace(Place.SECOND);

        listOfStypes.add(sType2);

        final SType sType3 = randomLeafCreate(random);
        sType3.setActive(false);
        sType3.setEndDate(new Date(System.currentTimeMillis() + 555));
        sType3.setStartDate(new Date(System.currentTimeMillis() - 232));
        sType3.setFieldOne("Eeek");
        sType3.setFieldTwo("Oooh");
        sType3.setPlace(Place.THIRD);

        listOfStypes.add(sType3);

        sType1.setListOfStypes(listOfStypes);

        Map<String, SType> mapOfSTypes = new HashMap<String, SType>();

        mapOfSTypes.put(random.randString(), randomLeafCreate(random));
        mapOfSTypes.put(random.randString(), randomLeafCreate(random));
        mapOfSTypes.put(random.randString(), randomLeafCreate(random));

        sType1.setMapofStypes(mapOfSTypes);

        return sType1;
    }

    private static SType randomLeafCreate(RandomProvider random) {
        final SType sType = new SType();
        sType.setActive(random.nextBoolean());
        sType.setFieldOne(random.randString());
        sType.setFieldTwo(random.randString());
        sType.setStartDate(randDatePast(random));
        sType.setEndDate(randDateFuture(random));
        sType.setByteValue((byte) random.nextInt(100000));
        sType.setFloatValue(new Double(random.nextDouble()).floatValue() + random.nextInt(10000));
        sType.setIntValue(random.nextInt(100000));
        sType.setDoubleValue(random.nextInt(100000) + random.nextDouble());
        sType.setLongValue(random.nextInt(1000000));
        sType.setCharValue(random.nextChar());
        sType.setPlace(randPlace(random));

        return sType;
    }

  
    private static Date randDateFuture(RandomProvider random) {
        return new Date(System.currentTimeMillis() + random.nextInt(100000));
    }

    private static Date randDatePast(RandomProvider random) {
        return new Date(System.currentTimeMillis() - random.nextInt(100000));
    }

    private static Place randPlace(RandomProvider random) {
        return Place.values()[random.nextInt(100000) % Place.values().length];
    }
}
