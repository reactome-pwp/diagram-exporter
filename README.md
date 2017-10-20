<img src=https://cloud.githubusercontent.com/assets/6883670/22938783/bbef4474-f2d4-11e6-92a5-07c1a6964491.png width=220 height=100 />

Diagram Exporter
=====================

### Introduction
This project includes methods for exporting a given diagram into an image (png, jpeg or jpeg) or a PowerPoint file.

 * [Power point exporter](src/main/java/org/reactome/server/tools/diagram/exporter/pptx/README.md)
 * [Image exporter](src/main/java/org/reactome/server/tools/diagram/exporter/raster/README.md)


### Pre-requirements
* Maven 3.x.x
* Java 8


### Clone and install

```console
git clone https://github.com/reactome-pwp/diagram-exporter.git
cd diagram-exporter
mvn clean package
```
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
