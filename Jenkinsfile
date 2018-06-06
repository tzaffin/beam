pipeline {
  agent {
  	label 'jenkins-slave'
  }
  stages {
    stage('build') {
      steps {
        sh './gradlew assemble'
      }
    }
  }
}
