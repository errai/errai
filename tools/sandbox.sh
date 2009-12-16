#!/bin/sh

#
# Creates an Errai Project skeleton
#

mvn archetype:generate \
	-DarchetypeGroupId=org.jboss.errai \
	-DarchetypeArtifactId=sandbox-archetype \
	-DarchetypeVersion=1.0-SNAPSHOT \
	-DarchetypeRepository=http://snapshots.jboss.org/maven2
	
	
