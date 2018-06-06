pipeline {
  agent {
    node {
      label 'ec2'
    }

  }
  stages {
    stage('build') {
      when {
        branch 'master'
      }
      steps {
        checkout(scm: scm, poll: true, changelog: true)
        sh './gradlew clean assemble'
      }
    }
  }
  post {
    always {
      echo 'TODO: run publish script to Glip'

    }

  }
}