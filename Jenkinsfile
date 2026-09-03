pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Unit Tests') {
            steps {
                dir('HelloWorldFunction') {
                    bat 'mvn clean test'
                }
            }
        }

        stage('SAM Validate') {
            steps {
                bat 'sam validate'
            }
        }

        stage('SAM Build') {
            steps {
                bat 'sam build'
            }
        }
    }
}