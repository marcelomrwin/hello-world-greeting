A hello world program to print greeting message based on time.

### Execute local mvn
mvn clean verify sonargraph:dynamic-report sonar:sonar -X -Dsonar.host.url=http://10.1.123.189:9000 -Dsonar.login=admin -Dsonar.password=admin -Dsonar.verbose=true -Dsonar.projectName=com.example:greetings -Dsonar.projectKey=com.example:greetings -Dsonar.dependencyCheck.reportPath=target/dependency-check-report.xml -Dsonar.dependencyCheck.htmlReportPath=target/dependency-check-report.html -Dcobertura.report.format=html -Dsonar.cobertura.reportPath=target/cobertura/coverage.html -Dsonar.projectVersion=131
