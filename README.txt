
Running and testing the CDI integration
=======================================

1.) Build the top level module first (!)

	mvn clean install

2.) Change into the 'example/gui' directory

Either run

	mvn gwt:run

or

	mvn gwt:debug

to launch the hosted mode browser


3.) Connect with a browser of your choice to the URL given in the hosted mode browser.
The default login is 'admin:admin'

    ||
    || NOTE: This does require installation of the OOPHM plugin:
    || http://code.google.com/p/google-web-toolkit/wiki/UsingOOPHM
    ||
        
Deployment on JBoss-6.0.0
=========================

The current setup bootstraps the CDI container within the servlet engine
when running in hosted mode. However when running on JBoss 6, CDI already
ships with the application server.

In order to successfully deploy to JBoss 6, three steps are necessary:

1) Do a clean rebuild (!) with the '-Pjboss6' profile switch
 
 mvn -Pjboss6 clean install

2) Update the installer properties

 Change 'jboss.home' and 'jboss.deploy.path' in the install/build.properties file.

3) Run the installer

  Switch into the 'install' directory
  Execute 'ant'


    ||
    || NOTE: Make sure to run a clean build every time you switch between hosted mode
    || and deployment on JBoss 6. It replaces the web.xml.
    ||

Examples on jboss6
==================

The example application runs under:

    http://localhost:8080/errai-cdi


Troubleshooting
===============

If you run into trouble don't hesitate to get in touch with us:

User Forum:     http://community.jboss.org/en/errai?view=discussions 
Mailing List:   http://jboss.org/errai/MailingLists.html
IRC:            irc://irc.freenode.net/errai



Have fun,
The Errai Team


