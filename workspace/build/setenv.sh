#-------------------------------------------
#customize these variables to match your environment
#-------------------------------------------
#where java is installed
#please use the 32-bit jdk unless you know what you're doing
JAVA_HOME=${HOME}/java/jdk1.6.0_21-x64

#MS SQL Server
#where ms sql server tools are installed, should contain bcp.exe and sqlcmd.exe
#if you are using ms sql server, uncomment the line below and verify the path
#MSSQL_TOOLS=C:/Program Files/Microsoft SQL Server/100/Tools/Binn

#MySQL
#where mysql is installed, should contain mysql.exe
#if you are using MySQL, uncomment the line below and make sure the path is correct
MYSQL_HOME=/usr/bin

#Oracle
#if you are using Oracle, uncomment the line below and make sure the paths are correct 
#ORACLE_HOME=C:/oraclexe/app/oracle/product/10.2.0/server

#where ytex is intalled (the directory this file is in)
YTEX_HOME=${HOME}/java/clinicalnlp/ytex

#-------------------------------------------
#if you installed from ytex-with-dependencies.zip, 
#you should not have to change anything below this line
#-------------------------------------------

#where ant is installed
#downloaded from http://ant.apache.org/bindownload.cgi
ANT_HOME=$YTEX_HOME/../apache-ant-1.8.0

#MS SQL server jdbc driver directory
#downloaded from http://www.microsoft.com/downloads/details.aspx?displaylang=en&FamilyID=a737000d-68d0-4531-b65d-da0f2a735707
SQLJDBC_HOME=$YTEX_HOME/../sqljdbc_3.0

#add JDBC dependencies
JDBC_CP=$SQLJDBC_HOME/enu/sqljdbc4.jar

#MySQL JDBC driver 
JDBC_CP=$JDBC_CP:$YTEX_HOME/../mysql-connector-java-5.1.9/mysql-connector-java-5.1.9-bin.jar

#Oracle JDBC driver 
JDBC_CP=$JDBC_CP:$YTEX_HOME/../oracle11.2.0.1.0/ojdbc6.jar

#tomcat installation directory
CATALINA_HOME=$YTEX_HOME/../apache-tomcat-6.0.26

#-------------------------------------------
#end customizations.  The following is environment-independent
#-------------------------------------------

PATH=$JAVA_HOME/bin:$SystemRoot:$SystemRoot/System32:$SystemRoot/System32/wbem:$ANT_HOME/bin

#add mysql to path if defined
@if defined MYSQL_HOME set PATH=$PATH:$MYSQL_HOME%

#add mssql tools and jdbc driver dll to path if defined
@if defined MSSQL_TOOLS set PATH=$PATH:$MSSQL_TOOLS%

#if you are using a 64-bit jdk, adjust accordingly
PATH=$PATH:$SQLJDBC_HOME/enu/auth/x86

#add oracle
@if defined ORACLE_HOME set PATH=$PATH:$ORACLE_HOME/bin

#we have a tomcat configuration in this directory
CATALINA_BASE=$YTEX_HOME/web

#external libraries are in the ytex.web directory
YTEX_LIB_HOME=$YTEX_HOME/web/webapps/ytex.web/WEB-INF/lib

YTEX_MAVERIC_HOME=$YTEX_HOME/maveric

#add ytex dependencies
YTEX_CP=$YTEX_LIB_HOME/com.springsource.antlr-2.7.7.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/com.springsource.org.aopalliance-1.0.0.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/com.springsource.org.apache.commons.codec-1.3.0.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/com.springsource.org.apache.commons.collections-3.2.1.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/commons-dbcp-1.4.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/com.springsource.org.apache.commons.fileupload-1.2.0.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/com.springsource.org.apache.commons.httpclient-3.1.0.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/com.springsource.org.apache.commons.io-1.4.0.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/com.springsource.org.apache.commons.lang-2.1.0.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/com.springsource.org.apache.commons.logging-1.1.1.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/com.springsource.org.apache.commons.pool-1.5.3.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/com.springsource.org.aspectj.weaver-1.6.8.RELEASE.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/dom4j-1.6.1.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/ehcache-1.2.3.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/hibernate3.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/javassist-3.9.0.GA.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/jta-1.1.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/commons-beanutils-1.8.3.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/org.springframework.aop-3.0.2.RELEASE.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/org.springframework.asm-3.0.2.RELEASE.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/org.springframework.aspects.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/org.springframework.beans-3.0.2.RELEASE.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/org.springframework.context-3.0.2.RELEASE.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/org.springframework.context.support-3.0.2.RELEASE.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/org.springframework.core-3.0.2.RELEASE.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/org.springframework.expression-3.0.2.RELEASE.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/org.springframework.jdbc-3.0.2.RELEASE.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/org.springframework.orm-3.0.2.RELEASE.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/org.springframework.transaction-3.0.2.RELEASE.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/slf4j-api-1.5.8.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/slf4j-jcl-1.5.8.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/jchronic-0.2.3.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/weka.jar
YTEX_CP=$YTEX_CP:$YTEX_LIB_HOME/ytex.model.jar
YTEX_CP=$YTEX_CP:$YTEX_HOME/lib/ytex.negex.jar
YTEX_CP=$YTEX_CP:$YTEX_HOME/lib/ytex.uima.jar

#tomcat classpath
TOMCAT_CP=$JDBC_CP:$YTEX_HOME/config/desc

#add configuration dependencies

#add maveric arc and its dependencies
#add patches before the rest of the maveric/ctakes libraries
ARC_CP=$TOMCAT_CP%
ARC_CP=$ARC_CP:$YTEX_HOME/lib/ctakes-patches.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/dest
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/resources
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/log4j-1.2.14.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/lvg2010dist.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/opennlp-tools-1.4.0.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/ext/mavericPipeline-1.0.0.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/jdom.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/maxent-2.5.0.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/OpenAI_FSM.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/trove.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/lucene-1.4-final.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/uima-adapter-soap.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/uima-adapter-vinci.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/uima-core.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/uima-cpe.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/uima-document-annotation.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/uima-tools.jar
ARC_CP=$ARC_CP:$YTEX_MAVERIC_HOME/lib/uima-cpe.jar

#TOOLS_CP is for running uima tools
TOOLS_CP=$YTEX_CP:$ARC_CP

TOOLS_OPTIONS=-Xmx1g -Dlog4j.configuration=file:///$YTEX_HOME/config/desc/log4j.properties -Djava.util.logging.config.file=file:///$YTEX_HOME/config/desc/logger.properties -DVNS_HOST=localhost
