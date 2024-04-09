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
            git "https://github.com/vishnupriyanarayana/docker-frontend-backend-db.git"
          }
        }
      }
      stage ("Docker build") {
        steps {
          container ("docker") {
            script {
              sh 'docker build -t frontend .'
              sh 'docker tag frontend vishnupriya772002/frontend:9'
              sh 'docker push vishnupriya772002/frontend:9'
            }
          }
          
        }
      }
      stage ("Deploy service") {
        steps {
          container ("docker") {
            script {
              sh 'kubectl apply -f frontendkube.yaml -n vishnu'
              sh 'kubectl port-forward svc/frontend-service 3000:3000 -n vishnu'
            }

          }
        }
      }
    }
    

}
