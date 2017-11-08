package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.batik.util.SVGConstants;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.w3c.dom.svg.SVGDocument;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public class EHLDRenderer {

	private final SVGDocument document;
	private final RasterArgs args;

	public EHLDRenderer(RasterArgs args, String ehldPath) throws EHLDException {
		this.document = ResourcesFactory.getEHLD(ehldPath, args.getStId());
		this.args = args;
	}

	public BufferedImage render() {
		SVGDecoratorFactory.selectAndFlag(document, args);
		SVGAnalisysRenderer.analysis(document, args);
		return renderImage();
	}

	private BufferedImage renderImage() {
		final String viewBox = document.getRootElement().getAttribute(SVGConstants.SVG_VIEW_BOX_ATTRIBUTE);
		final Scanner scanner = new Scanner(viewBox);
		scanner.useLocale(Locale.UK);
		scanner.nextFloat();  // x
		scanner.nextFloat();  // y
		float width = scanner.nextFloat();
		float height = scanner.nextFloat();

		final TranscoderInput input = new TranscoderInput(document);

		// FIXME: this is just for debugging. remove for production
		toFile(input);

		return toImage(width, height, input);
	}

	private BufferedImage toImage(float width, float height, TranscoderInput input) {

		final float factor = new Float(args.getFactor());
		final float w = width * factor;
		final float h = height * factor;

		final ImageTranscoder transcoder;
		switch (args.getFormat()) {
			case "jpg":
			case "jpeg":
				transcoder = new JPEGTranscoder();
				break;
			default:
				transcoder = new PNGTranscoder();
		}
		transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, w);
		transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, h);
		try {
			final File file = File.createTempFile(args.getStId(), args.getFormat());
			final TranscoderOutput output = new TranscoderOutput(new FileOutputStream(file));
			transcoder.transcode(input, output);
			System.out.println("YES: " + factor);
			return ImageIO.read(file);
		} catch (TranscoderException e) {
			e.printStackTrace();
			System.out.println("NO: " + factor);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void toFile(TranscoderInput input) {
		try {
			final Transcoder trans = new SVGTranscoder();
			final File output = new File("ehld.svg");
			trans.transcode(input, new TranscoderOutput(new FileWriter(output)));
		} catch (TranscoderException | IOException e) {
			e.printStackTrace();
		}
	}
}
