def gv
def matcher
def nextIncrementVersion
def major
def minor
def patch

pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK'
    }

    environment {
       IMAGE_NAME = 'fyp-grading-admin-service'
       BRANCH_NAME = 'admin-service-pipeline'
       GitHub_REPO = 'fyp-grading-admin-service'
       GitHub_USR = 'FirasKhalife'
       VERSION = readMavenPom().getVersion()
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out SCM'
                checkout scm
            }
        }

        stage("init"){
            steps{
                script{
                    env.WORKSPACE = pwd()
                    echo "filePath: ${filePath}"

                    gv = load("${env.WORKSPACE}/semver.groovy")
                    echo "semver.groovy loaded, ${gv}"
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Building'
                bat 'mvn clean install'
            }
        }

        stage('Test') {
            steps {
                echo 'Testing'
                bat 'mvn test'
            }
            post{
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage("increment version"){
            steps{
                script{
                    env.VERSION = gv.extractVersionFromPom()
                    echo "Version : gv.extractVersionFromPom()"
                    echo "VERSION: ${env.VERSION}"
                }
            }
        }

        stage("increment version2"){
            steps{
                script{
                    matcher = gv.matcher(VERSION)
                    nextIncrementVersion = gv.nextIncrementVersion(matcher)
                    major = gv.major(matcher)
                    minor = gv.minor(matcher)
                    patch = gv.patch(matcher)
                    echo "nextIncrementVersion: ${nextIncrementVersion}"
                    echo "major: ${major}"
                    echo "minor: ${minor}"
                    echo "patch: ${patch}"
                }
            }
        }


        stage('Build and Push Docker Image') {
            steps {
                echo 'Building and pushing Docker image'
                expression {
                    env.BRANCH_NAME == 'add_admin_pipeline'
                }
            }
            post {
                success {
                    echo 'Build and push Docker image successful'

                    withCredentials([usernamePassword(credentialsId: 'Docker_Hub_Credentials2',
                    usernameVariable: 'dockerHubCredentials2_USR',
                    passwordVariable: 'dockerHubCredentials2_PSW')]) {

                        bat 'docker login -u %dockerHubCredentials2_USR% -p %dockerHubCredentials2_PSW%'
                        echo 'Logged in to Docker Hub'

                        bat "docker build -t %dockerHubCredentials2_USR%/${env.IMAGE_NAME} ."
                        echo "Built Docker image %dockerHubCredentials2_USR%/${env.IMAGE_NAME}"

                        // Tag the image with 'latest' and version
                        bat "docker tag %dockerHubCredentials2_USR%/${env.IMAGE_NAME} %dockerHubCredentials2_USR%/${env.IMAGE_NAME}:latest"
                        echo "Tagged Docker image with latest"

                        bat "docker tag %dockerHubCredentials2_USR%/${env.IMAGE_NAME} %dockerHubCredentials2_USR%/${env.IMAGE_NAME}:${env.VERSION}"
                        echo "Tagged Docker image with version ${env.VERSION}"

                        // Push the tags to the Docker Hub
                        bat "docker push %dockerHubCredentials2_USR%/${env.IMAGE_NAME}:latest"
                        echo "Pushed Docker image with latest"

                        bat "docker push %dockerHubCredentials2_USR%/${env.IMAGE_NAME}:${env.VERSION}"
                        echo "Pushed Docker image with version ${env.VERSION}"
                    }
                }
            }
        }

    }
    post {
        always {
            emailext (
                subject: "Jenkins Pipeline Notification",
                body: "Build failed, please check Jenkins logs for more details.",
                from: "gaellesaid65@gmail.com",
                to: "gaellesaid5@gmail.com",
                replyTo: "gaellesaid65@gmail.com"
            )
        }
    }
}
