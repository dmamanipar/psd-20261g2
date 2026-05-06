pipeline {
    agent any

    tools {
        maven 'MAVEN_HOME'
    }

    environment {
        // ── Credenciales desde Jenkins Credentials Manager ──────────
        GITHUB_TOKEN  = credentials('github-token')
        SONAR_TOKEN   = credentials('Sonarqube')
        TOMCAT_CREDS  = credentials('tomcat-credentials')
        // TOMCAT_CREDS_USR y TOMCAT_CREDS_PSW se generan automáticamente

        // ── URLs de contenedores en network_jenkins ──────────────────
        SONAR_HOST_URL = 'http://sonarqube:9000'
        TOMCAT_URL     = 'http://tomcat-psd:8080'

        // ── Proyecto ─────────────────────────────────────────────────
        POM_PATH  = 'SysAlmacen/pom.xml'
        WAR_FILE  = 'SysAlmacen/target/SysAlmacen.war'
        APP_PATH  = '/SysAlmacen'
    }

    stages {

        stage('Clone') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    git branch: 'main',
                        credentialsId: 'github-token',
                        url: 'https://github.com/dmamanipar/PSD-DMP.git'
                }
            }
        }

        stage('Build') {
            steps {
                timeout(time: 8, unit: 'MINUTES') {
                    sh "mvn -DskipTests clean package -f ${POM_PATH}"
                }
            }
        }

        stage('Test') {
            environment {
                // Necesario para Testcontainers dentro del contenedor Jenkins
                DOCKER_HOST                   = 'unix:///var/run/docker.sock'
                TESTCONTAINERS_RYUK_DISABLED  = 'true'
                TESTCONTAINERS_CHECKS_DISABLE = 'true'
                TESTCONTAINERS_HOST_OVERRIDE  = 'host.docker.internal'
            }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    // clean install para generar reporte Jacoco igual que tenías
                    sh "mvn clean install -f ${POM_PATH}"
                }
            }
            post {
                always {
                    // Publicar resultados de tests en Jenkins UI
                    junit allowEmptyResults: true,
                          testResults: 'SysAlmacen/**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Sonar') {
            steps {
                timeout(time: 4, unit: 'MINUTES') {
                    withSonarQubeEnv('sonarqube') {
                        // Igual que tenías, con token desde credenciales
                        sh """
                            mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.0.2155:sonar \
                                -Pcoverage \
                                -Dsonar.token=${SONAR_TOKEN} \
                                -f ${POM_PATH}
                        """
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                sleep(10)
                timeout(time: 4, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        // Credenciales desde Jenkins — ya no hardcodeadas
                        sh """
                            curl -u ${TOMCAT_CREDS_USR}:${TOMCAT_CREDS_PSW} \
                                 -T "${WAR_FILE}" \
                                 "${TOMCAT_URL}/manager/text/deploy?path=${APP_PATH}&update=true"
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Deploy exitoso → http://localhost:8080${APP_PATH}"

            // Notificar estado OK al commit de GitHub
            withCredentials([string(credentialsId: 'github-token', variable: 'GH_TOKEN')]) {
                sh """
                    curl -s \
                         -H "Authorization: token ${GH_TOKEN}" \
                         -H "Content-Type: application/json" \
                         -X POST \
                         -d '{"state":"success","description":"Pipeline OK","context":"jenkins/ci"}' \
                         https://api.github.com/repos/dmamanipar/PDS-Desarrollo2025II/statuses/${GIT_COMMIT}
                """
            }
        }

        failure {
            echo "Pipeline falló — revisar logs en Jenkins"

            withCredentials([string(credentialsId: 'github-token', variable: 'GH_TOKEN')]) {
                sh """
                    curl -s \
                         -H "Authorization: token ${GH_TOKEN}" \
                         -H "Content-Type: application/json" \
                         -X POST \
                         -d '{"state":"failure","description":"Pipeline falló","context":"jenkins/ci"}' \
                         https://api.github.com/repos/dmamanipar/PDS-Desarrollo2025II/statuses/${GIT_COMMIT}
                """
            }
        }

        always {
            cleanWs()
        }
    }
}