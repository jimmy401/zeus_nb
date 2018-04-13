#!/bin/bash
export LINGOES_HOME=/home/zeus/zeus/lingoes
#JAVA_HOME=/usr/java/jdk1.7.0_67-cloudera/ used in yunmanman
JAVA_HOME=/usr/local/jdk/
PATH=${JAVA_HOME}/bin:${LINGOES_HOME}/bin:$PATH:.
echo "$PATH"
