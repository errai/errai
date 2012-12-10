#!/bin/bash

export version=$1
skip_mkdirs=""

# TODO use getopt
if [ "$2" = "--skip-mkdirs" ]; then
    skip_mkdirs=yes
fi

distfile=target/errai-$version.zip

if [ -z "$version" ]; then
    echo "FAIL: you must specify the Errai release version number as the first command line arg."
    exit 1
fi

if [ ! -f $distfile ]; then
    echo "FAIL: can't find Errai distribution zipfile $distfile."
    echo "Please run this script with a working directory of ERRAI_HOME/dist"
    exit 1
fi

set -e

if [ -z "$skip_mkdirs" ]; then

    echo "Making directories..."

    sftp -b - errai@filemgmt.jboss.org <<END
cd /docs_htdocs/errai
mkdir $version
cd $version
mkdir errai

cd /downloads_htdocs/errai/dist
mkdir $version
END

    echo "Done!"
fi

echo "Uploading binary distribution..."

scp $distfile errai@filemgmt.jboss.org:/downloads_htdocs/errai/dist/$version/

echo "Done!"

echo "Uploading documentation..."

(cd ../reference/target/docbook/publish/en/pdf && mv Reference_Guide.pdf Errai_${version}_Reference_Guide.pdf)
(cd ../quickstart/target/docbook/publish/en/pdf && mv Quickstart_Guide.pdf Errai_${version}_Quickstart_Guide.pdf)

(cd ../reference/target/docbook/publish/en  && scp -rp . errai@filemgmt.jboss.org:/docs_htdocs/errai/$version/errai/reference)
(cd ../quickstart/target/docbook/publish/en && scp -rp . errai@filemgmt.jboss.org:/docs_htdocs/errai/$version/errai/quickstart)

echo "Copying images from a previous release (yecch...)"

tmpdir=`mktemp -d tmp_author_downloaded`
(cd $tmpdir && scp -rp errai@filemgmt.jboss.org:/docs_htdocs/errai/2.2.0.CR1/errai/reference/html/author .)
(cd $tmpdir && scp -rp author errai@filemgmt.jboss.org:/docs_htdocs/errai/$version/errai/reference/html)
(cd $tmpdir && scp -rp author errai@filemgmt.jboss.org:/docs_htdocs/errai/$version/errai/reference/html_single)

rm -r $tmpdir

echo "Done!"
