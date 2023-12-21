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

def version() {
    echo "extracting version..."

    env.WORKSPACE = pwd()
    matcher = readFile("${env.WORKSPACE}/pom.xml") =~ '<version>(.+?)</version>'
    version = matcher[0][1]

    return version
}
