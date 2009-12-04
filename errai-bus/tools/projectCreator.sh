#!/bin/bash

#
# Creates an ErraiBus Project skeleton
#

mvn archetype:generate -DarchetypeGroupId=org.jboss.errai.bus -DarchetypeArtifactId=bus-starter-archetype -DarchetypeVersion=1.1-SNAPSHOT -DarchetypeRepository=http://snapshots.jboss.org/maven2
