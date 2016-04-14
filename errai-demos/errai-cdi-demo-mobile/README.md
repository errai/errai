Errai Multi-Module Demo
=======================

This demo app shows how to use the following technologies together:

 * ErraiCDI

Try It Out
==========

To try it in dev mode, ensure you have Maven 3 installed, then type the following at a command prompt:

    % mvn gwt:run

To build a .war file that you can deploy to Wildfly 8 or the OpenShift cloud:

    % mvn clean package -Pwildfly

Then copy target/errai-cdi-demo-mobile.war to the appropriate location.

