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
//                 def json = readJSON text: sh(script: 'curl -u admin:admin http://localhost:9000/api/qualitygates/project_status?projectKey=my_project', returnStdout: true)
//                     if (json.projectStatus.status == "ERROR") {
//                         error("Quality Gate failed")
//                     }
//
//                 if ("${currentBuild.result}" == "FAILURE") {
//                     error("Quality Gate failed")
//                 }
            }
        }

        stage('Quality Gate') {
            steps {
            //  waitForQualityGate abortPipeline: true
                // Nếu sonar cho ra kết quả fail thì build sẽ fail
                timeout(time: 10, unit: 'MINUTES') {
                    script {
                        waitForQualityGate abortPipeline: true
                        def json = sh(script: 'curl -s -u admin:admin http://10.79.60.7:9000/api/qualitygates/project_status?projectKey=SpringBootApplication', returnStdout: true)
                        def result = readJSON text: json
                        if (result.projectStatus.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${result.projectStatus.status}"
                        }
//                         def qg = waitForQualityGate()
//                         if (qg.status != 'OK') {
//                             error "Pipeline aborted due to quality gate failure: ${qg.status}"
//                         }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
//                     withDockerRegistry(credentialsId: 'personal-dockerhub', url: 'https://registry.hub.docker.com/') {
                    withDockerRegistry(credentialsId: 'dockerhub-resdii', url: 'http://10.79.60.7:8010/') {
                        sh 'docker build -t $DOCKER_REGISTRY:${BUILD_NUMBER} .'
                        sh 'docker push $DOCKER_REGISTRY:${BUILD_NUMBER}'
                        // dockerImage = docker.build("10.79.60.7:8010/ci-cd-test:$BUILD_NUMBER .")
                    }
                }
            }
        }

        // stage('Deploy') {
        //     steps {
        //         script {
//                     withDockerRegistry(credentialsId: 'dockerhub-resdii', url: 'http://10.79.60.7:8010/') {
//                         sh '''
//                         docker pull $DOCKER_REGISTRY:${BUILD_NUMBER}
//                         docker stop ci-cd-test || true
//                         docker rm ci-cd-test || true
//                         docker run -d --name ci-cd-test -p 8080:8080 $DOCKER_REGISTRY:${BUILD_NUMBER}
//                         '''
//                     }
        //         }
        //     }
        // }
    }
    // Gửi email thông báo kết quả build trong trường hợp build fail
    post {
        failure {
            email text(
                subject: "Jenkins Pipeline Failure: ${env.JOB_NAME} [${env.BUILD_NUMBER}]",
//                 subject: "Build failed: ${currentBuild.fullDisplayName}",
                body: """<p>Build failed in Jenkins Pipeline:</p>
                    <p>Project: ${env.JOB_NAME}</p>
                    <p>Build Number: ${env.BUILD_NUMBER}</p>
                    <p>Cause: ${currentBuild.description}</p>""",
                recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'DevelopersRecipientProvider']]
            )
        }
    }
}
