pipeline {
    
    agent { label 'ec2' }
    
    stages {
        stage('build') {
            steps {
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