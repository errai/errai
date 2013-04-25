package org.jboss.errai.cdi.demo.mvp.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class ContactDetails {
	private String id;
	private String displayName;

	public ContactDetails() {
		new ContactDetails("0", "");
	}

	public ContactDetails(String id, String displayName) {
		this.id = id;
		this.displayName = displayName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
