@rem -------------------------------------------
@rem customize these variables to match your environment
@rem -------------------------------------------
@rem where java is installed
@rem please use the 32-bit jdk unless you know what you're doing
@set JAVA_HOME=C:\java\jdk-6u17-windows-x32

@rem where ytex is intalled (the directory this file is in)
@set YTEX_HOME=C:\java\clinicalnlp\ytex

@rem -------------------------------------------
@rem if you installed from ytex-with-dependencies.zip, 
@rem you should not have to change anything below this line
@rem -------------------------------------------

@set YTEX_LIB_SYS_HOME=%YTEX_HOME%\libs.system

@rem where ant is installed
@rem downloaded from http://ant.apache.org/bindownload.cgi
@set ANT_HOME=%YTEX_HOME%\..\apache-ant-1.8.0

@rem MS SQL server jdbc driver directory
@rem downloaded from http://www.microsoft.com/downloads/details.aspx?displaylang=en&FamilyID=a737000d-68d0-4531-b65d-da0f2a735707
@set SQLJDBC_HOME=%YTEX_LIB_SYS_HOME%\sqljdbc_3.0

@rem add JDBC dependencies
@set JDBC_CP=%SQLJDBC_HOME%\enu\sqljdbc4.jar

@rem MySQL JDBC driver 
@set JDBC_CP=%JDBC_CP%;%YTEX_LIB_SYS_HOME%\mysql-connector-java-5.1.17\mysql-connector-java-5.1.17-bin.jar

@rem Oracle JDBC driver 
@set JDBC_CP=%JDBC_CP%;%YTEX_LIB_SYS_HOME%\oracle11.2.0.1.0\ojdbc6.jar

@rem tomcat installation directory
@set CATALINA_HOME=%YTEX_HOME%\..\apache-tomcat-7.0.25

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

@rem tomcat classpath
@set TOMCAT_CP=%JDBC_CP%;%YTEX_HOME%\config\desc

