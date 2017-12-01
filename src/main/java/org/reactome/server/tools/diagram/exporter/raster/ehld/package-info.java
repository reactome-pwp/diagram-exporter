/**
 * Exporter for Enhanced High Level Diagrams (EHLDs). The format of these
 * diagrams is svg, so the architecture is completely different to the regular
 * diagrams, although its usage is similar:
 * <code>
 * <pre>
 *     RasterArgs args = new RasterArgs(stId, "png");
 *     RasterRenderer renderer = new EHLDRenderer(args, EHLDPath);
 *     Dimension dimension = renderer.getDimension();
 *     BufferedImage image = renderer.render();
 * </pre>
 * </code>
 */
package org.reactome.server.tools.diagram.exporter.raster.ehld;
