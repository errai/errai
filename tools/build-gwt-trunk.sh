#!/bin/bash 
export GWT_TOOLS=../tools 
export GWT_VERSION=2.0.0-SNAPSHOT 
export LOCAL_REPO=file:///Users/hbraun/dev/prj/repository.jboss.org/maven2

svn up 
ant clean 
ant 

mvn deploy:deploy-file -DgroupId=com.google.gwt -DartifactId=gwt-user -Dversion=2.0.0-SNAPSHOT -Dpackaging=jar -Durl=$LOCAL_REPO -DgeneratePom=true -Dfile=build/lib/gwt-user.jar 

mvn deploy:deploy-file -DgroupId=com.google.gwt -DartifactId=gwt-servlet -Dversion=2.0.0-SNAPSHOT -Dpackaging=jar -Durl=$LOCAL_REPO -DgeneratePom=true -Dfile=build/lib/gwt-servlet.jar 

mvn deploy:deploy-file -DgroupId=com.google.gwt -DartifactId=gwt-dev -Dversion=2.0.0-SNAPSHOT -Dclassifier=mac -Dpackaging=jar -Durl=$LOCAL_REPO -DgeneratePom=true -Dfile=build/lib/gwt-dev-mac.jar 

mkdir temp 
tar -zxf build/dist/gwt-mac-2.0.0-SNAPSHOT.tar.gz -C temp 
cd temp/gwt-mac-2.0.0-SNAPSHOT 
zip -0 gwt-mac-2.0.0-SNAPSHOT.zip lib*.jnilib 
cd ../.. 

mvn deploy:deploy-file -DgroupId=com.google.gwt -DartifactId=gwt-dev -Dversion=2.0.0-SNAPSHOT -Dclassifier=mac-libs -Dpackaging=zip -Durl=$LOCAL_REPO -DgeneratePom=true -Dfile=temp/gwt-mac-2.0.0-SNAPSHOT/gwt-mac-2.0.0-SNAPSHOT.zip 