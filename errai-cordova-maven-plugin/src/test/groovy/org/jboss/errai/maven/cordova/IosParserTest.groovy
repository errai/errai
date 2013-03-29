package org.jboss.errai.maven.cordova

import static org.jboss.errai.maven.cordova.AndroidParserTest.assertXmlEqual

/**
 * @author edewit@redhat.com
 */
class IosParserTest extends GroovyTestCase {

    IosParser parser
    File baseDir
    File plist
    File iosConfig
    File configFile

    @Override
    public void setUp() throws Exception {
        baseDir = new File('/tmp/iosproject')
        baseDir.mkdir()
        def project = new File(baseDir, 'HelloCordova')
        project.mkdir()

        new File(baseDir, 'HelloCordova.xcodeproj').mkdir()

        iosConfig = new File(project, 'config.xml')
        iosConfig.write '''
            <widget>
                <access origin="http://google.com" />
            </widget>
        '''

        plist = new File(project, 'HelloCordova-Info.plist')
        plist.write '''<?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple Computer//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
              <dict>
                <key>CFBundleIdentifier</key>
                <string>io.cordova.hellocordova</string>
              </dict>
            </plist>
        '''

        configFile = File.createTempFile('test', 'xml')
        configFile <<
                '''
            <widget id="org.jboss.test">
                <name>The configured name of the app</name>
                <access origin="*" />
                <preference name="target-device" value="universal" />
                <preference name="KeyboardDisplayRequiresUserAction" value="false"/>
            </widget>
        '''
        parser = new IosParser(baseDir)
    }


    @Override
    public void tearDown() throws Exception {
        configFile.deleteOnExit()
        baseDir.deleteOnExit()
    }

    void testShouldUpdateBundleId() {
        //when
        parser.updateBundleId(new ConfigParser(configFile))

        //then
        assertTrue(plist.text.contains('<string>org.jboss.test</string>'))
    }

    void testShouldUpdateAccess() {
        //when
        parser.updateConfig(new ConfigParser(configFile))

        //then
        assertXmlEqual(
                '''
                <widget>
                  <access origin="*"/>
                </widget>
        ''', iosConfig.text)
    }

    void testShouldUpdatePreferences() {
        //when
        parser.updatePreferences(new ConfigParser(configFile))

        //then
        assertXmlEqual('''
            <widget>
              <access origin="http://google.com"/>
              <preference name="KeyboardDisplayRequiresUserAction" value="false"/>
              <preference name="SuppressesIncrementalRendering" value="false"/>
              <preference name="UIWebViewBounce" value="true"/>
              <preference name="TopActivityIndicator" value="gray"/>
              <preference name="EnableLocation" value="false"/>
              <preference name="EnableViewportScale" value="false"/>
              <preference name="AutoHideSplashScreen" value="true"/>
              <preference name="ShowSplashScreenSpinner" value="true"/>
              <preference name="MediaPlaybackRequiresUserAction" value="false"/>
              <preference name="AllowInlineMediaPlayback" value="false"/>
              <preference name="OpenAllWhitelistURLsInWebView" value="false"/>
              <preference name="BackupWebStorage" value="cloud"/>
              <preference name="target-device" value="universal"/>
            </widget>
        ''', iosConfig.text)
    }
}
