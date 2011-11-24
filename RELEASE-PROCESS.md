Errai Project Release Process
=============================

This document describes the steps that should be followed during every release of
the Errai project. When making a release, please be careful to follow these
steps in order. If something doesn't make sense, if you perform steps not listed
here, or if after the release, it was discovered that something was forgotten,
please update these notes to reflect the correct procedure.

The hope is that most or all of these steps will be automated. But a necessary
first step is to discover everything that needs to be done in order to make a
release successful.

A note on Errai Theory
----------------------

When the Maven Releae Plugin goes awry, it's a tedious process to back out its
automated git tags and commits. We prefer to follow the steps manually, committing
after each step as we go.

Release Steps
-------------

1. Run the test suite. Ensure all tests pass.
   % mvn -PenableTests test
   
1. Export docbook from confluence for /reference
   * https://docs.jboss.org/author/spaces/jboss_docbook_tools/exportandpostprocessConfigure.action?spaceKey=ERRAI&pageId=5833085
     (Single book output, no static files)
   * Copy and commit the docbook files in /reference. Clean out the old chapters first
     (otherwise there will be old cruft left over if any chapters were renamed or renumbered):
     % git rm reference/chapter-*
     % cp $newdocs/chapter-* reference/
     % git add reference/chatper-*
   * Edit the version numbers in Book_Info.xml to reflect release version
   * UPLOAD SINGLE_HTML OUTPUT, not the multi-page version

1. Update quickstart docs to reflect the new version number

1. Export docbook from confluence for /quickstart
   * https://docs.jboss.org/author/spaces/jboss_docbook_tools/exportandpostprocessConfigure.action?spaceKey=ERRAI&pageId=5833096
     (Single book output, no static files)
   * Copy and commit the docbook files in /quickstart. Verify there is only one chapter of
     each name and one of each number (there will be old cruft left over if any chapters
     were renamed or renumbered)
   * Manually copy the author directory from the downloaded zip into all three docbook
     output directories (html_single, html, pdf)
   * remember to upload single_html, multi-page html, and pdf versions

1. Ask Maven to update the version number in all the pom.xml files:
   % mvn versions:set -newVersion=x.y.z.Final
   Afterward, verify that all subprojects reference the new parent pom's version
   (if any are out of sync with the parent version, Maven will not have updated them)

1. Build and package the release. These are the bits that will be uploaded to nexus.
   Expect this to take about 9 minutes.
   % mvn clean install

1. Upload the release to nexus:
   % mvn deploy

1. Tag and push the release to github:
   % git tag x.y.z.Final
   % git push origin x.y.z
   % git push origin --tags
   % git push upstream x.y.z
   % git push upstream --tags
   
1. Update http://www.jboss.org/errai/Documentation to provide the download links for
   the generated/released docs.
   
   Get into the CMS at https://www.jboss.org/author/. Mike recommends using Firefox
   only: under WebKit browsers, Magnolia sometimes fails to save your work.

1. Publish new quickstart archetypes to Nexus repo (both snapshots and released version)
   % cd $somewhere/archetypes
   % cd bus-quickstart
   % mvn versions:set -newVersion=x.y.z.Final
   % mvn clean install
   
   Now test the archetype you just installed:
   % cd /tmp
   % mvn archetype:generate -DarchetypeGroupId=org.jboss.errai.archetypes -DarchetypeArtifactId=bus-quickstart -D

   If the above was successful, publish away!
   % mvn deploy

1. Create and upload the a-la-carte binary Errai distribution and docs
   % mvn install -Pdistro
   % sftp errai@filemgmt.jboss.org
   
   TODO fix all the pathname stuff here
   sftp> cd docs_htdocs/errai/dist
   sftp> mkdir x.y.z.Final
   sftp> quit
   % scp errai-x.y.z-Final.zip errai@filemgmt.jboss.org:/downloads_htdocs/errai/dist/x.y.z.Final/
   TODO also upload the docs, prefixing their filenames with Errai_x.y.z.Final

1. Tweet about the release!