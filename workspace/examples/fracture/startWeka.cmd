@rem wrapper to run weka
@call ..\..\setenv.cmd
@set CLASSPATH=%YTEX_HOME%\web\webapps\ytex.web\WEB-INF\lib\weka-3.6.7.jar
javaw -Xmx1g weka.gui.GUIChooser
