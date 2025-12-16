pipeline {
    agent any

    environment {
        GOOGLE_CLOUD_PROJECT = credentials('gcp-project-id')   // Project ID
        IMAGE_TAG = "1.0.${BUILD_NUMBER}"
        GITHUB_TOKEN = credentials('github-token')
        SCANNER_HOME = tool 'sonar-scanner'
        REGION = "us-central1"
        REPO_NAME = "docker-repo"
        IMAGE_NAME = "hello"
        GIT_USER_NAME = "somameenakayathirm"
        GIT_USER_EMAIL = "somameenakayathiri@example.com"
    }

    stages {

        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

        stage('Checkout Source Code') {
            steps {
                git branch: 'main',
                    credentialsId: 'github-token',
                    url: 'https://github.com/somameenakayathirim/gcp-secure-app-ci-pipeline.git'
            }
        }

        stage('Authenticate with Google Cloud') {
            steps {
                sh """
                    gcloud auth list
                    gcloud config set project ${GOOGLE_CLOUD_PROJECT}
                    gcloud auth configure-docker ${REGION}-docker.pkg.dev
                """
            }
        }

        stage('Trivy Filesystem Scan') {
            steps {
                sh "trivy fs ."
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar-server') {
                    sh """
                        ${SCANNER_HOME}/bin/sonar-scanner \
                        -Dsonar.projectKey=hello-app \
                        -Dsonar.projectName=hello-app
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                waitForQualityGate abortPipeline: false, credentialsId: 'sonar-token'
            }
        }

        stage('OWASP Dependency Check') {
            steps {
                dependencyCheck additionalArguments: '--scan ./ --disableYarnAudit --disableNodeAudit',
                                odcInstallation: 'DP-Check'
                dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    docker build -t ${IMAGE_NAME}:latest app/
                """
            }
        }

        stage('Tag & Push Image to Artifact Registry') {
            steps {
                sh """
                    docker tag ${IMAGE_NAME}:latest \
                    ${REGION}-docker.pkg.dev/${GOOGLE_CLOUD_PROJECT}/${REPO_NAME}/${IMAGE_NAME}:${IMAGE_TAG}

                    docker tag ${IMAGE_NAME}:latest \
                    ${REGION}-docker.pkg.dev/${GOOGLE_CLOUD_PROJECT}/${REPO_NAME}/${IMAGE_NAME}:latest

                    docker push ${REGION}-docker.pkg.dev/${GOOGLE_CLOUD_PROJECT}/${REPO_NAME}/${IMAGE_NAME}:${IMAGE_TAG}
                    docker push ${REGION}-docker.pkg.dev/${GOOGLE_CLOUD_PROJECT}/${REPO_NAME}/${IMAGE_NAME}:latest
                """
            }
        }

        stage('Trivy Image Scan') {
            steps {
                sh """
                    trivy image ${REGION}-docker.pkg.dev/${GOOGLE_CLOUD_PROJECT}/${REPO_NAME}/${IMAGE_NAME}:${IMAGE_TAG}
                """
            }
        }

        stage('Update Helm values.yaml') {
            steps {
                sh """
                    git config user.name "${GIT_USER_NAME}"
                    git config user.email "${GIT_USER_EMAIL}"

                    echo "===== BEFORE ====="
                    cat helm/values.yaml

                    sed -i "s|tag:.*|tag: ${IMAGE_TAG}|" helm/values.yaml

                    echo "===== AFTER ====="
                    cat helm/values.yaml

                    git add helm/values.yaml

                    if git diff --cached --quiet; then
                      echo "No changes to commit"
                    else
                      git commit -m "chore: update image tag to ${IMAGE_TAG}"
                      git push https://${GITHUB_TOKEN}@github.com/<your-username>/<your-repo>.git HEAD:main
                    fi
                """
            }
        }

        stage('CI Completed') {
            steps {
                echo "CI pipeline completed successfully. ArgoCD will sync automatically."
            }
        }
    }
}
