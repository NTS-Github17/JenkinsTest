boolean sonarQubeAnalysisDone = false

def check_runs

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
        REPO_NAME = "JenkinsTest"
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
                    checkPullRequestStatus()

                    echo "GhprbActualCommit: ${ghprbActualCommit}"
                    echo "GhprbPullId: ${ghprbPullId}"
                    echo "GhprbSourceBranch: ${ghprbSourceBranch}"
                    echo "GhprbTargetBranch: ${ghprbTargetBranch}"
                    echo "sha1: ${sha1}"


                    checkout scmGit(
                            branches: [[name: '${ghprbActualCommit}']],
                            extensions: [],
                            userRemoteConfigs: [[
                                                        credentialsId: 'pat_github',
                                                        name: 'origin',
                                                        refspec: '+refs/pull/*:refs/remotes/origin/pr/*',
                                                        url: 'https://github.com/Resdii-JSC/vars-3d-webapp.git'
                                                ]]
                    )
                }
            }
        }

        stage('SonarQube Analysis & Quality Gate') {
            steps {
                script {
                    checkPullRequestStatus()
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
                    checkPullRequestStatus()
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


def checkPullRequestStatus() {
    def response = httpRequest(
            url: "https://api.github.com/repos/NTS-Github17/JenkinsTest/pulls/${ghprbPullId}",
            customHeaders: [[name: 'Authorization', value: "token ${GITHUB_TOKEN}"]],
            httpMode: 'GET',
            validResponseCodes: '200'
    )

    echo "GITHUB_TOKEN: ${GITHUB_TOKEN}"
    echo "Response: ${response.content}"

    def jsonResponse = readJSON text: response.content
    def prState = jsonResponse.state
    if (prState == 'closed') {
        currentBuild.description = "PR ${ghprbPullId} is closed. Aborting build."
        error "Pull request is closed. Aborting pipeline."
    }
}