Errai To-Do List Demo
=====================

This demo app shows how to use the following technologies together:

 * ErraiCDI
 * ErraiJPA
 * ErraiUI
 * Errai Data Binding

For a quick but thorough overview of the Java code and HTML templates that make up this demo,
check out [the 7-minute screencast](https://vimeo.com/55454764) of this demo being created!

Try It Out
==========

To try it in dev mode, ensure you have Maven 3 installed, then type the following at a command prompt:

    % mvn gwt:run

To build a .war file that you can deploy to JBoss EAP 6, AS 7 or the OpenShift cloud:

    % mvn clean package -Pjboss7

Then copy target/errai-jpa-demo-todo-list.war to the appropriate location.
