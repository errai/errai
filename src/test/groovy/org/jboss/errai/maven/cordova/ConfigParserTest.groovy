package org.jboss.errai.maven.cordova

/**
 * @author edewit@redhat.com
 */
class ConfigParserTest extends GroovyTestCase {


    File configFile
    ConfigParser parser

    @Override
    public void setUp() throws Exception {
        //given
        configFile = File.createTempFile('config', 'xml')
        configFile << '''
            <widget id="io.cordova.hellocordova" version="2.0.0" xmlns="http://www.w3.org/ns/widgets" xmlns:gap="http://phonegap.com/ns/1.0">
                <name>HelloCordova</name>
                <access origin="*" />
                <preference name="fullscreen" value="false" />
            </widget>'''
        parser = new ConfigParser(configFile)
    }

    @Override
    protected void tearDown() throws Exception {
        configFile?.deleteOnExit()
    }

    def void testParser() {

        //then
        assertEquals('HelloCordova', parser.name)
        assertEquals('io.cordova.hellocordova', parser.packageName)

        assertEquals(['*'], parser.access)
    }

    def void testRemoveAccess() {

        parser.addAccess('www.redhat.com')
        parser.removeAccess('*')

        //then
        assert !configFile.text.contains('*')
        assertEquals(['www.redhat.com'], parser.access)

        parser.addAccess('*')
        parser.removeAccess()
        assertEquals([], parser.access)
    }

    def void testPreferences() {

        assertEquals([fullscreen: 'false'], parser.preference)
        parser.addPreference([orientation: 'default'])
        assertEquals([fullscreen: 'false', orientation: 'default'], parser.preference)
        parser.removePreference('orientation')
        assertEquals([fullscreen: 'false'], parser.preference)
        parser.addPreference(['target-device': 'universal'])
        assertEquals([fullscreen: 'false', 'target-device': 'universal'], parser.preference)
        parser.removePreference()
        assertEquals([:], parser.preference)
    }

}
