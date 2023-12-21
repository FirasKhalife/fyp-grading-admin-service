#!/usr/bin/env groovy

import groovy.xml.XmlSlurper

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
    def pomFile = new File("${env.WORKSPACE}/pom.xml")

    if (!pomFile.exists()) {
        echo "pom.xml not found"
        return
    }

    def pomXml = new XmlSlurper().parse(pomFile)
    def versionString = pomXml.version.text()

    echo "Current version: ${versionString}"

    def (major, minor, patch) = versionString.tokenize('.').collect { it as Integer }

    // Increment the patch number
    patch++

    return [major, minor, patch]
}
