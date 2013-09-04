
# Aerogear wrappers for Errai demo project

This project demos the Aerogear wrappers for Errai. It's an implementation of the Aerogear TODO demo frontend
with Errai-UI and the wrappers. To make this work first checkout the Aerogear demo.

    git clone git@github.com:aerogear/TODO.git

Then build the ear file:
    mvn clean install

Build this project into a war:
    <errai-demo-location>/errai-aerogear-demo/mvn clean install

Now we'll remove the original client war from the ``todo-ear.ear`` and replace it with this one
    cp <errai-demo-location>/errai-aerogear-demo/target/todo-www.war .
    zip -g ear/target/todo-ear.ear todo-www.war

Then start your jboss server copy the ear file into the deployment directory

