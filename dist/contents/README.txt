
Errai Framework 1.3 (http://jboss.org/errai)
=================================================

Errai is a GWT-based framework for building rich web applications
using next-generation web technologies. Built on-top of ErraiBus,
the framework provides a unified federation and RPC infrastructure with true,
uniform, asynchronous messaging across the client and server.

Having said so, we assume that you have some knowledge about Java and especially GWT.
If you don't know anything about GWT, then we suggest you make yourself familiar with it
before reading any further:

    GWT Introduction: http://code.google.com/webtoolkit/overview.html    

[Contents of this distribution]

    |-deps
    |---lib 		(third party libs)
    |-docs
    |---reference	(reference guide)
    |---quickstart	(quickstart guide)
    |-examples		(demo apps)
    |-libi		(errai libs)
    

[Running the examples]

The examples come with a prebuild maven setup. In order to run them
you need to execute the following command within an example directory:

    mvn gwt:run

This will lauch the GWT hosted mode which allows you to access the
application using any web browser available on your system.

Alternatively you can import the example in your IDE
(simply import the maven structure) and launch them using your IDE GWT plugin.

[Troubleshooting]

If you run into trouble don't hesitate to get in touch with us:

User Forum:     https://community.jboss.org/en/errai?view=discussions
Mailing List:   http://jboss.org/errai/MailingLists.html
IRC:            irc://irc.freenode.net/errai

Have fun,
The Errai Team
