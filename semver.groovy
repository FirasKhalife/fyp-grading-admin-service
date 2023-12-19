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
    patch = patch as Integer

    env.WORKSPACE = pwd()
    def version = increment()
    def versionString = version.join(",")
    writeFile (file: "${env.WORKSPACE}/version.xml",
            text: "version: ${major},${minor},${patch}", encoding: "UTF-8")

    withCredentials([usernamePassword(credentialsId: "github-token", usernameVariable: "githubToken_USR", passwordVariable: "githubToken_PSW")]) {
        sh "git config user.email 'jenkins@gmail.com'"
        sh "git config user.name 'jenkins-server'"
        sh "git add ${env.WORKSPACE}/version.xml"
        sh "git commit -m 'ci: version updated to ${versionString}'"
        sh "git remote set-url origin https://${githubToken_USR}:${githubToken_PSW}@github.com/${GitHub_USR}/${GitHub_REPO}.git"
        sh "git push --set-upstream origin $BRANCH_NAME"
    }
}
return this
