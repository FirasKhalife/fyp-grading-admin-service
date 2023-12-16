pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out SCM'
                checkout scm
            }
        }

        stage('Compile') {
            steps {
                echo 'Compiling'
                bat 'mvn compile'
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

                        bat "docker build -t %dockerHubCredentials2_USR%/admin-service-with-jenkins ."
                        echo 'Built Docker image'

                        bat "docker tag %dockerHubCredentials2_USR%/admin-service-with-jenkins %dockerHubCredentials2_USR%/admin-service-with-jenkins:latest"
                        echo 'Tagged Docker image'

                        bat "docker push %dockerHubCredentials2_USR%/admin-service-with-jenkins:latest"
                        echo 'Pushed Docker image'
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
