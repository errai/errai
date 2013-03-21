package org.jboss.errai.maven.cordova

/**
 * @goal emulator
 * @author edewit@redhat.com
 */
class CordovaEmulatorMojo extends CordovaMojo {

    @Override
    void execute() {
        if (!session.userProperties.containsKey('platform')) {
            throw new IllegalArgumentException('you should specify for which platform to run example (-Dplatform=android)')
        }

        def platform = session.userProperties.platform.toLowerCase()
        if (!supportedPlatforms*.toLowerCase().contains(platform))
            throw new IllegalArgumentException("only $supportedPlatforms are supported")

        if (platform == 'ios') {
            ant.exec(failonerror: "true",
                    dir: "${project.build.directory}/template",
                    executable: "./ios-sim") {
                arg(line: "launch platforms/$platform/build/${config.name}.app")
            }
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
