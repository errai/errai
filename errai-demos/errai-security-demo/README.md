# Errai Security Demo

## Overview

This simple app demonstrates how Errai Security can be used in conjunction with Errai UI, Errai Navigation, Errai RPC, and Errai JAX-RS to declaratively secure UI elements, pages, and remote services.

### Errai UI

Errai UI allows fields in plain HTML5 and CSS templates to be bound to fields in Java classes. Errai Security enhances this by providing a declarative syntax to hide Errai UI fields from unauthorized users.

### Errai Navigation

Errai Navigation allows you to declaratively define pages within your single page app with compile-time verified links. Errai Security provides a declarative syntax for restricting an unauthorized user's access to pages.

### Errai RPC and JAX-RS Services

Errai RPC and Errai JAX-RS provide a convenient, type-safe way of accessing services from the client, either through the Errai Message Bus or from a Rest Service, respectively. Errai Security can be used to declare either kind of service interface as restricted, so that it cannot be accessed by unauthorized users.

## Try It Out

### Get It Running

To try it in dev mode, ensure you have Maven 3 installed, then type the following at a command prompt:

    % mvn gwt:run

To deploy a .war file to JBoss EAP 6, or AS 7, start the application server and run:

    % mvn clean package jboss-as:deploy

### Demo App Pages

#### Welcome Page
* Greets a logged in user with their name.

#### Login Page
* Allows users to log in and out.
* A user will be redirected here if they attempt to access a restricted resource while unauthenticated.

#### Messages Page
* Contains buttons which trigger remote services.
* The "Say Hello!" service uses JAX-RS and requires a user to be authenticated.
* THe "Ping for admin" service uses Errai RPC and requires a user to be authenticated as a user with the role "admin".

#### Admin Page
* This page is only accessible by users with the role admin.
* You can try to setting `#AdminPage` as the hash value in the url.

#### Security Error Page
* A user is directed here when they attempt to access a service for which they do not have authorization.

### The Navigation Bar

The navigation bar has links to the first three pages described above when not logged in. It does also have a link to the Admin Page, but this link is hidden when not logged in as a user with the role "admin".

### Try Logging In

This demo has to users:

#### John
* **username**: john
* **password**: 123
* **roles**: admin

#### Hacker
* **username**: hacker
* **password**: 123

## Troubleshooting

Here are some resources that may help if you encounter difficulties:
* [Website](http://erraiframework.org/)
* [FAQ](tutorial-guide/FAQ.adoc)
* [Forum](https://community.jboss.org/en/errai)
* IRC : #errai @ freenode

