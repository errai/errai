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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ExposeEntity
public class SType {
    private String fieldOne;
    private String fieldTwo;
    private Date startDate;
    private Date endDate;
    private Boolean active;
    private List<SType> listOfStypes;
    private Place place;

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

        if (active != null ? !active.equals(sType.active) : sType.active != null) return false;
        if (endDate != null ? !endDate.equals(sType.endDate) : sType.endDate != null) return false;
        if (fieldOne != null ? !fieldOne.equals(sType.fieldOne) : sType.fieldOne != null) return false;
        if (fieldTwo != null ? !fieldTwo.equals(sType.fieldTwo) : sType.fieldTwo != null) return false;
        if (listOfStypes != null ? !listOfStypes.equals(sType.listOfStypes) : sType.listOfStypes != null) return false;
        if (place != sType.place) return false;
        if (startDate != null ? !startDate.equals(sType.startDate) : sType.startDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fieldOne != null ? fieldOne.hashCode() : 0;
        result = 31 * result + (fieldTwo != null ? fieldTwo.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        result = 31 * result + (listOfStypes != null ? listOfStypes.hashCode() : 0);
        result = 31 * result + (place != null ? place.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return fieldOne + "|" + fieldTwo + "|" + startDate + "|" + endDate + "|" + active + "|" + place + "|" + listOfStypes;
    }

    public static SType create() {
        final SType sType1 = new SType();
        sType1.setActive(true);
        sType1.setEndDate(new Date(System.currentTimeMillis()));
        sType1.setStartDate(new Date(System.currentTimeMillis() - 10000));
        sType1.setFieldOne("One!");
        sType1.setFieldTwo("Two!!");
        sType1.setPlace(Place.FIRST);

        List<SType> listOfStypes = new ArrayList<SType>();

        final SType sType2 = new SType();
        sType2.setActive(true);
        sType2.setEndDate(new Date(System.currentTimeMillis() + 1393));
        sType2.setStartDate(new Date(System.currentTimeMillis() - 3443));
        sType2.setFieldOne("Hrmm");
        sType2.setFieldTwo("Haaa");
        sType2.setPlace(Place.SECOND);

        listOfStypes.add(sType2);

        final SType sType3 = new SType();
        sType3.setActive(false);
        sType3.setEndDate(new Date(System.currentTimeMillis() + 555));
        sType3.setStartDate(new Date(System.currentTimeMillis() - 232));
        sType3.setFieldOne("Eeek");
        sType3.setFieldTwo("Oooh");
        sType3.setPlace(Place.THIRD);

        listOfStypes.add(sType3);

        sType1.setListOfStypes(listOfStypes);

        return sType1;
    }
}
