#!/usr/bin/env groovy

def increment(){
    echo "increment..."
    env.WORKSPACE = pwd()
    matcher = readFile("${env.WORKSPACE}/version.xml")
    def list = matcher.split(",")
    major = list[0]
    minor = list[1]
    patch = list[2]
    patch = patch as Integer
    patch = patch + 1

    return [major, minor, patch]
}

def updateCommit() {
    echo "Updating version file..."

    env.WORKSPACE = pwd()
    def version = increment()
    def versionString = version.join(",")
    sh "echo 'version = ${versionString}' > version.txt"

    withCredentials([usernamePassword(credentialsId: "github-token", usernameVariable: "githubToken_USR", passwordVariable: "githubToken_PSW")]){
        sh "git add version.txt"
        sh "git commit -m 'ci: version updated in version file'"
        sh "git remote set-url origin https://${githubToken_USR}:${githubToken_PSW}@github.com/${env.GitHub_USR}/${env.GitHub_REPO}.git"
        sh "git push --set-upstream origin ${env.Pipeline_NAME}"
    }
}
return this
