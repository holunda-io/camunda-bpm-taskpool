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

def isTagged() {
  expression {
    return !env.GIT_TAG?.isEmpty()
  }
}


node {

  env.JAVA_HOME = tool 'jdk-8-oracle'
  env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
  def err = null
  def mvnOpts = "-V -U --batch-mode -T4"
  currentBuild.result = "SUCCESS"

  timestamps {

    try {
        stage('Checkout project') {
            checkout scm
            sh "chmod 755 ./mvnw"
            env.GIT_TAG = sh(returnStdout: true, script: "git tag --contains").trim()
        }

        stage('Build') {
            sh "./mvnw clean verify ${mvnOpts}"
        }

        stage('I-Test') {
            sh "./mvnw integration-test -Pitest -DskipFrontend ${mvnOpts}"

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
          stage('Release') {

            if (isTagged()) {
              withCredentials([string(credentialsId: 'holunda-io-gpg-secret-keys', variable: 'GPG_SECRET_KEYS'),
                               string(credentialsId: 'holunda-io-gpg-ownertrust', variable: 'GPG_OWNERTRUST'),
                               string(credentialsId: 'holunda-io-gpg-passphrase', variable: 'GPG_PASSPHRASE'),
                               string(credentialsId: 'holunda-io-gpg-keyname', variable: 'GPG_KEYNAME')]) {
                echo "Releasing version ${env.GIT_TAG} to maven-central"
                sh '''
                  echo "Importing secret key"
                  echo $GPG_SECRET_KEYS | base64 --decode | gpg --import --batch --yes
                  echo "Importing ownertrust"
                  echo $GPG_OWNERTRUST | base64 --decode | gpg --import-ownertrust --batch --yes
                  ./mvnw deploy -Prelease -DskipNodeBuild=true -DskipTests=true
                '''
              }
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
