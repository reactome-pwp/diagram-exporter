[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

Diagram Exporter
=====================

### Introduction
This project exports a given diagram in the Pathway Browser into fully compatible Microsoft PowerPoint. The output pptx file is a real Power Point presentation where all the elements, such as, shapes and connectors can be rearranged and formatted openly. Extra parameters, for instance, colour profile, flag and select can be set.
At the moment, we are currently exporting to PowerPoint only, but we aim to export a diagram also to png.

<div>
<img src=https://cloud.githubusercontent.com/assets/6883670/23128274/3f6dc596-f776-11e6-88e5-54b76b33049f.png width=380 height=200 />
<img src=https://cloud.githubusercontent.com/assets/6883670/23128285/49d61eb6-f776-11e6-804b-e663dca34712.png width=380 height=200 />
</div>

When exporting to PowerPoint this code relies on the Diagram JSON (static files to render the diagram in Canvas) and render it using the 3rd party library.
### Usage

#### Pre-requirements
* Maven 3.x.x
* Java 8


#### Clone and install

```console
git clone https://github.com/reactome-pwp/diagram-exporter.git
cd diagram-exporter
mvn clean package
```

#### Exporting

Usage:
```console
java -jar ./target/diagram-exporter-jar-with-dependencies.jar
     -i <stId> Stable Identifier of the diagram
     -p <colourProfile> The colour profile [Modern or Standard]
     -o <output> The output folder
     -j <static> The static json's folder
     -f <flag> The element(s) to be flagged
     -s <selection> The element(s) to be selected
     -l <license> Software License file
```

Example:
```console
java -jar ./target/diagram-exporter-jar-with-dependencies.jar \
     -i R-HSA-69620 \
     -p Modern \
     -o /Users/reactome/Reactome/diagram/exporter \
     -j /Users/reactome/Reactome/diagram/static \
     -f 8852316,3786256 \
     -s 8852354,8852316,182558,29358,68374,113582,69604,69591 \
     -l /Users/reactome/slides/Aspose.Slides.lic
```

The output file is stored in the folder used in the `output folder` + profile.
If flagging and/or selection are present then the output file name is going to have unique name and won't overwrite the existing files

### Adding Diagram Exporter in your project

#### Maven Dependency

```html
<dependency>
    <groupId>org.reactome.server.tools</groupId>
    <artifactId>diagram-exporter</artifactId>
    <version>1.0.0</version>
</dependency>
```

```html
<!-- EBI repo -->
<repository>
    <id>pst-release</id>
    <name>EBI Nexus Repository</name>
    <url>http://www.ebi.ac.uk/Tools/maven/repos/content/repositories/pst-release</url>
</repository>
```

#### Calling the Exporter

```java
...
// Create diagram service instance
DiagramService diagramService = new DiagramService();

/**
 * @param stId                stable identifier of the diagram
 * @param diagramJsonFolder   static for the diagram json
 * @param profileName         Color profile name
 * @param outputFolder        output folder, the given folder + the color profile as a folder.
 * @param decorator           class that holds the flagged and the selected elements into two different lists.
 * @param licenseFilePath     a valid Aspose Software License.
 * @return the pptx file
 */
File myPPTX = diagramService.exportToPPTX(stId,
                                          diagramJsonFolder,
                                          profileName,
                                          outputFolder,
                                          decorator,
                                          licenseFilePath);

// Decorators can be null
// License file can be null, however evaluation version will created.
// At this point the file has been already written in the disk, myPPTX is a reference for it.
...
```

#### Software License / Evaluation Version

We are generating the Power Point based on a proprietary software. This license is unique and restricts Reactome of sharing to our users.
In order to create file without been watermarked as evaluation version, please visit [Aspose](https://www.aspose.com/products/slides/java) and purchase your on license.
Once you get the `.lic` file, indicate it in our method. If you hold a copy of our project on your local you may add the file in the src/main/resource, this is first path our code searches for a valid license.
