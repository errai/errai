/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.databinding.client;

import java.util.Collection;
import java.util.Map;

import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.client.api.IgnoreBinding;

/**
 * Object besed on GXT ModelData intrface
 * 
 * @see https://docs.sencha.com/gxt/3.x/javadoc/com/sencha/gxt/legacy/client/data/ModelData.html
 *
 * @author Sašo Petrovič <saso.petrovic@gmail.com>
 * 
 */

@Bindable
public class TestModelWithIgnoreAnnotation {
	
	private Integer id;
	private String name;
	private String age;
	
	@IgnoreBinding
	public <X> X get(String property) {
		return null;
	}

	public Map<String, Object> getProperties() {
		return null;
	}

	public <X> X remove(String property) {
		return null;
	}
	
	@IgnoreBinding
	public void set(String property, Object value) {
		return;
	}
	
	@IgnoreBinding
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	@IgnoreBinding
	public void setName(String name) {
		this.name = name;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}
	
	
}
