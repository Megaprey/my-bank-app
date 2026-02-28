pipeline {
    agent any

    environment {
        HELM_CHART = 'helm'
        DOCKER_REGISTRY = credentials('docker-registry-url')
        SERVICES = 'auth-server accounts-service cash-service transfer-service notifications-service front-ui'
    }

    stages {
        stage('Validate Helm Charts') {
            steps {
                echo "[umbrella] Validating all Helm charts..."
                sh "helm lint ${HELM_CHART}"
                script {
                    for (svc in env.SERVICES.split(' ')) {
                        sh "helm lint ${HELM_CHART}/charts/${svc}"
                    }
                }
                sh "helm lint ${HELM_CHART}/charts/accounts-db"
                sh "helm lint ${HELM_CHART}/charts/notifications-db"
                echo "[umbrella] All Helm charts validated"
            }
        }

        stage('Build All') {
            steps {
                echo "[umbrella] Building all microservices..."
                sh "mvn clean package -DskipTests"
                echo "[umbrella] All builds completed"
            }
        }

        stage('Test All') {
            steps {
                echo "[umbrella] Running all tests..."
                sh "mvn test"
                echo "[umbrella] All tests passed"
            }
            post {
                always {
                    junit "**/target/surefire-reports/*.xml"
                }
            }
        }

        stage('Docker Build & Push All') {
            steps {
                echo "[umbrella] Building and pushing all Docker images..."
                script {
                    for (svc in env.SERVICES.split(' ')) {
                        echo "[umbrella] Building ${svc}..."
                        sh "docker build -t bank/${svc}:${BUILD_NUMBER} -t bank/${svc}:latest ${svc}/"
                        sh "docker push bank/${svc}:${BUILD_NUMBER}"
                        sh "docker push bank/${svc}:latest"
                    }
                }
                echo "[umbrella] All Docker images pushed"
            }
        }

        stage('Helm Dependency Update') {
            steps {
                echo "[umbrella] Updating Helm dependencies..."
                sh "helm dependency update ${HELM_CHART}"
                echo "[umbrella] Dependencies updated"
            }
        }

        stage('Deploy to Test') {
            steps {
                echo "[umbrella] Deploying all services to test namespace..."
                sh """
                    helm upgrade --install bank ${HELM_CHART} \
                        --namespace test \
                        --create-namespace \
                        -f ${HELM_CHART}/values-test.yaml \
                        --set accounts-service.image.tag=${BUILD_NUMBER} \
                        --set cash-service.image.tag=${BUILD_NUMBER} \
                        --set transfer-service.image.tag=${BUILD_NUMBER} \
                        --set notifications-service.image.tag=${BUILD_NUMBER} \
                        --set front-ui.image.tag=${BUILD_NUMBER} \
                        --set auth-server.image.tag=${BUILD_NUMBER} \
                        --wait --timeout 600s
                """
                echo "[umbrella] Deployed to test namespace"
            }
        }

        stage('Helm Test') {
            steps {
                echo "[umbrella] Running Helm tests..."
                sh "helm test bank --namespace test --timeout 120s"
                echo "[umbrella] Helm tests passed"
            }
        }

        stage('Deploy to Prod') {
            when {
                branch 'master'
            }
            input {
                message "Deploy all services to production?"
                ok "Deploy to Production"
            }
            steps {
                echo "[umbrella] Deploying all services to prod namespace..."
                sh """
                    helm upgrade --install bank ${HELM_CHART} \
                        --namespace prod \
                        --create-namespace \
                        -f ${HELM_CHART}/values-prod.yaml \
                        --set accounts-service.image.tag=${BUILD_NUMBER} \
                        --set cash-service.image.tag=${BUILD_NUMBER} \
                        --set transfer-service.image.tag=${BUILD_NUMBER} \
                        --set notifications-service.image.tag=${BUILD_NUMBER} \
                        --set front-ui.image.tag=${BUILD_NUMBER} \
                        --set auth-server.image.tag=${BUILD_NUMBER} \
                        --wait --timeout 600s
                """
                echo "[umbrella] Deployed to prod namespace"
            }
        }
    }

    post {
        success {
            echo "[umbrella] Pipeline completed successfully"
        }
        failure {
            echo "[umbrella] Pipeline failed"
        }
    }
}
