pipeline {
  agent { label 'python38' }
  options { timeout(time: 60, unit: 'MINUTES') }

  environment {
    // Reuse your existing branch regex style
    DEV_BRANCH_REGEX = ".*DEV_.*"
    UAT_BRANCH_REGEX = ".*STAGING_.*"

    // TODO: confirm these with SA/lead
    IMAGE_REPO    = "docker-icg-dev-local.artifactory.citigroup.net/icg-isg-olympus/olympus-sb-user-consumption"
    DEV_NAMESPACE = "<dev-namespace>"
    UAT_NAMESPACE = "<uat-namespace>"
  }

  stages {

    stage('Build & Push Docker Image') {
      steps {
        sh '''
          set -e
          echo "Building Docker image ${IMAGE_REPO}:${LS_BUILD_VERSION}"
          docker build -t ${IMAGE_REPO}:${LS_BUILD_VERSION} .
          docker push ${IMAGE_REPO}:${LS_BUILD_VERSION}
        '''
      }
    }

    stage('Deploy CronJob to DEV (OCP)') {
      when {
        expression { env.LS_GIT_BRANCH ==~ "${DEV_BRANCH_REGEX}" }
      }
      steps {
        sh '''
          set -e
          helm upgrade --install olympus-sb-user-consumption ./helm/user_consumption \
            --namespace ${DEV_NAMESPACE} \
            --set image.repository=${IMAGE_REPO} \
            --set image.tag=${LS_BUILD_VERSION} \
            --set env.PROFILE=dev \
            --wait --atomic
        '''
      }
    }

    stage('Deploy CronJob to UAT (OCP)') {
      when {
        expression { env.LS_GIT_BRANCH ==~ "${UAT_BRANCH_REGEX}" }
      }
      steps {
        sh '''
          set -e
          helm upgrade --install olympus-sb-user-consumption ./helm/user_consumption \
            --namespace ${UAT_NAMESPACE} \
            --set image.repository=${IMAGE_REPO} \
            --set image.tag=${LS_BUILD_VERSION} \
            --set env.PROFILE=uat \
            --wait --atomic
        '''
      }
    }
  }

  post {
    always {
      echo "OCP pipeline finished"
    }
  }
}