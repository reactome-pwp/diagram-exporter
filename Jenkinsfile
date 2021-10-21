// This Jenkinsfile is used by Jenkins to run the DiagramExporter step of Reactome's release.
// It requires that the DiagramConverter step has been run successfully before it can be run.

import org.reactome.release.jenkins.utilities.Utilities

// Shared library maintained at 'release-jenkins-utils' repository.
def utils = new Utilities()
pipeline{
	agent any

	stages{
		// This stage checks that upstream project DiagramConverter was run successfully.
		stage('Check DiagramConverter build succeeded'){
			steps{
				script{
                    utils.checkUpstreamBuildsSucceeded("File-Generation/job/DiagramConverter/")
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
		// Execute the jar file, producing svg, png ang sbgn diagram files.
		stage('Main: Run Diagram-Exporter'){
			steps{
				script{
				    def releaseVersion = utils.getReleaseVersion()
					def diagramFolderPath = "${env.ABS_DOWNLOAD_PATH}/${releaseVersion}/diagram/"
					def ehldFolderPath = "${env.ABS_DOWNLOAD_PATH}/${releaseVersion}/ehld/"
					withCredentials([usernamePassword(credentialsId: 'neo4jUsernamePassword', passwordVariable: 'pass', usernameVariable: 'user')]){
						sh "java -Xmx${env.JAVA_MEM_MAX}m -jar target/diagram-exporter-exec.jar --user $user --password $pass --format svg --input ${diagramFolderPath} --ehld ${ehldFolderPath} --summary ${ehldFolderPath}/svgsummary.txt --target:\"Homo sapiens\" --output ./ --verbose"
						sh "java -Xmx${env.JAVA_MEM_MAX}m -jar target/diagram-exporter-exec.jar --user $user --password $pass --format png --input ${diagramFolderPath} --ehld ${ehldFolderPath} --summary ${ehldFolderPath}/svgsummary.txt --target:\"Homo sapiens\" --output ./ --verbose"
						sh "java -Xmx${env.JAVA_MEM_MAX}m -jar target/diagram-exporter-exec.jar --user $user --password $pass --format sbgn --input ${diagramFolderPath} --target:\"Homo sapiens\" --output ./ --verbose"
					}
				}
			}
		}
		stage('Post: Generate DiagramExporter archives and move them to the downloads folder') {
		    steps{
		        script{
				def releaseVersion = utils.getReleaseVersion()
				def svgArchive = "diagrams.svg.tgz"
				def pngArchive = "diagrams.png.tgz"
				def sbgnArchive = "homo_sapiens.sbgn.tar.gz"
				def downloadPath = "${env.ABS_DOWNLOAD_PATH}/${releaseVersion}"

				sh "cd svg/Modern/; tar -zcf ${svgArchive} *.svg; mv ${svgArchive} ../../"
				sh "cd png/Modern/; tar -zcf ${pngArchive} *.png; mv ${pngArchive} ../../"
				sh "cd sbgn/; tar -zcf ${sbgnArchive} *.sbgn; mv ${sbgnArchive} ../"

				sh "cp ${svgArchive} ${downloadPath}/"
				sh "cp ${pngArchive} ${downloadPath}/"
				sh "cp ${sbgnArchive} ${downloadPath}/"
		        }
		    }
		}
		// Move output contents to the download/XX folder, and archive everything on S3.
		stage('Post: Archive Outputs'){
			steps{
				script{
					def releaseVersion = utils.getReleaseVersion()
					def dataFiles = ["diagrams.svg.tgz", "diagrams.png.tgz", "homo_sapiens.sbgn.tar.gz"]
					def logFiles = []
					def foldersToDelete = ["svg", "png", "sbgn"]
					utils.cleanUpAndArchiveBuildFiles("diagram_exporter", dataFiles, logFiles, foldersToDelete)
				}
			}
		}
	}
}
