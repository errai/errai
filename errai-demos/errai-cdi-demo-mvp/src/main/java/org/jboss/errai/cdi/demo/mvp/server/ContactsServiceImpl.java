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
package org.jboss.errai.cdi.demo.mvp.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.cdi.demo.mvp.client.shared.Contact;
import org.jboss.errai.cdi.demo.mvp.client.shared.ContactDetails;
import org.jboss.errai.cdi.demo.mvp.client.shared.ContactsService;

@ApplicationScoped
@Service
public class ContactsServiceImpl implements ContactsService {

    private static final String[] contactsFirstNameData = new String[] {
        "Hollie", "Emerson", "Healy", "Brigitte", "Elba", "Claudio",
        "Dena", "Christina", "Gail", "Orville", "Rae", "Mildred",
        "Candice", "Louise", "Emilio", "Geneva", "Heriberto", "Bulrush",
        "Abigail", "Chad", "Terry", "Bell" };

    private final String[] contactsLastNameData = new String[] {
        "Voss", "Milton", "Colette", "Cobb", "Lockhart", "Engle",
        "Pacheco", "Blake", "Horton", "Daniel", "Childers", "Starnes",
        "Carson", "Kelchner", "Hutchinson", "Underwood", "Rush", "Bouchard",
        "Louis", "Andrews", "English", "Snedden" };

    private final String[] contactsEmailData = new String[] {
        "mark@example.com", "hollie@example.com", "boticario@example.com",
        "emerson@example.com", "healy@example.com", "brigitte@example.com",
        "elba@example.com", "claudio@example.com", "dena@example.com",
        "brasilsp@example.com", "parker@example.com", "derbvktqsr@example.com",
        "qetlyxxogg@example.com", "antenas_sul@example.com",
        "cblake@example.com", "gailh@example.com", "orville@example.com",
        "post_master@example.com", "rchilders@example.com", "buster@example.com",
        "user31065@example.com", "ftsgeolbx@example.com" };

    private final HashMap<String, Contact> contacts = new HashMap<String, Contact>();

    public ContactsServiceImpl() {
        initContacts();
    }

    private void initContacts() {
        // TODO: Create a real UID for each contact
        for (int i = 0; i < contactsFirstNameData.length
            && i < contactsLastNameData.length
            && i < contactsEmailData.length; ++i) {
            Contact contact = new Contact(String.valueOf(i), contactsFirstNameData[i], contactsLastNameData[i],
                contactsEmailData[i]);
            contacts.put(contact.getId(), contact);
        }
    }

    public Contact addContact(Contact contact) {
        contact.setId(String.valueOf(contacts.size()));
        contacts.put(contact.getId(), contact);
        return contact;
    }

    public Contact updateContact(Contact contact) {
        contacts.remove(contact.getId());
        contacts.put(contact.getId(), contact);
        return contact;
    }

    public Boolean deleteContact(String id) {
        contacts.remove(id);
        return true;
    }

    @Override
    public ArrayList<ContactDetails> deleteContacts(ArrayList<String> ids) {

        for (int i = 0; i < ids.size(); ++i) {
            deleteContact(ids.get(i));
        }

        return getContactDetails();
    }

    public ArrayList<ContactDetails> getContactDetails() {
        ArrayList<ContactDetails> contactDetails = new ArrayList<ContactDetails>();

        Iterator<String> it = contacts.keySet().iterator();
        while (it.hasNext()) {
            Contact contact = contacts.get(it.next());
            contactDetails.add(contact.getLightWeightContact());
        }

        return contactDetails;
    }

    public Contact getContact(String id) {
        return contacts.get(id);
    }
}
