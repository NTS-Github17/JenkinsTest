boolean sonarQubeAnalysisDone = false

def check_runs = load '.cicd/buildGithubCheckScript.groovy'

pipeline {
    agent any
    tools {
        maven 'maven'
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }
    environment {
        REPO_CREDENTIALS = credentials('pat_github')
        SONARQUBE_AUTH_TOKEN = credentials('sonarqube-auth-token')
        GITHUB_TOKEN = credentials('github-token')
        REPO_NAME = "JenkinsTest"
    }

    stages {
        stage('Checkout SCM') {
            steps {
                script {
                    checkPullRequestStatus()
                    checkout([
                            $class           : 'GitSCM',
                            branches         : [[name: "${ghprbActualCommit}"]],
                            extensions       : [],
                            userRemoteConfigs: [[
                                                        credentialsId: 'pat_github',
                                                        name         : 'origin',
                                                        url          : 'https://github.com/NTS-Github17/JenkinsTest.git',
                                                        refspec      : "+refs/pull/${ghprbPullId}/*:refs/remotes/origin/pr/${ghprbPullId}/*"
                                                ]]
                    ])
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
                    withCredentials([string(credentialsId: 'github-app-private-key', variable: 'privateKey')]) {
                        try {
                            sh 'mvn -B -DskipTests clean package'
                            check_runs.buildGithubCheck(${REPO_NAME}, env.GIT_COMMIT, privateKey, 'success', "build")
                        } catch (Exception e) {
                            check_runs.buildGithubCheck(${REPO_NAME}, env.GIT_COMMIT, privateKey, 'failure', "build")
                            echo "Exception: ${e}"
                        }
                    }

                }
//                sh 'mvn -B -DskipTests clean package'
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