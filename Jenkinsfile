pipeline {
  
  agent { label 'ec2' }
  
  stages {
  
    stage('build') {
      steps {
        sh './gradlew build'
      }
    }
  
  }

  post {
  		always {

  		}
  }
}
