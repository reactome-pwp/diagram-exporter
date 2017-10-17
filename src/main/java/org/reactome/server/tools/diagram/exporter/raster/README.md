# Raster exporter
Exports a diagram in the Pathway Browser to an image. Current supported formats are:

* png (transparent background)
* jpg, jpeg, gif (white background)

The diagram is generated using the diagram JSON file, so you may notice  small differences with Pathway Browser.

<div>
<img src=doc/R-HSA-169911_pathway-browser.png width=300/>
<img src=doc/R-HSA-169911_diagram-exporter.png width=300/>
</div>

With the raster exporter you can generate High Definition images up to 100 megapixels.

## Exporting a diagram

```java
String stId = "R-HSA-169911";
// This path must contain "R-HSA-169911.json" and "R-HSA-169911.graph.json" files
String diagramPath = "path/to/diagram";
String ext = "png"; // png, jpeg, jpg, gif
Double factor = 2;  // 0.1 to 10

Decorator decorator = new Decorator(selection, flags);
String token = "MjAxNzEwMTIxNTE2MjJfNjk%253D"; // can be null
ColorSchem scheme = new ColorScheme("modern", "standard", "cyan");

BufferedImage image = RasterExporter.export(stId, diagramPath,
	ext, factor, decorator, token, scheme);

// If sending through an URL
URL url = ...;
HttpUrlConnection connection = (HttpUrlConnection) url.openConnection();
connection.setDoOutput(true);  // your url must support writing
ImageIO.write(image, ext, connection.getOutputStream());

// If saving to a file
ImageIO.write(image, ext, new File(path, filename));
```
