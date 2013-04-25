This demo is based on the article "Large scale application development and MVP" by Chris Ramsdale, Google Developer Relations [1]. It was modified to show the benefits of adding Errai [2].

To launch the GWT development mode, change into the project directory and type:

    mvn gwt:run (launch hosted mode)
    mvn gwt:debug (launch hosted with debug settings)

By default the archetype does package the web application for Development Mode execution. To deploy your application to JBoss AS 7, you need to execute a clean rebuild using the JBoss profile (e.g. -Pjboss7).

    mvn -Pjboss7 clean install
    cp target/errai-mvp.war $JBOSS_HOME/standalone/deployments

1. http://code.google.com/webtoolkit/articles/mvp-architecture.html
2. http://jboss.org/errai