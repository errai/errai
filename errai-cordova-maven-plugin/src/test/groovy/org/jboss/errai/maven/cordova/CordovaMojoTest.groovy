package org.jboss.errai.maven.cordova

/**
 * @author edewit@redhat.com
 */
class CordovaMojoTest extends GroovyTestCase {

    void testShouldCreateCleanCopy() {
        File dir = createTempDir()

        def fileToBeRemoved = new File(dir, "test.html")
        fileToBeRemoved << "some text"

        def source = createTempDir()
        def fileToBeCopied = new File(source, "copied.html")
        fileToBeCopied << "I'm copied"

        def mojo = new CordovaMojo()
        mojo.clean(dir)
        mojo.copy(dir, source)

        assert fileToBeCopied.isFile()
        assert dir.listFiles().length != 0
        assertEquals dir.listFiles()[0].name, fileToBeCopied.name
        assert !fileToBeRemoved.isFile()
    }

    static def File createTempDir() {
        def dir = File.createTempFile("some", "dir")
        dir.delete()
        dir.mkdir()
        dir
    }

}
