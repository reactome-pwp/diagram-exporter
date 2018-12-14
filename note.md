#Note for developers
In Reactome, we try to use only trusted libraries and minimise the number of dependencies. To cope with different 
formats, we rely in several technologies, described in the table below.
The decision of using each library has been taken regarding its popularity, documentation and support.

Source | Raster(PNG, JPEG, GIF) | SVG | PDF
---|---|---|---
Standard diagram | BufferedImage (SunGraphics2D) | Batik (SVGGraphics2D)  |  *iText5 (PdfGraphics2D)
EHLD | Batik (ImageTranscoder) | as is | **embedded raster

\* Actually, we are not using iText5 as a library. We cloned last version of iText5 PdfGraphics2D class and added the 
necessary methods to meet Reactome specifications. 

\** iText5 PdfGraphics2D is yet too simple to meet EHLD specifications.