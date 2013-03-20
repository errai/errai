package org.jboss.errai.maven.cordova

import groovy.mock.interceptor.MockFor
import org.apache.maven.execution.MavenSession
import org.objenesis.ObjenesisStd

/**
 * @author edewit@redhat.com
 */
class CordovaEmulatorMojoTest extends GroovyTestCase {


    void testShouldParseUserPreferences() {
        //given
        def mocker = new MockFor(MavenSession)
        mocker.demand.with {
            getUserProperties{[:]}
        }

        CordovaEmulatorMojo mojo = new CordovaEmulatorMojo()
        mojo.session = new ObjenesisStd().getInstantiatorOf(MavenSession).newInstance() as MavenSession

        //then

        mocker.use {
            try {
                mojo.execute()
                fail('IllegalArgumentException should have been thrown')
            } catch (IllegalArgumentException e) {
                //success
            }
        }
    }
}
