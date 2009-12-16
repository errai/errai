
Welcome to Project Errai (http://jboss.org/errai)
=================================================

[Prerequisites]

Errai is a GWT-based framework for building rich web applications
using next-generation web technologies. Built on-top of ErraiBus,
the framework provides a unified federation and RPC infrastructure with true,
uniform, asynchronous messaging across the client and server.

Having said so, we assume that you have some knowledge about Java and especially GWT.
If you don't know anything about GWT, then we suggest you make yourself familiar with it
before reading any further:

    GWT Introduction: http://code.google.com/webtoolkit/overview.html
    

[Contents of this distribution]

    |-examples
    |---clientservice
    |---helloworld
    |---queryservice
    |---serialization
    |-lib    

The examples document the core features that currently ship with Errai:

    - clientservice: a message broadcast example
    - helloworld: everyone has to have one
    - queryservice: using a request/response pattern across the bus
    - serialization: sending pojo's over the bus


[Running the examples]

The examples come with a prebuild maven setup. In order to run them
you need to execute the following command within an example directory:

    mvn gwt:run

This will lauch the GWT hosted mode which allows you to access the
application using any web browser available on your system.

Alternatively you can import the example in your IDE
(simply import the maven structure) and launch them using your IDE GWT plugin.


[Quickstart]

In order to get you started quickly, we've included a mavena archetype
that creates a project structure similar to the examples:

    mvn archetype:generate \
	-DarchetypeGroupId=org.jboss.errai \
	-DarchetypeArtifactId=sandbox-archetype \
	-DarchetypeVersion=1.0-SNAPSHOT \
	-DarchetypeRepository=http://snapshots.jboss.org/maven2

Run it in a directory of your choice and create a bare bone Errai project
within minutes.


[Documentation & Userguide]

This is still work in progress. In the meantime you can find the most
up to date documentation on the Errai website: http://jboss.org/errai

[Troubleshooting]

If you run into trouble don't hesitate to get in touch with us:

User Forum:     http://www.jboss.org/index.html?module=bb&op=viewforum&f=295
Mailing List:   http://jboss.org/errai/MailingLists.html
IRC:            irc://irc.freenode.net/errai



Have fun,
The Errai Team