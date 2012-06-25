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
YTEX_HOME=${HOME}/clinicalnlp/ytex-0.7
export YTEX_HOME

# where ant is installed
ANT_HOME=${YTEX_HOME}/../apache-ant-1.8.0
export ANT_HOME

# where ctakes is/will be installed
# if you have ctakes 2.5.0 installed, set this variable to your ctakes directory
# otherwise this is the directory where the ytex installer will put ctakes 
CTAKES_HOME=${YTEX_HOME}/../cTAKES-2.5.0
export CTAKES_HOME

# where metamap is installed (optional)
#MM_HOME=/opt/public_mm
#export MM_HOME

# PATH variable
# add java and ant to the front of the path
PATH=${ANT_HOME}/bin:${JAVA_HOME}/bin:${PATH}
export PATH

################################
# if you installed from ytex-with-dependencies.zip,
# you should not have to change anything below this line
################################
CATALINA_HOME=${YTEX_HOME}/../apache-tomcat-7.0.25
export CATALINA_HOME

# we have a tomcat configuration in this directory
CATALINA_BASE=${YTEX_HOME}/web
export CATALINA_BASE

# ytex libraries and jdbc drivers
YTEX_LIB_SYS_HOME=${YTEX_HOME}/libs.system

# jdbc driver classpath
JDBC_CP=${YTEX_LIB_SYS_HOME}/mysql-connector-java-5.1.17/mysql-connector-java-5.1.17-bin.jar
JDBC_CP=${JDBC_CP}:${YTEX_LIB_SYS_HOME}/sqljdbc_3.0/sqljdbc4.jar
JDBC_CP=${JDBC_CP}:${YTEX_LIB_SYS_HOME}/oracle11.2.0.1.0/ojdbc6.jar

# tomcat classpath
TOMCAT_CP=${JDBC_CP}:${YTEX_HOME}/config/desc
export TOMCAT_CP

# metamap classpath
if [ -f ${MM_HOME}/src/uima/lib/metamap-api-uima.jar ]; then
	MM_CLASSPATH=${MM_HOME}/src/javaapi/dist/MetaMapApi.jar:${MM_HOME}/src/javaapi/dist/prologbeans.jar:${MM_HOME}/src/uima/lib/metamap-api-uima.jar:${MM_HOME}/src/uima/desc
fi

# YTEX classpath
CLASSPATH=${YTEX_LIB_SYS_HOME}/ctakes-patches.jar:${YTEX_LIB_SYS_HOME}/ytex.jar:${MM_CLASSPATH}
export CLASSPATH

JAVA_OPTS="-Xmx500m -Djava.util.logging.config.file=${YTEX_HOME}/config/desc/Logger.properties -Dlog4j.configuration=log4j.properties"
export JAVA_OPTS


