pipeline {
    agent any

    environment {
        IMAGE_NAME = "project_management_system"
        VERSION = "${BUILD_NUMBER}"
    }

    stages {

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t $IMAGE_NAME:$VERSION .'
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                docker compose down || true
                docker compose up -d --build
                '''
            }
        }

    }
}