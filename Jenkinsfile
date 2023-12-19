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
                    gv = load("semver.groovy")
                }
            }
        }

        stage("increment"){
            steps{
                script{
                    gv.increment()
                    major = gv.major
                    minor = gv.minor
                    patch = gv.patch

                    echo "major: ${major} minor: ${minor} patch: ${patch}"
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

        stage("Update Version"){
            steps{
                script{

                    withCredentials([usernamePassword(credentialsId: "github-token", usernameVariable: "githubToken_USR", passwordVariable: "githubToken_PSW")]) {
                        sh "git checkout ${env.BRANCH_NAME}"
                        writeFile (file: "${env.WORKSPACE}/version.xml",
                                   text: "${major},${minor},${patch}", encoding: "UTF-8")
                        echo 'Wrote version file'
                        sh "git add ."
                        sh "git commit -m 'ci: version updated to ${versionString}'"
                        sh "git remote set-url origin https://${githubToken_USR}:${githubToken_PSW}@github.com/${env.GitHub_USR}/${env.GitHub_REPO}.git"
                        sh "git push --set-upstream origin ${env.BRANCH_NAME}"
                    }
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

                        bat "docker tag %dockerHubCredentials2_USR%/${env.IMAGE_NAME} %dockerHubCredentials2_USR%/${env.IMAGE_NAME}:${major}.${minor}.${patch}"
                        echo "Tagged Docker image with version ${major}.${minor}.${patch}"

                        // Push the tags to the Docker Hub
                        bat "docker push %dockerHubCredentials2_USR%/${env.IMAGE_NAME}:latest"
                        echo "Pushed Docker image with latest"

                        bat "docker push %dockerHubCredentials2_USR%/${env.IMAGE_NAME}:${major}.${minor}.${patch}"
                        echo "Pushed Docker image with version ${major}.${minor}.${patch}"
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
