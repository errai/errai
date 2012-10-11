Errai Multi-Module Demo
=======================

This demo app shows how to use the following technologies together:

 * ErraiCDI
 * ErraiJPA
 * ErraiUI
 * Errai Data Binding
 * Twitter Bootstrap

Try It Out
==========

To try it in dev mode, ensure you have Maven 3 installed, then type the following at a command prompt:

    % mvn gwt:run

To build a .war file that you can deploy to JBoss EAP 6, AS 7 or the OpenShift cloud:

    % mvn clean package -Pjboss7

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
 * Google Maps integration for setting up store location
 * Sync data to server using Errai Data Sync
   * List sharing among users, with push updates
 * HTML5 offline support (app cache manifest)
