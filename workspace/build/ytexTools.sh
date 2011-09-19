#! /bin/sh
. ${HOME}/ytex.profile
ant -Dytex.home=${YTEX_HOME} -Dbasedir=. -buildfile ${YTEX_HOME}/build-tools.xml %*