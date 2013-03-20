package org.jboss.errai.maven.cordova

import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.project.MavenProject

/**
 * @goal emulator
 * @author edewit@redhat.com
 */
class CordovaEmulatorMojo extends CordovaMojo {

    /**
     * The Maven Session Object
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession session;

    /**
     * The Maven Project Object
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    @Override
    void execute() throws MojoExecutionException, MojoFailureException {
        if (!session.userProperties.containsKey('platform')) {
            throw new IllegalArgumentException('you should specify for which platform to run example (-Dplatform=android)')
        }

        def platform = session.userProperties.platform.toLowerCase()
        if (!supportedPlatforms*.toLowerCase().contains(platform))
            throw new IllegalArgumentException("only $supportedPlatforms are supported")

        if (platform == 'ios') {
            ant.exec(failonerror: "true",
                    dir: "${project.build.directory}/template",
                    executable: "./ios-sim launch platforms/$platform/build/${config.name}.app")
        } else {
            ant.exec(failonerror: "true",
                    dir: "${project.build.directory}/template/platforms/$platform/cordova",
                    executable: "./cordova") {
                arg(line: 'run')
            }
        }

        if (ant.project.properties.cmdExit) {
            throw new Error("An error occurred while starting the $platform emulator. ${ant.project.properties.cmdExit}");
        }
    }
}
