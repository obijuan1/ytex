mvn clean test [-P derby]
mvn clean compile exec:java -Dexec.mainClass=${project.groupId}.AppMain -Dexec.args="[args...]"
mvn clean assembly:assembly
