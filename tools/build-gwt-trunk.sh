#!/bin/sh

# Make sure you have Ant and Maven installed
# and execute this scriptin GWT trunk
#
# Taken from http://asgeirf.blogspot.com/2009/10/building-maven-artifacts-for-gwt-200.html
# Thanks.

if [ "x$LOCAL_REPO" == "x" ]; then
  echo "LOCAL_REPO not set!"
  echo "Please point to your svn repository checkout"
  exit
fi

SVN_REV=`svn info | grep Revision | awk '{print $2}'`
GWT_VERSION=2.0.0-$SVN_REV
ant clean
ant -Dgwt.version=$GWT_VERSION

for artifact in `ls build/lib`; do 
	artifact_packaging=${artifact##*.}
	artifact_name=${artifact%.*}
	mvn deploy:deploy-file -DgroupId=com.google.gwt \
	-DartifactId=$artifact_name \
	-Dversion=$GWT_VERSION \
	-Dpackaging=$artifact_packaging \
	-Durl=$LOCAL_REPO -DgeneratePom=true \
	-Dfile=build/lib/$artifact
done

mvn deploy:deploy-file \
	-DgroupId=com.google.gwt \
	-DartifactId=gwt \
	-Dversion=$GWT_VERSION -Dpackaging=zip \
	-Durl=$LOCAL_REPO -DgeneratePom=true \
	-Dfile=build/dist/gwt-$GWT_VERSION.zip

