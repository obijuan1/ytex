@rem -------------------------------------------
@rem customize these variables to match your environment
@rem -------------------------------------------
@rem where java is installed
@rem please use the 32-bit jdk unless you know what you're doing
@set JAVA_HOME=C:\java\jdk-6u17-windows-x32

@rem MS SQL Server
@rem where ms sql server tools are installed, should contain bcp.exe and sqlcmd.exe
@rem if you are using ms sql server, uncomment the line below and verify the path
@rem @set MSSQL_TOOLS=C:\Program Files\Microsoft SQL Server\100\Tools\Binn

@rem MySQL
@rem where mysql is installed, should contain mysql.exe
@rem if you are using MySQL, uncomment the line below and make sure the path is correct
@rem @set MYSQL_HOME=C:\Program Files\MySQL\MySQL Server 5.1\bin

@rem Oracle
@rem if you are using Oracle, uncomment the line below and make sure the paths are correct 
@rem @set ORACLE_HOME=C:\oraclexe\app\oracle\product\10.2.0\server

@rem where ytex is intalled (the directory this file is in)
@set YTEX_HOME=C:\java\clinicalnlp\ytex

@rem -------------------------------------------
@rem if you installed from ytex-with-dependencies.zip, 
@rem you should not have to change anything below this line
@rem -------------------------------------------

@rem where ant is installed
@rem downloaded from http://ant.apache.org/bindownload.cgi
@set ANT_HOME=%YTEX_HOME%\..\apache-ant-1.8.0

@rem MS SQL server jdbc driver directory
@rem downloaded from http://www.microsoft.com/downloads/details.aspx?displaylang=en&FamilyID=a737000d-68d0-4531-b65d-da0f2a735707
@set SQLJDBC_HOME=%YTEX_HOME%\libs\sqljdbc_3.0

@rem add JDBC dependencies
@set JDBC_CP=%SQLJDBC_HOME%\enu\sqljdbc4.jar

@rem MySQL JDBC driver 
@set JDBC_CP=%JDBC_CP%;%YTEX_HOME%\libs\mysql-connector-java-5.1.9\mysql-connector-java-5.1.9-bin.jar

@rem Oracle JDBC driver 
@set JDBC_CP=%JDBC_CP%;%YTEX_HOME%\libs\oracle11.2.0.1.0\ojdbc6.jar

@rem tomcat installation directory
@set CATALINA_HOME=%YTEX_HOME%\..\apache-tomcat-6.0.26

@rem -------------------------------------------
@rem end customizations.  The following is environment-independent
@rem -------------------------------------------

@set PATH=%JAVA_HOME%\bin;%SystemRoot%;%SystemRoot%\System32;%SystemRoot%\System32\wbem;%ANT_HOME%\bin

@rem add mysql to path if defined
@if defined MYSQL_HOME set PATH=%PATH%;%MYSQL_HOME%

@rem add mssql tools and jdbc driver dll to path if defined
@if defined MSSQL_TOOLS set PATH=%PATH%;%MSSQL_TOOLS%

@rem if you are using a 64-bit jdk, adjust accordingly
@set PATH=%PATH%;%SQLJDBC_HOME%\enu\auth\x86

@rem add oracle
@if defined ORACLE_HOME set PATH=%PATH%;%ORACLE_HOME%\bin

@rem we have a tomcat configuration in this directory
@set CATALINA_BASE=%YTEX_HOME%\web

@rem external libraries are in the ytex.web directory
@set YTEX_LIB_HOME=%YTEX_HOME%\web\webapps\ytex.web\WEB-INF\lib

@set YTEX_MAVERIC_HOME=%YTEX_HOME%\maveric

@rem add ytex dependencies
@set YTEX_CP=%YTEX_LIB_HOME%\com.springsource.antlr-2.7.7.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\com.springsource.org.aopalliance-1.0.0.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\com.springsource.org.apache.commons.codec-1.3.0.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\com.springsource.org.apache.commons.collections-3.2.1.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\commons-dbcp-1.4.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\com.springsource.org.apache.commons.fileupload-1.2.0.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\com.springsource.org.apache.commons.httpclient-3.1.0.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\com.springsource.org.apache.commons.io-1.4.0.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\com.springsource.org.apache.commons.lang-2.1.0.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\com.springsource.org.apache.commons.logging-1.1.1.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\com.springsource.org.apache.commons.pool-1.5.3.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\com.springsource.org.aspectj.weaver-1.6.8.RELEASE.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\dom4j-1.6.1.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\ehcache-1.2.3.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\hibernate3.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\javassist-3.9.0.GA.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\jta-1.1.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\commons-beanutils-1.8.3.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\org.springframework.aop-3.0.2.RELEASE.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\org.springframework.asm-3.0.2.RELEASE.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\org.springframework.aspects.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\org.springframework.beans-3.0.2.RELEASE.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\org.springframework.context-3.0.2.RELEASE.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\org.springframework.context.support-3.0.2.RELEASE.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\org.springframework.core-3.0.2.RELEASE.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\org.springframework.expression-3.0.2.RELEASE.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\org.springframework.jdbc-3.0.2.RELEASE.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\org.springframework.orm-3.0.2.RELEASE.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\org.springframework.transaction-3.0.2.RELEASE.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\slf4j-api-1.5.8.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\slf4j-jcl-1.5.8.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\jchronic-0.2.3.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\weka.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\ytex.model.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_LIB_HOME%\ytex.kernel.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_HOME%\lib\ytex.negex.jar
@set YTEX_CP=%YTEX_CP%;%YTEX_HOME%\lib\ytex.uima.jar

@rem tomcat classpath
@set TOMCAT_CP=%JDBC_CP%;%YTEX_HOME%\config\desc

@rem add configuration dependencies

@rem add maveric arc and its dependencies
@rem add patches before the rest of the maveric/ctakes libraries
@set ARC_CP=%TOMCAT_CP%
@set ARC_CP=%ARC_CP%;%YTEX_HOME%\lib\ctakes-patches.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\dest
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\resources
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\log4j-1.2.14.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\lvg2010dist.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\opennlp-tools-1.4.0.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\ext\mavericPipeline-1.0.0.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\jdom.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\maxent-2.5.0.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\OpenAI_FSM.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\trove.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\lucene-1.4-final.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\uima-adapter-soap.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\uima-adapter-vinci.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\uima-core.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\uima-cpe.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\uima-document-annotation.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\uima-tools.jar
@set ARC_CP=%ARC_CP%;%YTEX_MAVERIC_HOME%\lib\uima-cpe.jar

@rem TOOLS_CP is for running uima tools
@set TOOLS_CP=%YTEX_CP%;%ARC_CP%

@set TOOLS_OPTIONS=-Xmx1g -Dlog4j.configuration=file:///%YTEX_HOME%/config/desc/log4j.properties -Djava.util.logging.config.file=file:///%YTEX_HOME%/config/desc/logger.properties -DVNS_HOST=localhost
