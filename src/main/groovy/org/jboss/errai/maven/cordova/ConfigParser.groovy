package org.jboss.errai.maven.cordova

/**
 * @author edewit@redhat.com
 */
class ConfigParser {

    File configFile
    def config

    ConfigParser(File config) {
        this.configFile = config;
        this.config = new XmlParser(false, false).parse(config)
    }

    String getPackageName() {
        config.@id;
    }

    String getName() {
        config.name.text()
    }

    List<String> getAccess() {
        config.depthFirst().findAll { it.name() == 'access' }.collect { it.@origin }
    }

    void addAccess(uri) {
        config.appendNode (
            'access', [origin: uri]
        )

        writeToFile()
    }

    void removeAccess(uri) {
        if (uri) {
            config.remove config.depthFirst().find { it.@origin == uri }
        } else {
            for (e in config.depthFirst().findAll { it.name() == 'access' })
                config.remove e
        }
        writeToFile()
    }

    Map<String, String> getPreference() {
        config.depthFirst().findAll { it.name() == 'preference' }.inject([:]) {map, node -> map + [(node.@name): node.@value]}
    }

    void addPreference(pref) {
        for (e in pref) {
            config.appendNode(
                    'preference', [name: e.key, value: e.value]
            )
        }
        writeToFile()
    }

    void removePreference(name) {
        if (name) {
            config.remove config.depthFirst().find { it.@name == name}
        } else {
            for (e in config.depthFirst().findAll { it.name() == 'preference' })
                config.remove e
        }
        writeToFile()
    }

    def void writeToFile() {
        new XmlNodePrinter(new PrintWriter(new FileWriter(configFile))).print(config)
    }
}
