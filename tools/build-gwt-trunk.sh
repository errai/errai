#!/bin/bash 
export GWT_TOOLS=/Users/hbraun/dev/prj/gwt-trunk/tools/ 
export GWT_VERSION=2.0.0-6320 
export LOCAL_REPO=file:///Users/hbraun/dev/prj/repository.jboss.org/maven2
export PLATFORM=mac

svn up 
ant clean 
ant 

mvn deploy:deploy-file -DgroupId=com.google.gwt -DartifactId=gwt-user -Dversion=$GWT_VERSION -Dpackaging=jar -Durl=$LOCAL_REPO -DgeneratePom=true -Dfile=build/lib/gwt-user.jar 

mvn deploy:deploy-file -DgroupId=com.google.gwt -DartifactId=gwt-servlet -Dversion=$GWT_VERSION -Dpackaging=jar -Durl=$LOCAL_REPO -DgeneratePom=true -Dfile=build/lib/gwt-servlet.jar 

mvn deploy:deploy-file -DgroupId=com.google.gwt -DartifactId=gwt-dev-$PLATFORM -Dversion=$GWT_VERSION -Dpackaging=jar -Durl=$LOCAL_REPO -DgeneratePom=true -Dfile=build/lib/gwt-dev-$PLATFORM.jar 

mvn deploy:deploy-file -DgroupId=com.google.gwt -DartifactId=gwt-dev-oophm -Dversion=$GWT_VERSION -Dpackaging=jar -Durl=$LOCAL_REPO -DgeneratePom=true -Dfile=build/lib/gwt-dev-oophm.jar 

mvn deploy:deploy-file -DgroupId=com.google.gwt -DartifactId=gwt-soyc-vis -Dversion=$GWT_VERSION -Dpackaging=jar -Durl=$LOCAL_REPO -DgeneratePom=true -Dfile=build/lib/gwt-soyc-vis.jar 


mvn deploy:deploy-file -DgroupId=com.google.gwt -DartifactId=gwt-dev -Dversion=$GWT_VERSION -Dclassifier=$PLATFORM -Dpackaging=jar -Durl=$LOCAL_REPO -DgeneratePom=true -Dfile=build/lib/gwt-dev-$PLATFORM.jar 

mkdir temp 
tar -zxf build/dist/gwt-$PLATFORM-$GWT_VERSION.tar.gz -C temp 
cd temp/gwt-$PLATFORM-$GWT_VERSION 
zip -0 -R gwt-$PLATFORM-$GWT_VERSION.zip * 
cd ../.. 

mvn deploy:deploy-file -DgroupId=com.google.gwt -DartifactId=gwt-dev -Dversion=$GWT_VERSION -Dclassifier=$PLATFORM-libs -Dpackaging=zip -Durl=$LOCAL_REPO -DgeneratePom=true -Dfile=temp/gwt-$PLATFORM-$GWT_VERSION/gwt-mac-$GWT_VERSION.zip 
