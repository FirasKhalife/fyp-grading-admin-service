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
       IMAGE_NAME = 'admin-service'
    }

    stages {
        stage("init"){
            steps{
                script{
                    gv = load("script.groovy")
                }
            }
        }
        stage("increment"){
            steps{
                script{
                    gv.increment()

                }
            }
        }


        stage('Checkout') {
            steps {
                echo 'Checking out SCM'
                checkout scm
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

                        // Use the environment variable IMAGE_NAME
                        def fullImageName = "%dockerHubCredentials2_USR%/${env.IMAGE_NAME}"

                        bat "docker build -t ${fullImageName} ."
                        echo "Built Docker image ${fullImageName}"

                        // Tag the image with 'latest' and version
                        bat "docker tag ${fullImageName} ${fullImageName}:latest"
                        echo "Tagged Docker image with latest"

                        bat "docker tag ${fullImageName} ${fullImageName}:${major}.${minor}.${patch}"
                        echo "Tagged Docker image with version ${major}.${minor}.${patch}"

                        // Push the tags to the Docker Hub
                        bat "docker push ${fullImageName}:latest"
                        echo "Pushed Docker image with latest"

                        bat "docker push ${fullImageName}:${major}.${minor}.${patch}"
                        echo "Pushed Docker image with version ${major}.${minor}.${patch}"

                        // Write the version to a file
                        writeFile (file: "${env.WORKSPACE}/version.xml",
                                    text: "${major},${minor},${patch}", encoding: "UTF-8")
                        echo 'Wrote version file'
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
