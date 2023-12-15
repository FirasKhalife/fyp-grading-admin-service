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

        stage("send email") {
            steps {
                echo 'send email'
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
}
