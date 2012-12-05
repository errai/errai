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

      ```
         % cd reference
         % rm src/main/docbook/en/chapter-*
         % cp $newdocs/*.xml src/main/docbook/en/
         % mvn clean xml:transform
         % cp target/generated-resources/xml/xslt/en/*.xml src/main/docbook/en/
         % git add src
      ```
  * Edit the version numbers in Book_Info.xml to reflect release version
  * Don't upload to JBoss FTP server! The release upload script will do this later.

1. Export docbook from confluence for /quickstart
  * https://docs.jboss.org/author/spaces/jboss_docbook_tools/exportandpostprocessConfigure.action?spaceKey=ERRAI&pageId=5833096
     (Single book output, no static files)
  * Copy and commit the docbook files (only chapter*) in /quickstart. Verify there is only one chapter of
     each name and one of each number (there will be old cruft left over if any chapters
     were renamed or renumbered)

     ```
        % cd quickstart
        % rm src/main/docbook/en/chapter-*
        % cp $newdocs/chapter-* src/main/docbook/en/
        % git add src
     ```
      
  * Don't upload to JBoss FTP server! The release upload script will do this later.

1. Update quickstart docs to reflect the new version number

1. Ask Maven to update the version number in all the pom.xml files:
   
        % cd $errai_root_dir
        % mvn versions:set -DnewVersion=x.y.z.Final
   
   Afterward, verify that all subprojects reference the new parent pom's version:
   
        % find . -name pom.xml | xargs grep x.y.z | grep SNAP
       
   (if any are out of sync with the parent version, Maven will not have updated them)

1. Build and package the release. These are the bits that will be uploaded to nexus.
   Expect this to take about 4 minutes, depending on network speed.

        % mvn clean deploy -Dmaven.test.skip=true -Dgwt.compiler.skip=true

1. Publish new quickstart archetypes to Nexus repo (both snapshots and released version)

        % cd $somewhere/archetypes
        % mvn versions:set -DnewVersion=x.y.z.Final
    
   Afterward, verify that all subprojects reference the new parent pom's version:

        % find . -name pom.xml | xargs grep x.y.z | grep SNAP

   Then publish the archetypes to the repository:
   
        % mvn clean deploy    
  * Note that the kitschensink archetype is tested automatically. For the test to work,
     AS7 has to be running.
  * Now test the archetypes you just installed (use instructions from quickstart guides)
  * Check generated app's pom.xml for correct version
 
    ```
    % mvn gwt:run
    ```

1. Create the a-la-carte binary Errai distribution and docs

        % mvn install -Pdistro -Dmaven.test.skip=true -Dgwt.compiler.skip=true

1. Upload the docs and the distro zipfile

        % cd dist
        % scripts/upload_binaries.sh ${version}

1. Tag and push the release to github (DO THIS FOR BOTH ERRAI AND ITS ARCHETYPES):

        % git commit a -m "Updated to new version x.y.z"
        % git tag x.y.z.Final
    
  reset all versions to x.y.z+1-SNAPSHOT and commit
  
        % git push origin /branch/
        % git push origin --tags
        % git push upstream /branch/
        % git push upstream --tags

1. Browse to nexus (https://repository.jboss.org/nexus/index.html)
  * Find the corresponding staging repository (Sort by repository name)
  * Select it and click Close
  * Select it again and click Release
  * Browse to https://repository.jboss.org/nexus/content/groups/public/org/jboss/errai/ and verify that 
     the artifacts are present

1. Update http://www.jboss.org/errai/Documentation to provide the download links for
   the generated/released docs and distribution.

1. Update the announcement on the welcome page (https://www.jboss.org/author/)

1. Publish a release blog entry at http://errai-blog.blogspot.com/

1. Tweet about the release!

=== You're done! Congrats! You deserve beer! ===
