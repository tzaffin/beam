pipeline {
    
    agent { label 'ec2' }
    
    stages {
        stage('build') {
        	when {
        		branch 'master'
        	}
            steps {
            	checkout scm
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