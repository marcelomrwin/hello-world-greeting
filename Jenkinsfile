def version = null
def artifactId = null
def groupId = null
def pom = null

pipeline{
  agent {
    label "maven"
  }

  tools {
    maven "M3"
  }

  environment {
    NEXUS_VERSION = "nexus3"
    NEXUS_PROTOCOL = "https"
    NEXUS_URL = "10.1.123.207"
    NEXUS_REPOSITORY = "company-project"
    NEXUS_CREDENTIAL_ID = "jenkinsldap"
    MAX_WARNING_VIOLATIONS = 30
  }

  options {
    // Only keep the 10 most recent builds
    buildDiscarder(logRotator(numToKeepStr:'10'))
  }

  stages{
    stage('Poll'){
        steps {
          script{
            checkout scm
            version = getVersionFromPom()
            groupId = getGroupIdFromPom()
            artifactId = getArtifactIdFromPom()
            pom = readMavenPom file: "pom.xml";
          }
        }
    }

    stage('Build & Unit test'){
        steps {
          script {
            sh 'mvn clean verify -DskipITs=true';
            junit '**/target/surefire-reports/TEST-*.xml'
          //  archive 'target/*.jar'
          }
        }
    }

    stage('SonarQube Static Code analysis') {
      steps {
        withMaven( maven: 'M3', mavenSettingsConfig: 'maven-settings',
        options: [
          artifactsPublisher(disabled: true),
          findbugsPublisher(disabled: false),
          openTasksPublisher(disabled: false),
          junitPublisher(disabled: false)
        ]) {
          withSonarQubeEnv('SonarQube') {
            sh "mvn clean verify sonargraph:dynamic-report sonar:sonar -Dsonar.login=admin -Dsonar.password=admin -Dsonar.verbose=true -Dsonar.projectName=${groupId}:${artifactId} -Dsonar.projectKey=${groupId}:${artifactId} -Dsonar.dependencyCheck.reportPath=target/dependency-check-report.xml -Dsonar.dependencyCheck.htmlReportPath=target/dependency-check-report.html -Dcobertura.report.format=html -Dsonar.cobertura.reportPath=target/cobertura/coverage.html -Dsonar.projectVersion=$BUILD_NUMBER";
          }
        }
      }
    }

    stage("Fetch Quality Gate") {
      steps{
        script{
          timeout(time: 5, unit: 'MINUTES') {
            def qg = waitForQualityGate()
            if (qg.status != 'OK') {
              error "Falha devido a má qualidade do código.\nStatus da análise: ${qg.status}"
            }
          }
        }
      }
      post {
          success {
              archiveArtifacts artifacts: '**/dependency-check-report.json', onlyIfSuccessful: true
              archiveArtifacts artifacts: '**/jacoco.exec', onlyIfSuccessful: true
              sh 'tar -czvf target/sonar.tar.gz target/sonar'
              archiveArtifacts artifacts: 'target/sonar.tar.gz', onlyIfSuccessful: true

              sh 'tar -czvf target/jacoco.tar.gz target/site/jacoco'
              archiveArtifacts artifacts: 'target/jacoco.tar.gz', onlyIfSuccessful: true

              sh 'tar -czvf target/cobertura.tar.gz target/site/cobertura'
              archiveArtifacts artifacts: 'target/cobertura.tar.gz', onlyIfSuccessful: true

              publishHTML (target: [
                allowMissing: true,
                alwaysLinkToLastBuild: false,
                keepAll: true,
                reportFiles: 'dependency-check-vulnerability.html',
                reportName: "Dependency Check Vulnerability"
              ])


          }
      }
    }

    stage ('Integration Test'){
      steps{
        script {
          sh 'mvn clean verify -Dsurefire.skip=true';
          junit '**/target/failsafe-reports/TEST-*.xml'
//          archive 'target/*.jar'
        }
      }
    }

    stage('Prepare to performance Test'){
      steps{
        stash includes: "target/*.${pom.packaging},src/pt/Hello_World_Test_Plan.jmx", name: 'binary'
      }
    }

    stage('Performance Testing'){
      agent { label "docker-performance" }
      steps {
        script {
          timeout(time: 5, unit: 'MINUTES') {
            sh '''cd /home/jenkins/tomcat/bin
            ./startup.sh''';
            unstash 'binary'
            sh "cp target/*.${pom.packaging} /home/jenkins/tomcat/webapps/"
            sh "sleep 10"
            sh 'while ! httping -qc1 http://localhost:8080/greetings-0.0.1 ; do sleep 3 ; done'
            sh '''cd /opt/jmeter/bin/
            ./jmeter.sh -n -t $WORKSPACE/src/pt/Hello_World_Test_Plan.jmx -l $WORKSPACE/test_report.jtl''';
            step([$class: 'ArtifactArchiver', artifacts: '**/*.jtl'])
            perfReport sourceDataFiles: '**/test_report.jtl', modePerformancePerTestCase: true, modeOfThreshold: true, errorFailedThreshold: 1

            if (getAvgFromJmeter() > 5){
              echo 'Avg abaixo acima de 5'
            }

            // If percent of errors is more than 10
            if (getErrorPercentFromJmeter() > 10){
              error "Falha devido percentual de erros no teste de performance."
            }

            // If avg response time for all requests is above 100ms consider approve manual build
            if (getAvgResponseTimeFromJmeter() > 100){
              timeout(time: 5, unit: 'MINUTES') {
                input "Performance acima de 100 milesegundos, prosseguir?"
              }
            }
          }
        }
      }
    }

    stage("publish to nexus") {
        steps {
            script {
                // Read POM xml file using 'readMavenPom' step , this step 'readMavenPom' is included in: https://plugins.jenkins.io/pipeline-utility-steps
                pom = readMavenPom file: "pom.xml";
                // Find built artifact under target folder
                filesByGlob = findFiles(glob: "target/*.${pom.packaging}");
                // Print some info from the artifact found
                echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
                // Extract the path from the File found
                artifactPath = filesByGlob[0].path;
                // Assign to a boolean response verifying If the artifact name exists
                artifactExists = fileExists artifactPath;
                if(artifactExists) {
                    echo "*** File: ${artifactPath}, group: ${pom.groupId}, packaging: ${pom.packaging}, version ${pom.version}";
                    nexusArtifactUploader(
                        nexusVersion: NEXUS_VERSION,
                        protocol: NEXUS_PROTOCOL,
                        nexusUrl: NEXUS_URL,
                        groupId: pom.groupId,
                        version: pom.version,
                        repository: NEXUS_REPOSITORY,
                        credentialsId: NEXUS_CREDENTIAL_ID,
                        artifacts: [
                            // Artifact generated such as .jar, .ear and .war files.
                            [artifactId: pom.artifactId,
                            classifier: '',
                            file: artifactPath,
                            type: pom.packaging],
                            // Lets upload the pom.xml file for additional information for Transitive dependencies
                            [artifactId: pom.artifactId,
                            classifier: '',
                            file: "pom.xml",
                            type: "pom"]
                        ]
                    );
                } else {
                    error "*** File: ${artifactPath}, could not be found";
                }
            }
        }
    }

  }
  post {
    always {
      echo "Send notifications for result: ${currentBuild.result}"
    }
  }
}
