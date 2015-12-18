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

package org.jboss.errai.demo.grocery.client.shared;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.TypedQuery;

import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Department (section of a store) that an item can be found in.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Bindable
@Entity
@NamedQueries({
    @NamedQuery(name = "allDepartments", query = "SELECT d FROM Department d ORDER BY d.name"),
    @NamedQuery(name = "departmentByName", query = "SELECT d FROM Department d WHERE lower(d.name) = lower(:name) ORDER BY d.name")
})
public class Department {

    @Id
    @GeneratedValue
    private long id;

    private String name;
    
    public Department() {
      this.name = new String("");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Department other = (Department) obj;
        if (id != other.id)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Department [id=" + id + ", name=" + name + "]";
    }

    /**
     * Finds the pre-existing department that has the given name, creating a new one if no such department exists.
     *
     * @param em The entity manager to search for the department in.
     * @param name The name of the department to look for.
     * @return an existing Department from JPA storage, or a new department instance if no existing one was found. Return value
     *         is never null.
     */
    public static Department resolve(EntityManager em, String name) {
        TypedQuery<Department> deptQuery = em.createNamedQuery("departmentByName", Department.class);
        deptQuery.setParameter("name", name);
        Department resolvedDepartment;
        List<Department> resultList = deptQuery.getResultList();
        if (resultList.isEmpty()) {
            resolvedDepartment = new Department();
            resolvedDepartment.setName(name);
        }
        else {
            resolvedDepartment = resultList.get(0);
        }
        return resolvedDepartment;
    }
}
