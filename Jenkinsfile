import groovy.json.JsonSlurper
// This Jenkinsfile is used by Jenkins to run the DiagramExporter step of Reactome's release.
// It requires that the DiagramConverter step has been run successfully before it can be run.
def currentRelease
pipeline{
	agent any

	stages{
		// This stage checks that upstream project DiagramConverter was run successfully.
		stage('Check DiagramConverter build succeeded'){
			steps{
				script{
					currentRelease = (pwd() =~ /Releases\/(\d+)\//)[0][1];
					// This queries the Jenkins API to confirm that the most recent build of DiagramConverter was successful.
					def diagramUrl = httpRequest authentication: 'jenkinsKey', validResponseCodes: "${env.VALID_RESPONSE_CODES}", url: "${env.JENKINS_JOB_URL}/job/${currentRelease}/job/File-Generation/job/DiagramConverter/lastBuild/api/json"
					if (diagramUrl.getStatus() == 404) {
						error("DiagramConverter has not yet been run. Please complete a successful build.")
					} else {
						def diagramJson = new JsonSlurper().parseText(diagramUrl.getContent())
						if (diagramJson['result'] != "SUCCESS"){
							error("Most recent DiagramConverter build status: " + diagramJson['result'] + ". Please complete a successful build.")
						}
					}
				}
			}
		}
		// This stage builds the jar file using maven.
		stage('Setup: Build jar file'){
			steps{
				script{
					sh "mvn clean compile assembly:single"
				}
			}
		}
		// Execute the jar file, producing data-export files.
		stage('Main: Run Data-Export'){
			steps{
				script{
					def diagramFolderPath = "${env.ABS_DOWNLOAD_PATH}/${currentRelease}/diagram/"
					def ehldFolderPath = "${env.ABS_DOWNLOAD_PATH}/${currentRelease}/ehld/"
					withCredentials([usernamePassword(credentialsId: 'neo4jUsernamePassword', passwordVariable: 'pass', usernameVariable: 'user')]){
						sh "java -Xmx${env.JAVA_MEM_MAX}m -jar target/diagram-exporter-jar-with-dependencies.jar --user $user --password $pass --format svg --input ${diagramFolderPath} --ehld ${ehldFolderPath} --summary ${ehldFolderPath}/svgsummary.txt --target:\"Homo sapiens\" --output ./ --verbose"
						sh "java -Xmx${env.JAVA_MEM_MAX}m -jar target/diagram-exporter-jar-with-dependencies.jar --user $user --password $pass --format png --input ${diagramFolderPath} --ehld ${ehldFolderPath} --summary ${ehldFolderPath}/svgsummary.txt --target:\"Homo sapiens\" --output ./ --verbose"
						sh "java -Xmx${env.JAVA_MEM_MAX}m -jar target/diagram-exporter-jar-with-dependencies.jar --user $user --password $pass --format sbgn --input ${diagramFolderPath} --target:\"Homo sapiens\" --output ./ --verbose"
					}
				}
			}
		}
		/*
		// Archive everything on S3, and move the 'diagram' folder to the download/vXX folder.
		stage('Post: Archive Outputs'){
			steps{
				script{
					def s3Path = "${env.S3_RELEASE_DIRECTORY_URL}/${currentRelease}/data_export"
					def archive = "export-v${currentRelease}.tgz"
					sh "tar -zcvf ${archive} ${folder}"
					sh "mv ${folder}/* ${env.ABS_DOWNLOAD_PATH}/${currentRelease}/" 
					sh "gzip logs/*"
					sh "aws s3 --no-progress --recursive cp logs/ $s3Path/logs/"
					sh "aws s3 --no-progress cp ${archive} $s3Path/"
					sh "rm -r logs/ ${folder} ${archive}"
				}
			}
		}
		*/
	}
}
