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
	
	Object methodToBeIgnored;
	
	@IgnoreBinding
	public <X> X get(String property) {
		return null;
	}

	public Map<String, Object> getProperties() {
		return null;
	}

	public Collection<String> getPropertyNames() {
		return null;
	}

	public <X> X remove(String property) {
		return null;
	}
	
	@IgnoreBinding
	public void set(String property, Object value) {
		return;
	}
}
