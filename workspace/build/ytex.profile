#!/bin/sh

# this script sets environment variables needed to run ytex
# other ytex scripts depend on this script being called first

################################
# customize these variables to match your environment
################################

# where the JDK is installed
JAVA_HOME=${HOME}/java/jdk1.6.0_21-x64
export JAVA_HOME

# where ytex is installed
YTEX_HOME=${HOME}/clinicalnlp/ytex
export YTEX_HOME

# add java and ant to the front of the path
PATH=${HOME}/java/apache-ant-1.8.2/bin:${JAVA_HOME}/bin:${PATH}:
export PATH

################################
# if you installed from ytex-with-dependencies.zip,
# you should not have to change anything below this line
################################

ANT_HOME=${YTEX_HOME}/../apache-ant-1.8.2
export ANT_HOME

CATALINA_HOME=${HOME}/../apache-tomcat-6.0.20
export CATALINA_HOME

# where mysql binary is located - should not need change
MYSQL_HOME=/usr/bin
export MYSQL_HOME

# we have a tomcat configuration in this directory
CATALINA_BASE=${YTEX_HOME}/web
export CATALINA_BASE

# ytex libraries and jdbc drivers
YTEX_LIB_SYS_HOME=${YTEX_LIB_SYS_HOME}/libs.system

# external libraries are in the ytex.web directory
YTEX_LIB_HOME=${YTEX_HOME}/web/webapps/ytex.web/WEB-INF/lib

YTEX_MAVERIC_HOME=${YTEX_HOME}/maveric

# jdbc driver classpath
JDBC_CP=${YTEX_LIB_SYS_HOME}/mysql-connector-java-5.1.9/mysql-connector-java-5.1.9-bin.jar
JDBC_CP=${JDBC_CP}:${YTEX_LIB_SYS_HOME}/sqljdbc_3.0/enu/sqljdbc4.jar
JDBC_CP=${JDBC_CP}:${YTEX_LIB_SYS_HOME}/oracle11.2.0.1.0/ojdbc6.jar

# ytex classpath
YTEX_CP=
# add all jars from the lib directory
for file in ${YTEX_LIB_SYS_HOME}/*.jar; do YTEX_CP=${YTEX_CP}:${file}; done
# add all jars from the WEB-INF/lib directory
for file in ${YTEX_LIB_HOME}/*.jar; do YTEX_CP=${YTEX_CP}:${file}; done

# tomcat classpath
TOMCAT_CP=${JDBC_CP}:${YTEX_HOME}/config/desc
export TOMCAT_CP

# add configuration dependencies
ARC_CP=${TOMCAT_CP}
ARC_CP=${ARC_CP}:${YTEX_MAVERIC_HOME}/dest
ARC_CP=${ARC_CP}:${YTEX_MAVERIC_HOME}/resources
ARC_CP=${ARC_CP}:${YTEX_MAVERIC_HOME}/ext/mavericPipeline-1.0.0.jar
# add all jars from the lib directory
for file in ${YTEX_MAVERIC_HOME}/lib/*.jar; do ARC_CP=${ARC_CP}:${file}; done

TOOLS_CP=${YTEX_CP}:${ARC_CP}
export TOOLS_CP

TOOLS_OPTIONS="-Xmx1g -Dlog4j.configuration=file://${YTEX_HOME}/config/desc/log4j.properties -Djava.util.logging.config.file=file://${YTEX_HOME}/config/desc/logger.properties -DVNS_HOST=localhost"
export TOOLS_OPTIONS

