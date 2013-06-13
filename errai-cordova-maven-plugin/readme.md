Cordova maven plugin
====================

This plugin is based on the great cordova-cli tool. To get started add the following to the build section
of your `pom.xml`

```xml

<build>
    <plugins>
      <plugin>
        <groupId>org.jboss.errai</groupId>
        <artifactId>cordova-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <executions>
            <execution>
                <id>build</id>
                <phase>package</phase>
                <goals>
                    <goal>build-project</goal>
                </goals>
            </execution>
        </executions>
      </plugin>

```
The configuration above will execute one of the plugin goals `build-project` at the `package` build phase you can also
omit this to speedup building and then later execute `mvn cordova:build-project` to build the native parts.
Right now _only_ android and ios are supported.

Add the following to for maven to be able to find the plugin:

```xml

    <pluginRepositories>
        <pluginRepository>
            <id>snapshots.jboss.org</id>
            <name>JBoss Snapshot Repository</name>
            <url>http://snapshots.jboss.org/maven2</url>
            <layout>default</layout>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
```

You can also start the simulator by excecuting the following for ios `mvn cordova:emulator -Dplatform=ios`

