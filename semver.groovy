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

    return [major, minor, patch]
}

def increment2() {
    echo "increment..."

    env.WORKSPACE = pwd()
    def pomPath = "${env.WORKSPACE}/pom.xml"

    // Read the Maven pom.xml file
    def pom = readMavenPom file: pomPath
    def versionString = pom.getVersion()

    echo "Current version: ${versionString}"

    def (major, minor, patch) = versionString.tokenize('.').collect { it as Integer }

    return [major, minor, patch]
}
