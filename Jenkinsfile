pipeline {
    
    agent { label 'ec2' }
    
    stages {
        stage('build') {
            steps {
            	checkout scm
                sh './gradlew clean assemble'
            }
        }
    }
	
	post { 
        always { 
            //publish result to glip
        }
    }
}