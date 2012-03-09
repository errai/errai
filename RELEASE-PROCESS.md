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

When the Maven Release Plugin goes awry, it's a tedious process to back out its
automated git tags and commits. We prefer to follow the steps manually, committing
after each step as we go.

Release Steps
-------------

1. Run the test suite. Ensure all tests pass.
   % mvn -Pintegration-test test
   
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
   * Copy and commit the docbook files (only chapter*) in /quickstart. Verify there is only one chapter of
     each name and one of each number (there will be old cruft left over if any chapters
     were renamed or renumbered)
   * Manually copy the author directory from the downloaded zip into all three docbook
     output directories (html_single, html, pdf)
   * remember to upload single_html, multi-page html, and pdf versions

1. Ask Maven to update the version number in all the pom.xml files:
   cd into the errai root directory	
   % mvn versions:set -DnewVersion=x.y.z.Final
   Afterward, verify that all subprojects reference the new parent pom's version: find . -name pom.xml | xargs grep x.y.z | grep SNAP
   (if any are out of sync with the parent version, Maven will not have updated them)

1. Build and package the release. These are the bits that will be uploaded to nexus.
   Expect this to take about 9 minutes.
   % mvn clean install

1. Upload the release to nexus:
   % mvn deploy -Dgwt.compiler.skip=true

1. Publish new quickstart archetypes to Nexus repo (both snapshots and released version)
   * % cd $somewhere/archetypes
   * % mvn versions:set -DnewVersion=x.y.z.Final
   * Afterward, verify that all subprojects reference the new parent pom's version: find . -name pom.xml | xargs grep x.y.z | grep SNAP
   * % mvn clean install
   
      !!Note that the kitschensink archetype is tested automatically. For the test to work AS7 has to be running!!

      Now test the archetypes you just installed (use instructions from quickstart guides)
   * check generated app's pom.xml for correct version
   * mvn gwt:run
   
1. % mvn deploy -Dmaven.test.skip=true

   The next step has to be done for both Errai and its archetypes!
1. Tag and push the release to github:
   * % git commit a -m "update to new version x.y.z"
   * % git tag x.y.z.Final
   * % git push origin /branch/
   * % git push origin --tags
   * % git push upstream /branch/
   * % git push upstream --tags

1. Create and upload the a-la-carte binary Errai distribution and docs
   * % mvn install -Pdistro
   * % sftp errai@filemgmt.jboss.org
   
   * sftp> cd /docs_htdocs/errai
   * sftp> mkdir x.y.z.Final
   * sftp> cd x.y.z.Final
   * sftp> mkdir errai
   * sftp> mkdir errai-cdi
   
   * sftp> cd /downloads_htdocs/errai/dist
   * sftp> mkdir x.y.z.Final

   * % scp errai-x.y.z-Final.zip errai@filemgmt.jboss.org:/downloads_htdocs/errai/dist/x.y.z.Final/
   * % scp errai-cdi-x.y.z-Final.zip errai@filemgmt.jboss.org:/downloads_htdocs/errai/dist/x.y.z.Final/

   * Errai docs:
   * % scp -rp . errai@filemgmt.jboss.org:/doc_htdocs/errai/x.y.z.Final/errai/reference
   * % scp -rp . errai@filemgmt.jboss.org:/doc_htdocs/errai/x.y.z.Final/errai/quickstart
   * Download author directory of previous release and upload it to both /reference/html/author and /reference/html_single/author

   * Errai-CDI docs:
   * % scp -rp . errai@filemgmt.jboss.org:/doc_htdocs/errai/x.y.z.Final/errai-cdi/reference
   * % scp -rp . errai@filemgmt.jboss.org:/doc_htdocs/errai/x.y.z.Final/errai-cdi/quickstart

   * rename PDFs to Errai[_CDI]_x.y.z.Final_[Reference][Quickstart]_Guide.pdf


1. Browse to nexus (https://repository.jboss.org/nexus/index.html)
   Find the corresponding staging repository (Sort by repository name)
   Select it and click Close
   Select it again and click Release
   Browse to https://repository.jboss.org/nexus/content/groups/public/org/jboss/errai/ and verify that artifact are present

1. Update http://www.jboss.org/errai/Documentation to provide the download links for
   the generated/released docs and distribution. Also update the announcement on the welcome page (https://www.jboss.org/author/)

1. Tweet about the release!

1. reset all versions of Errai and Errai-CDI to x.y.z+1-SNAPSHOT, commit and push to upstream

=== You're done! Congrats! You deserve beer! ===

TODO list:
  - fix docbook export to include images, and add language="java" attribute to programmlistings
  - configure docbook to generate proper pdf file name
  - automate ftp upload procedure
