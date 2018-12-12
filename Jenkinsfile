#!/groovy
properties properties: [
    [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '10', numToKeepStr: '10']],
    disableConcurrentBuilds()
]

def isMasterBranch() {
  expression {
    // use !(expr) to negate something, || for or, && for and
    // env.BRANCH_NAME =~ /^[[:digit:]]*\.[[:digit:]]*\.[[:digit:]]*/$
    return env.BRANCH_NAME.startsWith("master")
  }
}


node {

  env.JAVA_HOME = tool 'jdk-8-oracle'
  env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
  def err = null
  def mvnOpts = "-V -U --batch-mode"
  currentBuild.result = "SUCCESS"

  timestamps {

    try {
        stage('Checkout project') {
            checkout scm
            sh "chmod 755 ./mvnw"
        }

        stage('Build') {
            sh "./mvnw clean verify ${mvnOpts}"
        }

        stage('I-Test') {
            sh "./mvnw integration-test -Pitest ${mvnOpts}"

            try {
            // check if there were errors
            sh "./mvnw failsafe:verify ${mvnOpts}"
            } catch (buildError) {
            // set status to unstable, if errors found
            currentBuild.result = "UNSTABLE"
            }
        }

        stage('Code coverage') {
            sh "curl -s https://codecov.io/bash | bash -s -"
        }

        if (isMasterBranch()) {
          stage('Deploy') {
            echo "Running a deploy"
            if (buildingTag()) {
              echo "Building a tag " + env.TAG_NAME
            }
          }
        }

    } catch (caughtError) {
      err = caughtError
      currentBuild.result = "FAILURE"

    } finally {

      // collect coverage
      step([$class: 'JacocoPublisher'])

      // collect unit
      step([$class: 'JUnitResultArchiver', allowEmptyResults: true, testResults: '**/target/*-reports/TEST-*.xml'])

      cleanWs cleanWhenSuccess: false, cleanWhenUnstable: false

      /* Must re-throw exception to propagate error */
      if (err) {
        throw err
      }
    } // finally

  } // timestamps
}
