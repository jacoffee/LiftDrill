set SCRIPT_DIR=%~dp0
java -Drebel.lift_plugin=true -noverify -javaagent:E:/LiftDrill/jrebel.jar -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -Xmx1024M -Xss2M -jar "%SCRIPT_DIR%sbt-launch.jar" %*