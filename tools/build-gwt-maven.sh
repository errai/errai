#!/bin/sh

SVN_REV=`svn info | grep Revision | awk '{print $2}'`
PLUGIN_VERSION=1.2.0-$SVN_REV

echo "SVN Rev: $SVN_REV"
mvn -DskipTests=true \
	-DciVersion=$PLUGIN_VERSION \
	-DaltDeploymentRepository=repository.jboss.org::default::file:///Users/hbraun/dev/prj/repository.jboss.com/maven2 \
	clean deploy

