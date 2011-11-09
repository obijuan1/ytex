#! /bin/sh
# we assume that . ${HOME}/ytex.profile has been called in this shell
ant -Dytex.home=${YTEX_HOME} -Dbasedir=. -buildfile ${YTEX_HOME}/build-tools.xml %*
