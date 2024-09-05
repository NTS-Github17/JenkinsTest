boolean sonarQubeAnalysisDone = false

pipeline {
    agent any
    tools {
        maven 'maven'
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        skipDefaultCheckout(true)
    }
    environment {
        REPO_CREDENTIALS = credentials('pat_github')
        SONARQUBE_AUTH_TOKEN = credentials('sonarqube-auth-token')
        GITHUB_TOKEN = credentials('github-token')
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
                script {
                    echo 'Checking out PR branch...abcd'
                    checkout scmGit(
                            branches: [[name: "origin/pr/*"]],
                            extensions: [cleanBeforeCheckout(deleteUntrackedNestedRepositories: true)],
                            userRemoteConfigs: [[
                                                        credentialsId: 'pat_github',
                                                        name: '',
                                                        refspec: '+refs/pull/*:refs/remotes/origin/pr/*',
                                                        url: 'https://github.com/NTS-Github17/JenkinsTest.git'
                                                ]]
                    )
                }
            }
        }

        stage('SonarQube Analysis & Quality Gate') {
            steps {
                script {
                    withSonarQubeEnv(installationName: 'sonar') {
                        sh 'mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar'
                    }
                    echo 'Checking Quality Gate...'
                    sonarQubeAnalysisDone = true
                    timeout(time: 10, unit: 'MINUTES') {
                        def qg = waitForQualityGate()

                        echo "Quality Gate: ${qg}"

                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    sh 'mvn -B -DskipTests clean package'
                }
            }
        }

        stage('Which Java?') {
            steps {
                sh 'java --version'
            }
        }
    }

    post {
        always {
            script {
                if (sonarQubeAnalysisDone) {
                    echo 'Sending email notification...'
                    def qg = waitForQualityGate()
                    emailext(
                            subject: "Jenkins Pipeline Failure: ${env.JOB_NAME} [${env.BUILD_NUMBER}]",
                            // Lấy kết quả từ SonarQube và gửi qua email khi sonarqube fail hoặc không pass quality gate
                            body: """<p>Project: ${env.JOB_NAME}</p>
                            <p>Build Number: ${env.BUILD_NUMBER}</p>
                            <p>Quality gate status: ${qg.status}</p>
                            <p>Cause: ${currentBuild.description}</p>
                            <p>More details at: <a href="http://10.79.60.7:9000/dashboard?id=com.resdii.vars3d:vars-3d-ms-core">SonarQube</a></p>
                            """,
                            recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'DevelopersRecipientProvider']]
                    )
                } else {
                    echo "SonarQube analysis was not performed, skipping quality gate check."
                }

                echo "Cleaning up workspace..."

                cleanWs(
                        cleanWhenNotBuilt: false,      // Không xóa workspace khi build không được thực hiện
                        deleteDirs: true,              // Xóa cả thư mục
                        disableDeferredWipeout: true,  // Không trì hoãn việc xóa
                        notFailBuild: true             // Không làm fail build nếu việc xóa gặp lỗi
                )
            }
        }
    }
}