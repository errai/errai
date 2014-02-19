Errai Security Demo
=======================

This demo showcases Errai Security, which allows you to restrict user's access to pages and services using declaritive syntax.

Try It Out
==========

To try it in dev mode, ensure you have Maven 3 installed, then type the following at a command prompt:

    % mvn gwt:run

To deploy a .war file to JBoss EAP 6, or AS 7, start the application server and run:

    % mvn clean package jboss-as:deploy

What It Does
============

This app demonstrates how client-side pages and server-side services can be restricted based on user roles. Try it out by logging in as either of the two users:

username: John
password: 123

or

username: Hacker
password: 123

