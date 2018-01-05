package org.jboss.errai.databinding.client;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.junit.Ignore;

/**
 * Simple bindable model for testing purposes.
 * 
 * @author Sašo Petrovič <saso.petrovic@gmail.com>
 */
@Bindable
@Portable
@Ignore
public class TestModelBindableProxyMethods {
	
	private String prop;
	
	public void set(String property, Object value) {
		//Method used by bindable proxy
	}
	
	public Object get(String property) {
		//Method used by bindable proxy
		return null;
	}

	public String getProp() {
		return prop;
	}

	public void setProp(String prop) {
		this.prop = prop;
	}
	
}
