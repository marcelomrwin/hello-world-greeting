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
    NEXUS_URL = "10.1.124.132"
    NEXUS_REPOSITORY = "company-project"
    NEXUS_CREDENTIAL_ID = "jenkinsldap"
    MAX_WARNING_VIOLATIONS = 30
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
            archive 'target/*.jar'
          }
        }
    }

    stage('SonarQube Static Code analysis') {
      steps {
        withMaven( maven: 'M3', mavenSettingsConfig: 'maven-settings') {

          withSonarQubeEnv('SonarQube') {
            sh "mvn clean verify sonar:sonar -Dsonar.login=admin -Dsonar.password=admin -Dsonar.verbose=true -Dsonar.projectName=${groupId}:${artifactId} -Dsonar.projectKey=${groupId}:${artifactId} -Dsonar.projectVersion=$BUILD_NUMBER";
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
    }

    stage ('Integration Test'){
      steps{
        script {
          sh 'mvn clean verify -Dsurefire.skip=true';
          junit '**/target/failsafe-reports/TEST-*.xml'
          archive 'target/*.jar'
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
          sh '''cd /home/jenkins/tomcat/bin
          ./startup.sh''';
          unstash 'binary'
          sh "cp target/*.${pom.packaging} /home/jenkins/tomcat/webapps/";
          sh 'sleep 10'
          sh 'cat /home/jenkins/tomcat/logs/*.log'
          sh '''cd /opt/jmeter/bin/
          ./jmeter.sh -n -t $WORKSPACE/src/pt/Hello_World_Test_Plan.jmx -l $WORKSPACE/test_report.jtl''';
          step([$class: 'ArtifactArchiver', artifacts: '**/*.jtl'])
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
}
