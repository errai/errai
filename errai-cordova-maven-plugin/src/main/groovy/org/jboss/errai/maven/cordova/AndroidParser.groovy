package org.jboss.errai.maven.cordova

import groovy.xml.StreamingMarkupBuilder

/**
 * @author edewit@redhat.com
 */
class AndroidParser {
    def defaultPreferences = [
            useBrowserHistory: "true",
            "exit-on-suspend": "false"
    ]

    File baseDir
    File strings
    File manifest
    File androidConfig
    def ant

    AndroidParser(File baseDir) {
        ant = new AntBuilder()
        this.baseDir = baseDir
        baseDir.eachFileRecurse({
            if (it.name =~ /strings.xml$/) strings = it
            if (it.name =~ /AndroidManifest.xml$/) manifest = it
            if (it.name =~ /config.xml$/) androidConfig = it
        })
    }

    def updateProject(ConfigParser config) {
        updateAppNameInStrings(config)
        updatePackageName(config)
        updateWhiteList(config)
        updatePreferences(config)
    }

    def void updateAppNameInStrings(ConfigParser config) {
        def stringsXml = new XmlSlurper().parse(strings)
        stringsXml.string.findAll { it.@name == 'app_name' }[0] = config.name

        writeToFile(strings, stringsXml)
    }

    def void updatePackageName(ConfigParser config) {
        def manifestXml = new XmlSlurper(false, false).parse(manifest)
        def src = baseDir.absolutePath + '/src'
        def originalPackage = new File(src, manifestXml.'@package'.toString().replace('.', File.separator))
        def packageName = config.packageName
        manifestXml.'@package' = packageName

        writeToFile(manifest, manifestXml)

        ant.replaceregexp(match: 'package [\\w\\.]*;', replace: "package ${packageName};") {
            fileset(dir: originalPackage) {
                include(name: '**/*.java')
            }
        }

        def packageDir = new File(src, packageName.replace('.', File.separator))
        packageDir.mkdirs()
        ant.move(todir: packageDir, flatten: true) {
            fileset(dir: originalPackage) {
                include(name: '**/*.java')
            }
        }
    }

    def void updateWhiteList(ConfigParser config) {
        def parser = new ConfigParser(androidConfig)
        parser.removeAccess()
        config.access.each { uri ->
            parser.addAccess(uri)
        }
    }

    def updatePreferences(ConfigParser config) {
        def parser = new ConfigParser(androidConfig)
        parser.removePreference()
        parser.addPreference(defaultPreferences + config.preference)
    }

    def void writeToFile(file, stringsXml) {
        def outputBuilder = new StreamingMarkupBuilder()
        file.write outputBuilder.bind { mkp.yield stringsXml }.toString()
    }
}
