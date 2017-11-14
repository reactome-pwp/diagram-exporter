[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

# Raster exporter
Exports a diagram in the Pathway Browser to an image. Current supported formats are:

* png (transparent background)
* jpg, jpeg, gif (white background)

The diagram is generated using the diagram JSON file, so you may notice  small differences with Pathway Browser.

With the raster exporter you can generate High Definition images up to 100 megapixels.

Images are generated from the EHLD if it is available.

## Exporting a diagram

### Basic usage
```java
// This path must contain "R-HSA-169911.json" and "R-HSA-169911.graph.json" files
String diagramPath = "path/to/diagram";
String ehldPath = "path/to/ehld";

final SimpleRasterArgs args = new SimpleRasterArgs("R-HSA-5602410", "jpg");
args.setFactor(3.);
args.setProfiles(new ColorProfiles("standard", null, null));
args.setBackground("#444444");

final BufferedImage image = RasterExporter.export(args, diagramPath, ehldPath);

// If saving to a file
final File file = new File(args.getStId() + "." + args.getFormat());
ImageIO.write(image, args.getFormat(), file);

// If sending through an URL
URL url = new URL("...");
HttpUrlConnection connection = (HttpUrlConnection) url.openConnection();
connection.setDoOutput(true);  // your url must support writing
ImageIO.write(image, args.getFormat(), connection.getOutputStream());    

```

### Decorators
Raster exporter supports decoration: selection and flags. You can specify dbIds, stIds, interactors or geneNames, or a mix of them.

```java
args.setSelected(Arrays.asList("1234", "R-HSA-1234"));
args.setFlags(Arrays.asList("PTEN"));
```

### Analysis
If you want to see an analysis overlaid, simply add the token to the args.
```java
args.setToken("MjAxNzExMDYxMDQ3MzBfMzA%253D");
```

Diagram exporter automatically detects which type of analysis you run and properly mimics PathwayBrowser appearance.

### Animated GIFs
If you run an analysis with expression values and want to see an animated GIF with changing colors, use the *exportToGif* method.
```java
String stId = "R-HSA-1234";
File file = new File(stId + ".gif");
OutputStream os = new FileOutputStream(file);
RasterRenderer.renderToGif(stId, diagramPath, ehldPath, os);
os.close();
```

## Status

feature|diagram|ehld
---|---|---
PNG|Yes|Yes
JPG,JPEG|Yes|Yes
GIF|Yes|Yes
Animated GIF|Yes|Yes
Selection|Yes|No\*
Flag|Yes|No\*
EXPRESSION|Yes|Yes
ENRICHMENT|Yes|Yes
BACKGROUND|Yes|Yes
EXP COLUMN|Yes|Yes

\* There is a bug with the SVG renderer (https://issues.apache.org/jira/browse/BATIK-1207). For stability, selection and flagging are deactivated.

## Requirements
This project requires an external library to deal with the SVG files: BATIK 1.9.
To install BATIK, download from its webpage, and add all the jar files as dependencies.
https://xmlgraphics.apache.org/batik/download.html
