package org.jboss.errai.cdi.demo.mvp.client.shared;

import java.util.ArrayList;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface ContactsService {
	
  Contact addContact(Contact contact);
  Boolean deleteContact(String id); 
  ArrayList<ContactDetails> deleteContacts(ArrayList<String> ids);
  ArrayList<ContactDetails> getContactDetails();
  Contact getContact(String id);
  Contact updateContact(Contact contact);
}
