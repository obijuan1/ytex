@rem wrapper to run weka
@call ..\..\setenv.cmd
@set CLASSPATH=%YTEX_LIB_HOME%\weka.jar
javaw -Xmx1g weka.gui.GUIChooser
