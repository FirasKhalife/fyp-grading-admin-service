#!/usr/bin/env groovy

def increment(){
    echo "increment..."

    env.WORKSPACE = pwd()
    matcher = readFile("${env.WORKSPACE}/pom.xml") =~ /<version>(.+?)<\/version>/


    def list = matcher.split(".")
    major = list[0]
    minor = list[1]
    patch = list[2]
    patch = patch as Integer

    return [major, minor, patch]
}

return this

def version() {
    echo "extracting version..."

    env.WORKSPACE = pwd()
    matcher = readFile("${env.WORKSPACE}/pom.xml")
    def matcher2 = matcher =~ /<version>(.+?)<\/version>/
    def version = matcher2[0][1]

    return version
}

return this

String extractVersionFromPom() {
    def filePath = "pom.xml"
    def pomFile = new File(filePath)
    if (!pomFile.exists()) {
        throw new FileNotFoundException("File not found: $filePath")
    }

    def pomXml = new XmlParser().parse(pomFile)

    try {
        def version = extractVersionFromPom()
        println "Version: $version"
    } catch (Exception e) {
        println e.message
    }

    return pomXml.version.text()
}

return this



