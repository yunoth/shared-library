def call(def server, def port) {
    httpRequest httpMode: 'POST', url: "http://${server}:${port}/shutdown", validResponseCodes: '200,408,404'
    sshagent(['RemoteCredentials']) {
        sh "scp -o StrictHostKeyChecking=no target/*.jar ec2-user@${server}:~/jenkins-demo.jar"
        sh "ssh -o StrictHostKeyChecking=no ec2-user@${server} nohup java -Dserver.port=${port} -jar ~/jenkins-demo.jar &"
    }
    retry (3) {
        sleep 5
        httpRequest url:"http://${server}:${port}/services/UMSL/", validResponseCodes: '200'
    }
}