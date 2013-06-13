package org.jboss.errai.maven.cordova

import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.project.MavenProject
import org.codehaus.gmaven.mojo.GroovyMojo
import org.codehaus.plexus.util.xml.Xpp3Dom

import static org.twdata.maven.mojoexecutor.MojoExecutor.*

/**
 * @goal build-project
 */
class CordovaMojo extends GroovyMojo {
    /**
     * The Maven Project Object
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The Maven Session Object
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession session;

    /**
     * The Maven PluginManager Object
     *
     * @component
     * @required
     */
    protected BuildPluginManager pluginManager;

    void execute() {
        unpackProjectTemplate()
        copyContent()
        updateConfig()
        compile()
    }

    def supportedPlatforms = ['Android', 'Ios']

    void unpackProjectTemplate() {
        executeMojo(
                plugin(
                        groupId('org.apache.maven.plugins'),
                        artifactId('maven-dependency-plugin'),
                        version('2.1')
                ),
                goal('unpack'),
                configuration(
                        element(name('artifactItems'), element(name('artifactItem'),
                                element(name('groupId'), 'org.jboss.errai'),
                                element(name('artifactId'), 'errai-cordova-template'),
                                element(name('version'), '3.0-SNAPSHOT'),
                                element(name('type'), 'tar.gz'),
                                element(name('overWrite'), 'false'),
                                element(name('outputDirectory'), '${project.build.directory}/template')
                        )),
                ),
                executionEnvironment(
                        project,
                        session,
                        pluginManager
                )
        )
    }

    void copyContent() {
        def template = "${project.build.directory}/template"
        assert new File(template).isDirectory()

        def androidDir = "${template}/platforms/android/assets/www"
        def iosDir = "${template}/platforms/ios/www/"

        def www = "${project.build.directory}/${project.build.finalName}"

        [androidDir, iosDir].each { dir ->
            clean(dir)
            copy(dir, www)
        }

        copy("${template}/www/", www)
        ant.copy(file: "${template}/www/config.xml", todir: warSourceDir)
    }

    void updateConfig() {
        for (platform in supportedPlatforms) {
            def baseDir = new File("${project.build.directory}/template/platforms/$platform")
            def parser = Class.forName("org.jboss.errai.maven.cordova.${platform}Parser").newInstance(baseDir)
            parser.updateProject(getConfig())
        }
    }

    def ConfigParser getConfig() {
        new ConfigParser(new File("${warSourceDir}/config.xml"))
    }

    void compile() {
        supportedPlatforms.each {
            ant.exec(failonerror: "true",
                    dir: "${project.build.directory}/template/platforms/${it.toLowerCase()}/cordova",
                    executable: './build')

            if (ant.project.properties.cmdExit) {
                throw new Error("An error occurred while building the $it project. ${ant.project.properties.cmdExit}");
            }
        }
    }

    String getWarSourceDir() {
        Xpp3Dom config = project.buildPlugins.find{it.key == 'org.apache.maven.plugins:maven-war-plugin'}.configuration
        if (config) {
            def found = config.getChildren().find { it.name == 'warSourceDirectory' }
            if (found) {
                return found.value
            }
        }
        return 'src/main/webapp'
    }

    def void copy(dir, www) {
        ant.copy(todir: dir) {
            fileset(dir: www, includes: '**/*.htm, **/*.html, **/*.gif, **/*.jpg, **/*.jpeg, **/*.png, **/*.js, **/*.css')
        }
    }

    def void clean(dir) {
        ant.delete(includeemptydirs: 'true', {
            fileset(dir: dir, includes: '**/*')
        })
    }
}
