package org.reactome.server.tools.diagram.exporter.raster;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageExport {

	public static void save(BufferedImage image, String format, String stId) throws IOException {
		if (format.equalsIgnoreCase("jpeg")) {
			final BufferedImage jImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			jImage.getGraphics().drawImage(image, 0, 0, Color.WHITE, null);
			ImageIO.write(jImage, format, new File("diagrams/" + stId + "." + format));
		} else {
			ImageIO.write(image, format, new File("diagrams/" + stId + "." + format));
		}

	}
}
