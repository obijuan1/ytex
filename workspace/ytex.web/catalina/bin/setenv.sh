# set tomcat environment variables
# this assumes that YTEX_HOME\setenv.bat has been called

# jdbc driver should be loaded in system classpath
# the only thing we need from %YTEX_HOME%\config\desc is the ytex.properties file
CLASSPATH=${TOMCAT_CP}
export CLASSPATH
CATALINA_OPTS=-Dorg.apache.el.parser.COERCE_TO_ZERO=false
export CATALINA_OPTS
