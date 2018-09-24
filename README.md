[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

Diagram Exporter
=====================

### Introduction
This project includes methods for exporting a given diagram into an image (png, jpeg or jpeg) or a PowerPoint file.

 * [Power point exporter](src/main/java/org/reactome/server/tools/diagram/exporter/pptx/README.md)
 * [Raster exporter](src/main/java/org/reactome/server/tools/diagram/exporter/raster/README.md)


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
    <version>1.3.9</version>
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
