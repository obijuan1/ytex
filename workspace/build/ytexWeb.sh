export JAVA_OPTS="-Xmx300m  -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -XX:MaxPermSize=128M"
mkdir $CATALINA_BASE/temp
mkdir $CATALINA_BASE/logs
${CATALINA_HOME}/bin/catalina.sh start