pipeline {
    agent any
//     tools {
//             jdk 'JDK17'
//     }

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub_id')
    }
    stages {
        stage('Checkout SCM') {
            steps {
                // Checkout code from SCM (e.g., Git)
                    checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/master']],
                    userRemoteConfigs: [[url: 'https://github.com/NTS-Github17/JenkinsTest.git']]
                ])
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B -DskipTests clean package'
            }
        }

        stage('Scan') {
                steps {
                    withSonarQubeEnv(installationName: 'sonar') {
                        bat 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    dockerImage = docker.build("tiensy05/ci-cd-test:${env.BUILD_NUMBER}")
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'DOCKERHUB_CREDENTIALS') {
                        dockerImage.push()
                    }
                }
            }
        }
    }
}