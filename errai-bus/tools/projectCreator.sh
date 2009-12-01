#!/bin/bash

#
# Creates an ErraiBus Project skeleton
#

mvn archetype:generate \
	-DarchetypeGroupId=org.jboss.errai.bus \
	-DarchetypeArtifactId=helloworld-archetype \
	-DarchetypeVersion=1.0-SNAPSHOT \
	-DarchetypeRepository=http://snapshots.jboss.org/maven2 \
