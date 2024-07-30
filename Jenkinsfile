pipeline {
    agent any
    tools {
        maven 'maven'
    }
    triggers {
        githubPush()
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }

    environment {
        // DOCKER_REGISTRY = "10.79.60.7:8010/ci-cd-test"
        // REMOTE_DOCKER_HOST = "tcp://10.79.60.28:2375"
        REMOTE_DOCKER_HOST = "http://10.79.60.28:2375"
        IMAGE_NAME = "10.79.60.7:8010/ci-cd-test:${BUILD_NUMBER}"
        REGISTRY_CREDS = credentials('dockerhub-resdii')
        // CONTAINER_NAME = "ci-cd-test"
//         scannerHome = tool 'SonarQube Scanner'
//         sonarToken = credentials('sonarqube-token-id')
//         DOCKERHUB_CREDENTIALS = 'dockerhub_id'
    }

    stages {
        stage('Checkout SCM') {
            steps {
                    checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[url: 'https://github.com/NTS-Github17/JenkinsTest.git']]
                ])
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

//         stage('Quality Gate') {
//             steps {
//                 echo 'Checking Quality Gate...'
//                 // Nếu sonar cho ra kết quả fail thì build sẽ fail
//                 timeout(time: 10, unit: 'MINUTES') {
//                     script {
// //                         waitForQualityGate abortPipeline: true
// //                         def json = sh(script: 'curl -s -u admin:Resdii@168861 http://10.79.60.7:9000/api/qualitygates/project_status?projectKey=SpringBootApplication', returnStdout: true)
// //                         def result = readJSON text: json
// //                         if (result.projectStatus.status != 'OK') {
// //                             error "Pipeline aborted due to quality gate failure: ${result.projectStatus.status}"
// //                         }
//                         def qg = waitForQualityGate()
//                         if (qg.status != 'OK') {
//                             error "Pipeline aborted due to quality gate failure: ${qg.status}"
//                         }
//                     }
//                 }
//             }
//         }

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
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-resdii', usernameVariable: 'REGISTRY_CREDS_USR', passwordVariable: 'REGISTRY_CREDS_PSW')]) {
                        echo "Username: $REGISTRY_CREDS_USR"
                        echo "Password: $REGISTRY_CREDS_PSW"

                        def authConfig = '{\"username\": \"${REGISTRY_CREDS_USR}\", \"password\": \"${REGISTRY_CREDS_PSW}\", \"email\": \"nguyentiensy2k17@gmail.com\", \"serveraddress\": \"10.79.60.7:8010\"}'
                        def authBase64 = authConfig.bytes.encodeBase64().toString()
                        def dockerPull = """
                            curl --unix-socket /var/run/docker.sock \
                            -H "Content-Type: application/json" \
                            -H "X-Registry-Auth: ${authBase64}" \
                            -X POST "${REMOTE_DOCKER_HOST}/images/create?fromImage=${IMAGE_NAME}"
                        """
                        
                        sh(dockerPull)
                    }
                    // def dockerPull = """
                    //     curl --unix-socket /var/run/docker.sock \
                    //     -H "Content-Type: application/json" \
                    //     -X POST "${REMOTE_DOCKER_HOST}/images/create?fromImage=${IMAGE_NAME}"
                    // """
                    // sh(dockerPull)
                        // def dockerPull = """
                        // curl --unix-socket /var/run/docker.sock \
                        // -X POST -H "Content-Type: application/json" --data '{"fromImage": "${IMAGE_NAME}"}' ${REMOTE_DOCKER_HOST}/images/create
                        // """
                }
//                 script {
//                     sshagent(['vars3d-ssh-remote']) {
// //                         sh 'ssh -o StrictHostKeyChecking=no root@10.79.60.28 touch test-remote-server.txt'
//                         sh """ ssh -o StrictHostKeyChecking=no root@10.79.60.28 '
//                         docker pull $IMAGE_NAME && \\
//                         docker stop ci-cd-test || true && \\
//                         docker rm ci-cd-test || true && \\
//                         docker run -d --name ci-cd-test -p 8085:8080 $IMAGE_NAME && \\
//                         touch test-remote-server.txt '
//                         """
//                     }
//                 }
            }
        }
    }

    // Gửi email thông báo kết quả build trong trường hợp build fail
//     post {
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
//     }
}
