#!/bin/sh

#
# Creates an Errai Project skeleton
#

mvn archetype:generate \
	-DarchetypeGroupId=org.jboss.errai \
	-DarchetypeArtifactId=sandbox-archetype \
	-DarchetypeVersion=1.0-Beta1 \
	-DarchetypeRepository=http://repository.jboss.org/maven2 \
#	-Dpackage=org.jboss.errai
	
	
