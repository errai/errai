package org.jboss.errai.maven.cordova

import org.apache.commons.configuration.plist.XMLPropertyListConfiguration

/**
 * @author edewit@redhat.com
 */
class IosParser {
    def defaultPreferences = [
            KeyboardDisplayRequiresUserAction: 'true',
            SuppressesIncrementalRendering: 'false',
            UIWebViewBounce: 'true',
            TopActivityIndicator: 'gray',
            EnableLocation: 'false',
            EnableViewportScale: 'false',
            AutoHideSplashScreen: 'true',
            ShowSplashScreenSpinner: 'true',
            MediaPlaybackRequiresUserAction: 'false',
            AllowInlineMediaPlayback: 'false',
            OpenAllWhitelistURLsInWebView: 'false',
            BackupWebStorage: 'cloud',
    ]

    File baseDir
    File xcodeproj
    def ant
    ConfigParser iosConfig

    IosParser(File baseDir) {
        ant = new AntBuilder()
        this.baseDir = baseDir
        baseDir.eachFileRecurse({
            if (it.name =~ /\.xcodeproj$/) xcodeproj = it
            if (it.name =~ /config.xml$/) iosConfig = new ConfigParser(it)
        })
    }

    def updateProject(ConfigParser config) {
        updateBundleId(config)
        updateConfig(config)
        updatePreferences(config)
        updateProductName(config)
    }

    def updateBundleId(ConfigParser config) {
        def name = xcodeproj.name
        name = name.substring(0, name.indexOf('.xcodeproj'))

        def infoPlist = new File(baseDir, "$name${File.separator}$name-Info.plist")

        def configuration = new XMLPropertyListConfiguration(infoPlist)
        configuration.setProperty('CFBundleIdentifier', config.packageName)
        configuration.save(new FileWriter(infoPlist))
    }

    def updateConfig(ConfigParser config) {
        iosConfig.removeAccess()
        config.access.each {
            iosConfig.addAccess(it)
        }
    }

    void updatePreferences(ConfigParser config) {
        iosConfig.removePreference()
        iosConfig.addPreference(defaultPreferences + config.preference)
    }

    void updateProductName(ConfigParser config) {
        def configuration = new File(xcodeproj, 'project.pbxproj')
        def text = configuration.text.replaceAll('PRODUCT_NAME = ".*";', "PRODUCT_NAME = \"${config.name}\";")
        configuration.write text
    }
}
