node('ec2') {
  properties([
    [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '10']]
  ])
  
  stage('Checkout') {
    checkout scm
  }
  
  stage('Gradle Build') {
    sh "./gradlew assemble"
  }
  
}