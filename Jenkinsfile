pipeline {
    agent any
//     tools {
//             jdk 'JDK17'
//     }

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }
    environment {
//         DOCKERHUB_CREDENTIALS = credentials('tiensy05-dockerhub')
        DOCKERHUB_CREDENTIALS = credentials('dockerhub_id')
        DOCKER_REGISTRY = '10.79.60.7:8010'
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
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Scan') {
                steps {
                    withSonarQubeEnv(installationName: 'sonar') {
                        sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar'
                }
            }
        }

        stage('Login to Docker Registry') {
            steps {
                script {
                    sh 'echo $DOCKER_CREDENTIALS_PSW | docker login ${DOCKER_REGISTRY} -u $DOCKER_CREDENTIALS_USR --password-stdin'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh 'docker build -t 10.79.60.7:8010/ci-cd-test:${env.BUILD_NUMBER}'
//                     dockerImage = docker.build("10.79.60.7:8010/ci-cd-test:${env.BUILD_NUMBER}")
                }
            }
        }

//         stage('Login to Docker Hub') {
//             steps {
//                 script {
//                     bat 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
//                 }
//             }
//         }

        stage('Push Docker Image') {
            steps {
                script {
//                     bat 'docker push tiensy05/ci-cd-test:${env.BUILD_NUMBER}'
                    docker.withRegistry('10.79.60.7:8010', DOCKERHUB_CREDENTIALS) {
                        dockerImage.push()
                    }
                }
            }
        }
    }
}
