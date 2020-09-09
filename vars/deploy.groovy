def call(def server, def port) {
    httpRequest httpMode: 'POST', url: "http://${server}:${port}/shutdown", validResponseCodes: '200,408'
    sshagent(['RemoteCredentials']) {
        sh "scp -o StrictHostKeyChecking=no target/*.jar root@${server}:/opt/jenkins-demo.jar"
        sh "ssh -o StrictHostKeyChecking=no root@${server} nohup java -Dserver.port=${port} -jar /opt/jenkins-demo.jar &"
    }
    retry (3) {
        sleep 5
        httpRequest url:"http://${server}:${port}/health", validResponseCodes: '200', validResponseContent: '"status":"UP"'
    }
}