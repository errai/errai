Errai Multi-Module Demo
=======================

Note: this demo application is not done yet! See the TODO section below for an outline of
what remains to be done.


This demo app shows how to use the following technologies together:

 * ErraiCDI
 * ErraiJPA
 * ErraiUI
 * Errai Data Binding
 * Bean Validation (TODO)
 * Twitter Bootstrap

Try It Out
==========

To try it in dev mode, ensure you have Maven 3 installed, then type the following at a command prompt:

    % mvn gwt:run

To build a .war file that you can deploy to Wildfly 8 or the OpenShift cloud:

    % mvn clean package -Pwildfly

Then copy target/errai-jpa-demo-grocery-list.war to the appropriate location.


TODO
====

This app is currently a work in progress. The following items remain to be done:

 * Checking off an item (by click or swipe gesture)
 * Editing an existing item (by double click on it)
 * Sort items by:
   * Name
   * Department name
     * Alphabetically
     * In "store order" (order of visiting departments when walking through store)
     * With location awareness to suggest a store automatically
   * Date added
 * The Stores page
   * Basic add/remove functionality
   * Defining the order of departments within the store
   * Google Maps integration for setting store's location
 * Use Bean Validation annotations on the Store, Department, Item entities; validate before submitting to local storage
 * Sync data to server using Errai Data Sync
   * List sharing among users, with push updates
 * HTML5 offline support (app cache manifest)
