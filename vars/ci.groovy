pipeline {
  environment {
    DOCKER_REGISTRY_USER = credentials("DockerRegistryUserName")
    DOCKER_REGISTRY_PASS = credentials("DockerRegistryPassword")

  }
    agent {
        kubernetes {
            yaml '''
              apiVersion: v1
              kind: Pod
              spec:
                containers:
                - name: docker 
                  image: docker:20-dind
                  securityContext:
                      privileged: true
                  tty: true
                - name: kubectl
                  image: bitnami/kubectl
                  tty: true
              '''          

        }

    }
    stages {
      stage ("Git checkout") {
        steps {
          script {
            git branch: 'master', url: "https://github.com/vishnupriyanarayana/docker-frontend-backend-db.git"
          }
        }
      }
      stage('Load jenkinsfile') {
            steps {
                script {
                    // Load and execute the Jenkinsfile
                    def jenkinsfilePath = 'jenkinsfile'
                    load jenkinsfilePath
                }
            }
      } 
      stage ("Docker build frontend") {
        steps {
          container ("docker") {
            when {
                // Condition to trigger the frontend build, for example, if the branch is 'frontend'
                expression { env.BRANCH_NAME == 'frontend' }
            }
            script {
              sh 'docker build -t frontend .'
              dockerBuild.runDockerBuild(config.frontendRegistryUrl, config.frontendImage, "${tagBuildNumber}", "${DOCKER_REGISTRY_USER}", "${DOCKER_REGISTRY_PASS}", config.frontendDockerfileLocation)
            }
          }
          
        }
      }
      stage ("Docker build backend") {
        steps {
          container ("docker") {
            when {
                // Condition to trigger the frontend build, for example, if the branch is 'frontend'
                expression { env.BRANCH_NAME == 'backend' }
            }
            script {
              sh 'docker build -t backend .'
              dockerBuild.runDockerBuild(config.frontendRegistryUrl, config.frontendImage, "${tagBuildNumber}", "${DOCKER_REGISTRY_USER}", "${DOCKER_REGISTRY_PASS}", config.backendDockerfileLocation)
            }
          }
          
        }
      }
      stage ("Push images") {
        steps {
          container ("docker") {
            script {
              sh 'docker tag frontend vishnupriya772002/frontend:9'
              sh 'docker push vishnupriya772002/frontend:9'
            }

          }
        }
      }
    }
    

}
