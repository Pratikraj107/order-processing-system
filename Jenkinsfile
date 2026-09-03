pipeline {
    agent any

    environment {
        JAVA_HOME = 'C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.12.101-hotspot'
        PATH = "${JAVA_HOME}\\bin;${env.PATH}"
    }

    options {
        skipDefaultCheckout(true)
    }

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

        stage('AWS Authentication') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-order-processing']
                ]) {
                    bat 'aws sts get-caller-identity'
                }
            }
        }

        stage('Deploy to AWS') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-order-processing']
                ]) {
                    bat 'sam deploy --no-confirm-changeset --no-fail-on-empty-changeset'
                }
            }
        }
    }
}