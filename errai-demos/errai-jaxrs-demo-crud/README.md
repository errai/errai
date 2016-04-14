Errai Multi-Module Demo
=======================

Note: this demo application is not done yet! See the TODO section below for an outline of
what remains to be done.


This demo app shows how to use the following technologies together:

 * ErraiJAXRS

Try It Out
==========

To try it in dev mode, ensure you have Maven 3 installed, then type the following at a command prompt:

    % mvn gwt:run

To build a .war file that you can deploy to Wildfly 8 or the OpenShift cloud:

    % mvn clean package -Pwildfly

Then copy target/errai-jaxrs-demo-crud.war to the appropriate location.

