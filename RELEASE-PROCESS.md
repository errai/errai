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


Release Steps
-------------

1. Publish new quickstart archetypes to Nexus repo (both snapshots and released version)
   (TODO: insert exact command here)

1. Export docbook from confluence for /reference
   * https://docs.jboss.org/author/spaces/jboss_docbook_tools/exportandpostprocessConfigure.action?spaceKey=ERRAI&pageId=5833085 (Single book ouput, no static files)
   * Remove redundant chapters from the Reference_Guide.xml
   * Delete xml files for those chapters
   * Copy and commit the docbook files in /reference
   * UPLOAD SINGLE_HTML OUTPUT

1. Update quickstart docs to reflect the new version number

1. Export docbook from confluence for /quickstart
   * https://docs.jboss.org/author/spaces/jboss_docbook_tools/exportandpostprocessConfigure.action?spaceKey=ERRAI&pageId=5833096 (Single book ouput, no static files)
   * Remove redundant chapters from the Quickstart_Guide.xml
   * Delete xml files for those chapters
   * Copy and commit the docbook files in /quickstart
   * UPLOAD SINGLE_HTML OUTPUT

1. Update http://www.jboss.org/errai/Documentation to provide the download links for the generated/released docs