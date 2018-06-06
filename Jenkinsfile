pipeline {
    agent any
    stages {
        stage('build') {
            steps {
                sh './gradlew clean assemble'
            }
        }
    }
    post {
        success {
        }
        failure {
        }
    }
}