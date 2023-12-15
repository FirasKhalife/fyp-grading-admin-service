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

                    withCredentials([usernamePassword(credentialsId: 'Docker_Hub_Credentials',
                    usernameVariable: 'dockerHubCredentials_USR',
                    passwordVariable: 'dockerHubCredentials_PSW')]) {

                        bat 'docker login -u ${dockerHubCredentials_USR} -p ${dockerHubCredentials_PSW}'
                        echo 'Logged in to Docker Hub'

                        bat "docker build -t ${dockerHubCredentials_USR}/admin-service-with-jenkins ."
                        echo 'Built Docker image'

                        bat "docker tag ${dockerHubCredentials_USR}/admin-service-with-jenkins ${dockerHubCredentials_USR}/admin-service-with-jenkins:latest"
                        echo 'Tagged Docker image'

                        bat "docker push ${dockerHubCredentials_USR}/admin-service-with-jenkins:latest"
                        echo 'Pushed Docker image'
                    }
                }
            }
        }
    }
    post {
        always {
            emailext {
                subject = "Pipeline Status: ${currentBuild.result}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
                body = """
                        <html>
                            <body>
                                <p>Build Status: ${currentBuild.result}</p>
                                <p>Build Number: ${currentBuild.number}</p>
                                <p>Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
                                <p>Check console output at <a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>
                            </body>
                        </html>""",
                to = 'gaellesaid5@gmail.com',
                from = 'gaellesaid65@gmail.com',
                replyTo = 'gaellesaid65@gmail.com',
                mimeType = 'text/html',
                recipientProviders = [[$class: 'DevelopersRecipientProvider']]
            }
        }
    }
}
