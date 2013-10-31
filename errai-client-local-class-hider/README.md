client-local-class-hider
========================

A Java agent that hides the true definition of classes whose names match an
arbitrary regular expression.


Usage
-----

Speficy the JAR file to the -javaagent command line parameter when launching
the Java Virtual Machine.


Options
-------

Options are specified after the JAR file name as a comma-separated list of
*name=value* pairs:

    java -javaagent:/path/to/client-local-class-hider-1.0-SNAPSHOT.jar=opt1=val1,opt2-val2 com.xyz.MyMainClass

##Available options:

###debugAgent=*(true|false)*

Controls the printing of debug messages to System.out. Default is false
(debug messages are not printed)
   
###classPattern=*regex*

Class names matching this regular expression will be hidden by the agent.
The regular expression matches against "internal" class names of the form
java/lang/Object or java/util/List.
   
If not specified, the default pattern `.*/client/local/.*` is used.


Using with JBoss AS
-------------------

To use this agent in JBoss AS7, add the following to the end of standalone.conf:

    JAVA_OPTS="$JAVA_OPTS -javaagent:/path/to/client-local-class-hider-1.0-SNAPSHOT.jar"

