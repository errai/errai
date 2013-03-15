File androidPlatform = new File(basedir, 'target/template/platforms/android/')
File sourceFolder = new File( androidPlatform, 'src/org/jboss/test/');

assert sourceFolder.isDirectory()
assert sourceFolder.list().length == 1

assert new File(androidPlatform, 'AndroidManifest.xml').text.contains('org.jboss')
