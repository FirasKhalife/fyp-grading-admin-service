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
return this