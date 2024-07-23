pipeline {
    agent any
//     tools {
//             jdk 'JDK17'
//     }

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }
    environment {
        DOCKER_REGISTRY = "10.79.60.7:8010/ci-cd-test"
//         DOCKERHUB_CREDENTIALS = 'dockerhub_id'
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

//         stage('Build Docker Image') {
//             steps {
//                 script {
//                 withDockerRegistry(credentialsId: 'personal-dockerhub', url: 'https://registry.hub.docker.com/') {
//                     sh 'docker build -t 10.79.60.7:8010/ci-cd-test:${BUILD_NUMBER} .'
//                     // dockerImage = docker.build("10.79.60.7:8010/ci-cd-test:$BUILD_NUMBER .")
//                     }
//                 }
//             }
//         }

        stage('Build Docker Image') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'dockerhub-resdii', url: 'http://10.79.60.7:8010/') {
                        sh 'docker build -t $DOCKER_REGISTRY:${BUILD_NUMBER} .'
                        sh 'docker push $DOCKER_REGISTRY:${BUILD_NUMBER}'
                        // dockerImage = docker.build("10.79.60.7:8010/ci-cd-test:$BUILD_NUMBER .")
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'dockerhub-resdii', url: 'http://10.79.60.7:8010/') {
                        sh '''
                        docker pull $DOCKER_REGISTRY:${BUILD_NUMBER}
                        docker stop ci-cd-test || true
                        docker rm ci-cd-test || true
                        docker run -d --name ci-cd-test -p 8080:8080 $DOCKER_REGISTRY:${BUILD_NUMBER}
                        '''
                    }
                }
            }
        }
    }
}
