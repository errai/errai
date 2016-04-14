Errai-Bus Stress Test Example App
=================================

This simple application serves both as an example of how to use the GWT UiBinder
framework within Errai, as well as an easy way of testing bus throughput in your
own environment.

Building it
-----------

To build the app, test it, and produce a WAR file suitable for deployment to your app server,
execute the following command:

    mvn clean package -Pintegration-test -P<app-server-profile>

Available app server profile names are wildfly and tomcat. Remember to clean every
time you build for a different app server.


Running it in Dev Mode
----------------------

To run the app in GWT Dev Mode, execute the command

    mvn gwt:run

There is no need to build the project first; Dev Mode uses the app's sources, not the compiled
classes.
