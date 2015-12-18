/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.demo.grocery.client.shared;

import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.demo.grocery.client.local.producer.StoreListProducer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Bindable
@Entity
@EntityListeners(StoreListProducer.StoreListener.class)
@NamedQuery(name = "allStores", query = "SELECT s FROM Store s ORDER BY s.name")
public class Store {

    @Id
    @GeneratedValue
    private long id;

    private String name;
    private String address;

    private double latitude;
    private double longitude;

    private Integer radius = 0;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<Department> departments = new ArrayList<Department>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Store{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", address='" + address + '\'' +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", radius=" + radius +
            ", departments=" + departments +
            '}';
    }
}
