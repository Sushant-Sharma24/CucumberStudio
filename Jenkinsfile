import java.util.UUID
import org.jenkinsci.plugins.pipeline.modeldefinition.Utils;

pipeline {
  agent {
    kubernetes {
      cloud 'k8s-config'
      label "ui-tests-${UUID.randomUUID().toString()}"
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: awscli
    image: aztek/awscli
    tty: true
    command: [ 'cat' ]
  - name: hiptest
    image: hiptest/hiptest-publisher
    tty: true
    command: [ 'cat' ]
  - name: dotnet
    image: microsoft/dotnet:2.1-sdk
    tty: true
    command: [ 'cat' ]
  - name: python
    image: python:3
    tty: true
    command: [ 'cat' ]
"""
    }
  }
  parameters {
      string(name: 'TestRunId', description: 'Hiptest test run id', defaultValue: '298094')
      choice(name: 'Environment', choices: ['Dev', 'Test'])
      choice(name: 'Browser', choices: ['Chrome', 'Edge', 'Firefox', 'IE'])
      choice(name: 'Driver', choices: ['SauceLabs', 'Grid'])
      choice(name: 'Folder', description: 'Automation test folder to use', choices: ['InsightUIAutomation.SA'])
      choice(name: "AWS_REGION", choices: ["us-west-2"])
  }
  stages {
    stage("Pull Tests from Hiptest") {
        steps {
          container('awscli') {
            withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: "dellinsights-${params.Environment.toLowerCase()}"]]) {
              withEnv(["BUCKET_NAME=insights-${params.Environment.toLowerCase()}-instance-keys"]) {
                sh """
                  aws --region us-west-2 ec2 describe-instances --filters Name=tag:Name,Values=insights-sa-${params.Environment.toLowerCase()}-bastion | jq '.Reservations[0].Instances[0].PublicDnsName' | tr -d '"' >> bastion_host_${params.Environment}
                  aws s3api get-object --bucket insights-${params.Environment.toLowerCase()}-instance-keys --key insights-${params.Environment.toLowerCase()}.pem bastion_key_${params.Environment}
                  chmod 0600 bastion_key_${params.Environment}
                """
              }
            }
          }

          container('hiptest') {
              sh(script: "hiptest-publisher -c ./${params.Folder}/hiptest-publisher.conf --test-run-id=${params.TestRunId} --meta=test_run_id:${params.TestRunId}", label: 'Pull Tests')
          }
        }
    }
    stage("Build Tests") {
        steps {
            container('dotnet') {
                sh(script: "dotnet build ${WORKSPACE}/${params.Folder}/${params.Folder}.csproj", label: "Build")
            }
        }
    }
    stage('Allow Bastion SSH Access') {
      steps {
        container('python') {
          withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: "dellinsights-${params.Environment.toLowerCase()}"]]) {
            withEnv(["PROJECT=insights-sa", "ENV=${params.Environment.toLowerCase()}", "AWS_DEFAULT_REGION=${params.AWS_REGION}", "CI=true"]) {
              sh '''
                export INGRESS_IP=$(curl ipinfo.io/ip)
                echo $PROJECT
                echo $ENV
                echo $CI
                pip install -r build/bastion/requirements.txt
                python build/bastion/open_bastion.py
              '''
            }
          }
        }
      }
    }
    stage('Open Dev Tunnel') {
      when {
        expression { params.Environment == 'Dev' }
      }
      steps {
        container('dotnet') {
          sh 'ssh -fN -4 -oStrictHostKeyChecking=no -L 9003:rds.insights-sa-' + params.Environment.toLowerCase() + '.com:5432 ec2-user@$(cat bastion_host_' + params.Environment + ') -i bastion_key_' + params.Environment
        }
      }
    }
    stage('Open Test Tunnel') {
      when {
        expression { params.Environment == 'Test' }
      }
      steps {
        container('dotnet') {
          sh 'ssh -fN -4 -oStrictHostKeyChecking=no -L 9003:rds.sa-' + params.Environment.toLowerCase() + '.insights.dell.com:5432 ec2-user@$(cat bastion_host_' + params.Environment + ') -i bastion_key_' + params.Environment
        }
      }
    }
    stage("Run Tests") {
      steps {
        container('dotnet') {
          sh "(dotnet test --no-build -s ${WORKSPACE}/${params.Folder}/${params.Driver}_${params.Environment}_${params.Browser}.runsettings --logger:'nunit;LogFilePath=test-result-${params.TestRunId}.xml' --logger:'trx;LogFilePath=test-result-trx.xml' --results-directory test_results ${params.Folder}/${params.Folder}.csproj --filter TestCategory=${params.TestRunId} && exit 0) || exit 0"
        }
      }
    }
    stage('Remove Bastion SSH Access') {
      steps {
        container('python') {
          withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: "dellinsights-${params.Environment.toLowerCase()}"]]) {
            withEnv(["PROJECT=insights-sa", "ENV=${params.Environment.toLowerCase()}", "AWS_DEFAULT_REGION=${params.AWS_REGION}", "CI=true"]) {
              sh '''
                pip install -r build/bastion/requirements.txt
                export INGRESS_IP=$(curl ipinfo.io/ip)
                python build/bastion/close_bastion.py
              '''
            }
          }
        }
      }
    }
    stage("Publish Results to Hiptest") {
        steps {
            container('hiptest') {
                sh """
                    hiptest-publisher -c ./${params.Folder}/hiptest-publisher.conf --push ./${params.Folder}/test-result-${params.TestRunId}.xml --test-run-id ${params.TestRunId} --push-format nunit
                """
            }
        }
    }	
    stage('Reports') {
      steps {      
        sh "ls ${WORKSPACE}/InsightUIAutomation.SA/bin/Debug/netcoreapp2.1/allure-results"
        sh "cp -R ${WORKSPACE}/InsightUIAutomation.SA/bin/Debug/netcoreapp2.1/allure-results ${WORKSPACE}/allure-results"
        sh "ls ${WORKSPACE}"
        sh ""
        allure([
                includeProperties: false,
                jdk: '',
                properties: [],
                reportBuildPolicy: 'ALWAYS',
                results: [[path: "${WORKSPACE}/InsightUIAutomation.SA/bin/Debug/netcoreapp2.1/allure-results"],[path: '${WORKSPACE}/InsightUIAutomation.SA/bin/Debug/netcoreapp2.1/build/allure-results'], [path: "${WORKSPACE}"]]
          ])
        }
      }
  }
  post {
    always {
      postSlack currentBuild.result, "#insights-jenkins"		 
    }
  }
}