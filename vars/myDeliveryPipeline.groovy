def call(Map pipelineParams) {

    pipeline {
        agent { label 'jenkins-slave' }
        stages {
            stage('checkout git') {
                steps {
                    git branch: pipelineParams.branch, credentialsId: 'GitCredentials', url: pipelineParams.scmUrl
                }
            }

            stage('build') {
                steps {
                    sh 'mvn clean package -DskipTests=true'
                }
            }

            stage ('test') {
                steps {
                    parallel (
                        "unit tests": { sh 'mvn test' },
                        "integration tests": { sh 'mvn integration-test' }
                    )
                }
            }
            stage('Archive') {
                steps {
                    archiveArtifacts "target/*.jar"
                }
            }

            stage('Approval') {
            // no agent, so executors are not used up when waiting for approvals
                agent none
                steps {
                    script {
                        def deploymentDelay = input id: 'Deploy', message: 'Deploy to production?', submitter: 'yunoth,admin', parameters: [choice(choices: ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24'], description: 'Hours to delay deployment?', name: 'deploymentDelay')]
                        sleep time: deploymentDelay.toInteger(), unit: 'HOURS'
                    }
                }
            }

            stage('deploy production'){
                steps {
                    deploy(pipelineParams.productionServer, pipelineParams.serverPort)
                }
            }
        }
        post {
            failure {
                mail to: pipelineParams.email, subject: 'Pipeline failed', body: "${env.BUILD_URL}"
            }
        }
    }
}