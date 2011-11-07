#!/bin/sh

# this script sets environment variables needed to run ytex
# other ytex scripts depend on this script being called first

################################
# customize these variables to match your environment
################################

# where the JDK is installed
JAVA_HOME=/usr/lib/jvm/java-6-openjdk
export JAVA_HOME

# where ytex is installed
YTEX_HOME=${HOME}/clinicalnlp/ytex
export YTEX_HOME

# where ant is installed - if you installed from ytex-with-dependencies.zip
# there is no need to change this
ANT_HOME=${YTEX_HOME}/../apache-ant-1.8.0
export ANT_HOME

# PATH variable
# make sure mysql or oracle binaries are in this path
# add java and ant to the front of the path
PATH=${ANT_HOME}/bin:${JAVA_HOME}/bin:${PATH}:
export PATH

################################
# if you installed from ytex-with-dependencies.zip,
# you should not have to change anything below this line
################################


CATALINA_HOME=${YTEX_HOME}/../apache-tomcat-6.0.20
export CATALINA_HOME

# where mysql binary is located - should not need change
MYSQL_HOME=/usr/bin
export MYSQL_HOME

# we have a tomcat configuration in this directory
CATALINA_BASE=${YTEX_HOME}/web
export CATALINA_BASE

# ytex libraries and jdbc drivers
YTEX_LIB_SYS_HOME=${YTEX_LIB_SYS_HOME}/libs.system

# jdbc driver classpath
JDBC_CP=${YTEX_LIB_SYS_HOME}/mysql-connector-java-5.1.17/mysql-connector-java-5.1.17-bin.jar
JDBC_CP=${JDBC_CP}:${YTEX_LIB_SYS_HOME}/sqljdbc_3.0/enu/sqljdbc4.jar
JDBC_CP=${JDBC_CP}:${YTEX_LIB_SYS_HOME}/oracle11.2.0.1.0/ojdbc6.jar

# tomcat classpath
TOMCAT_CP=${JDBC_CP}:${YTEX_HOME}/config/desc
export TOMCAT_CP
