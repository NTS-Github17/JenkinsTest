pipeline {
    agent any
    tools {
        maven 'maven'
    }
//    triggers {
//        githubPush()
//    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        skipDefaultCheckout(true)
    }
    environment {
        REMOTE_DOCKER_HOST = "http://10.79.60.28:2375"
        IMAGE_NAME = "10.79.60.7:8010/ci-cd-test:${BUILD_NUMBER}"
        OLD_IMAGE_NAME = "10.79.60.7:8010/ci-cd-test:${BUILD_NUMBER.toInteger() - 1}"
        REGISTRY_CREDS = credentials('dockerhub-resdii')
        CONTAINER_NAME = "ci-cd-test"
    }
    
    stages {
        stage('Prepare Workspace') {
            steps {
                // Clean the workspace before starting the build
                cleanWs()
            }
        }

        stage('Checkout SCM') {
            steps {
                echo 'Checking out code...'
                checkout scmGit(
                        branches: [[name: 'dev']],
                        extensions: [cleanBeforeCheckout(deleteUntrackedNestedRepositories: true)],
                        userRemoteConfigs: [[
                                                    credentialsId: 'pat_github',
                                                    url: 'https://github.com/NTS-Github17/JenkinsTest.git'
                                            ]]
                )
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('SonarQube Analysis & Quality Gate') {
            steps {
                script {
                    withSonarQubeEnv(installationName: 'sonar') {
//                         sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=SpringBootApplication -Dsonar.sources=src -Dsonar.host.url=http://10.79.60.7:9000 -Dsonar.login=sonar"
                        sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar'
                    }
                    echo 'Checking Quality Gate...'
                    timeout(time: 10, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                withDockerRegistry(credentialsId: 'dockerhub-resdii', url: 'http://10.79.60.7:8010/') {
                    sh 'docker build -t $IMAGE_NAME .'
                    sh 'docker push $IMAGE_NAME'
                }
                sh 'docker rmi $IMAGE_NAME'
            }
        }

        stage('Deploy') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-resdii', usernameVariable: 'REGISTRY_CREDENTIALS_USR', passwordVariable: 'REGISTRY_CREDENTIALS_PSW')]) {
                        script {

                            sshagent(['vars3d-ssh-remote']) {
                                sh """ ssh -o StrictHostKeyChecking=no root@10.79.60.28 '
                                docker pull $IMAGE_NAME && \\
                                docker stop ci-cd-test || true && \\
                                docker rm ci-cd-test || true && \\
                                docker rmi $OLD_IMAGE_NAME && \\
                                docker run -d --name ci-cd-test -p 8085:8080 $IMAGE_NAME '
                                """

                            }
                        }
                    }
                }
            }
        }
    }

     post {
         always {
             script {
                 echo "Cleaning up workspace..."

                 cleanWs(
                         cleanWhenNotBuilt: false,      // Không xóa workspace khi build không được thực hiện
                         deleteDirs: true,              // Xóa cả thư mục
                         disableDeferredWipeout: true,  // Không trì hoãn việc xóa
                         notFailBuild: true             // Không làm fail build nếu việc xóa gặp lỗi
                 )
             }
         }
//         failure {
//             emailext (
//                 subject: "Jenkins Pipeline Failure: ${env.JOB_NAME} [${env.BUILD_NUMBER}]",
// //                 subject: "Build failed: ${currentBuild.fullDisplayName}",
//                 body: """<p>Build failed in Jenkins Pipeline:</p>
//                     <p>Project: ${env.JOB_NAME}</p>
//                     <p>Build Number: ${env.BUILD_NUMBER}</p>
//                     <p>Cause: ${currentBuild.description}</p>""",
//                 recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'DevelopersRecipientProvider']]
//             )
//         }
     }
}