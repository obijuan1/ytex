@rem -------------------------------------------
@rem customize these variables to match your environment
@rem -------------------------------------------
@rem where java JDK is installed
@rem if you are using a 64-bit jdk and ms sql server
@rem you must change the PATH variable to load the 64-bit JDBC Driver dlls (see below)
@set JAVA_HOME=C:\java\jdk-6u17-windows-x32

@rem where ytex is intalled (the directory this file is in)
@set YTEX_HOME=C:\java\clinicalnlp\ytex

@rem -------------------------------------------
@rem if you installed from ytex-with-dependencies.zip, 
@rem you should not have to change anything below this line
@rem -------------------------------------------

@rem where ant is installed
@rem downloaded from http://ant.apache.org/bindownload.cgi
@set ANT_HOME=%YTEX_HOME%\..\apache-ant-1.8.0

@rem tomcat installation directory
@set CATALINA_HOME=%YTEX_HOME%\..\apache-tomcat-7.0.25

@rem -------------------------------------------
@rem end customizations.  The following is environment-independent
@rem -------------------------------------------

@set YTEX_LIB_SYS_HOME=%YTEX_HOME%\libs.system

@rem MS SQL server jdbc driver directory
@rem downloaded from http://www.microsoft.com/downloads/details.aspx?displaylang=en&FamilyID=a737000d-68d0-4531-b65d-da0f2a735707
@set SQLJDBC_HOME=%YTEX_LIB_SYS_HOME%\sqljdbc_3.0

@rem change %SQLJDBC_HOME%\enu\auth\x86 to %SQLJDBC_HOME%\enu\auth\x64 if using a 64 bit JDK
@set PATH=%JAVA_HOME%\bin;%SystemRoot%;%SystemRoot%\System32;%SystemRoot%\System32\wbem;%ANT_HOME%\bin;%SQLJDBC_HOME%\enu\auth\x86

@rem add JDBC dependencies
@set JDBC_CP=%SQLJDBC_HOME%\enu\sqljdbc4.jar;%YTEX_LIB_SYS_HOME%\mysql-connector-java-5.1.17\mysql-connector-java-5.1.17-bin.jar;%YTEX_LIB_SYS_HOME%\oracle11.2.0.1.0\ojdbc6.jar

@rem we have a tomcat configuration in this directory
@set CATALINA_BASE=%YTEX_HOME%\web

@rem tomcat classpath
@set TOMCAT_CP=%JDBC_CP%;%YTEX_HOME%\config\desc

@rem YTEX classpath
@set CLASSPATH=%YTEX_LIB_SYS_HOME%\ctakes-patches.jar;%YTEX_LIB_SYS_HOME%\ytex.jar

