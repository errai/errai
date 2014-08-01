package org.jboss.errai.maven.cordova

import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

/**
 * @author edewit@redhat.com
 */
class AndroidParserTest extends GroovyTestCase {

    AndroidParser parser
    File configFile
    File strings
    File manifest
    File androidConfig
    File source

    @Override
    protected void setUp() throws Exception {
        super.setUp()
        source = new File('/tmp/src/src/test')
        source.mkdirs()


        def dir = new File('/tmp/src/main/res/config')
        dir.mkdirs()

        strings = new File(dir, 'strings.xml')
        strings.createNewFile()
        strings.write('''
            <test>
                <string name="app_name">HelloCordova</string>
            </test>
        ''')

        manifest = new File(dir, 'AndroidManifest.xml')
        manifest.write('<manifest package="test"/>')

        androidConfig = new File(dir, 'config.xml')
        androidConfig.write(
        '''
            <cordova>
                <access origin="http://google.com" />
                <preference name="fullscreen" value="true" />
            </cordova>
        ''')

        configFile = File.createTempFile('test', 'xml')
        configFile.write(
        '''
            <widget id="org.jboss.test">
                <name>The configured name of the app</name>
                <access origin="*" />
                <preference name="useBrowserHistory" value="false" />
                <preference name="fullscreen" value="false" />
                <preference name="target-device" value="universal" />
            </widget>
        ''')

        parser = new AndroidParser(new File('/tmp/src'))
    }

    @Override
    protected void tearDown() throws Exception {
        new File('/tmp/src').deleteDir()
        configFile.deleteOnExit()
        super.tearDown()
    }

    void testShouldFindStringsFileWhenConstructed() {
        assert parser.strings != null
        assertEquals parser.strings, strings
    }

    void testChangeStringsFile() {
        //when
        parser.updateProject(new ConfigParser(configFile))

        //then
        assertXmlEqual("<test><string name='app_name'>The configured name of the app</string></test>", strings.text)
    }

    void testChangePackageNameAndroidManifest() {
        //when
        parser.updateProject(new ConfigParser(configFile))

        //then
        assertXmlEqual("<manifest package='org.jboss.test'/>", manifest.text)
    }

    static assertXmlEqual(expected, actual) {
        XMLUnit.setIgnoreWhitespace(true)
        def diff = new Diff(expected, actual)
        new DetailedDiff(diff).allDifferences.each {
            println "diff = $it"
        }
        assert diff.identical()
    }

    def void testShouldMoveJavaFilesToNewPackage() {
        //given
        def src = File.createTempFile("source", ".java", source)
        new File('/tmp/src/src/other').mkdir()
        def otherPackage = File.createTempFile("other", '.java', new File('/tmp/src/src/other'))
        src << 'package test.package;'

        //when
        parser.updateProject(new ConfigParser(configFile))

        //then
        def movedFile = new File("/tmp/src/src/org/jboss/test/${src.name}")
        assert movedFile.isFile()
        assertEquals('package org.jboss.test;', movedFile.text)
        assert otherPackage.absolutePath.contains('other')
    }

    def void testShouldUpdateWhiteListInConfig() {
        //when
        parser.updateProject(new ConfigParser(configFile))
        def config = new ConfigParser(androidConfig)

        //then
        assertEquals(['*'], config.access)
    }

    def void testShouldUpdatePreferences() {
        //when
        parser.updateProject(new ConfigParser(configFile))
        def config = new ConfigParser(androidConfig)

        //then
        def expected = [useBrowserHistory:'false', 'exit-on-suspend':'false', fullscreen:'false', 'target-device':'universal']
        assertEquals(expected, config.preference)
    }
}
