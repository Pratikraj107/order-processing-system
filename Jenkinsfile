pipeline {
    agent any

    environment {
        JAVA_HOME = 'C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.12.101-hotspot'
        PATH = "${JAVA_HOME}\\bin;${env.PATH}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Check Java') {
            steps {
                bat 'echo JAVA_HOME=%JAVA_HOME%'
                bat 'where java'
                bat 'java -version'
                bat 'mvn -version'
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