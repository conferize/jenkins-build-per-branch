package com.entagen.jenkins

import java.util.regex.Pattern

class GitApi {
    String gitUrl
    Pattern branchNameFilter = null

    public List<String> getBranchNames() {
        String command = "git ls-remote --heads ${gitUrl}"
        List<String> branchNames = []

        println "#### 1"
        eachResultLine(command) { String line ->
            println "#### 2 a"
            String branchNameRegex = "^.*\trefs/heads/(.*)\$"
            String branchName = line.find(branchNameRegex) { full, branchName -> branchName }
            Boolean selected = passesFilter(branchName)
            println "\t" + (selected ? "* " : "  ") + "$line"
            // lines are in the format of: <SHA>\trefs/heads/BRANCH_NAME
            // ex: b9c209a2bf1c159168bf6bc2dfa9540da7e8c4a26\trefs/heads/master
            if (selected) branchNames << branchName
            println "#### 2 b"
        }

        println "#### 3"
        return branchNames
    }

    public Boolean passesFilter(String branchName) {
        if (!branchName) return false
        if (!branchNameFilter) return true
        return branchName ==~ branchNameFilter
    }

    // assumes all commands are "safe", if we implement any destructive git commands, we'd want to separate those out for a dry-run
    public void eachResultLine(String command, Closure closure) {
        println "executing command: $command"

        println "xxxx 1"
        def process = command.execute()
        println "xxxx 2"
        def inputStream = process.getInputStream()
        println "xxxx 3"
        def gitOutput = ""
        println "xxxx 4"

        def env = System.getenv()
        for(e in env) {
          println "$e = ${e}"
        }

        while(true) {
          println "xxxx 5 a"
          int readByte = inputStream.read()
          println "xxxx 5 b"
          if (readByte == -1) break // EOF
          println "xxxx 5 c"
          byte[] bytes = new byte[1]
          println "xxxx 5 d"
          bytes[0] = readByte
          println "xxxx 5 f"
          gitOutput = gitOutput.concat(new String(bytes))
          println "xxxx 5 z"
        }
        println "xxxx 6"
        process.waitFor()

        println "xxxx 7"
        if (process.exitValue() == 0) {
            println "xxxx 8"
            gitOutput.eachLine { String line ->
               closure(line)
          }
        } else {
            String errorText = process.errorStream.text?.trim()
            println "error executing command: $command"
            println errorText
            throw new Exception("Error executing command: $command -> $errorText")
        }
        println "xxxx 10"
    }

}
