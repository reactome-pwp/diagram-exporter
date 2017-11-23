package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.RasterRenderer;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDRuntimeException;
import org.reactome.server.tools.diagram.exporter.raster.gif.AnimatedGifEncoder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.batik.util.SVGConstants.*;

/**
 * Main class to create a render from an EHLD.
 */
public class EHLDRenderer implements RasterRenderer {

	private static final Set<String> TRANSPARENT_FORMATS = new HashSet<>(Collections.singletonList("png"));
	private static final Set<String> NO_TRANSPARENT_FORMATS = new HashSet<>(Arrays.asList("jpg", "jpeg", "gif"));

	private static final float MARGIN = 15;
	private final SVGDocument document;
	private final RasterArgs args;
	private SVGAnalysis svgAnalysis;

	public EHLDRenderer(RasterArgs args, String ehldPath) throws EHLDException {
		this.document = ResourcesFactory.getEHLD(ehldPath, args.getStId());
		this.args = args;
		virtualRendering(args);

	}

	private void virtualRendering(RasterArgs args) {
		SVGDecoratorRenderer.selectAndFlag(document, args);
		disableMasks();
		svgAnalysis = new SVGAnalysis(document, args);
		svgAnalysis.analysis();
		updateDocumentDimensions();
		// TODO: remove to production, but don't remove from here
		// toFile();
	}

	@Override
	public Dimension getDimension() {
		final String viewBox = document.getRootElement().getAttribute(SVG_VIEW_BOX_ATTRIBUTE);
		final String[] values = viewBox.split(" ");
		final double width = Double.valueOf(values[2]);
		final double height = Double.valueOf(values[3]);
		return new Dimension((int) (width + 0.5), (int) (height + 0.5));
	}

	@Override
	public BufferedImage render() {
		return toImage();
	}

	private void toFile() {
		try {
			final File file = new File("/media/pascual/Disco1TB/reactome/ehld2/", args.getStId() + ".svg");
			final TranscoderInput input = new TranscoderInput(document);
			final TranscoderOutput output = new TranscoderOutput(new FileWriter(file));
			final Transcoder transcoder = new SVGTranscoder();
			transcoder.transcode(input, output);
		} catch (IOException | TranscoderException e) {
			e.printStackTrace();
		}
	}

	private void disableMasks() {
		final NodeList styleList = document.getRootElement().getElementsByTagNameNS(SVG_NAMESPACE_URI, SVG_STYLE_ATTRIBUTE);
		final Node style = styleList.getLength() > 0 ? styleList.item(0) : null;
		final NodeList masks = document.getElementsByTagNameNS(SVG_NAMESPACE_URI, SVG_MASK_TAG);
		final List<Element> maskNodes = IntStream.range(0, masks.getLength())
				.mapToObj(masks::item)
				.map(Element.class::cast)
				.collect(Collectors.toList());
		maskNodes.forEach(mask -> {
			mask.getParentNode().removeChild(mask);
			removeMaskFromStyle(style, mask.getAttribute(SVG_ID_ATTRIBUTE));
		});

	}

	private void removeMaskFromStyle(Node style, String maskId) {
		if (style == null)
			return;
		final String maskRef = String.format("mask:url(#%s);", maskId);
		style.setTextContent(style.getTextContent().replace(maskRef, ""));
	}

	private void updateDocumentDimensions() {
		final String viewBox = document.getRootElement().getAttribute(SVG_VIEW_BOX_ATTRIBUTE);
		final Scanner scanner = new Scanner(viewBox);
		scanner.useLocale(Locale.UK);
		scanner.nextFloat();  // x
		scanner.nextFloat();  // y
		float width = scanner.nextFloat();
		float height = scanner.nextFloat();

		width += 2 * MARGIN;
		height += 2 * MARGIN;
		// 1 increase image dimensions
		final String newVB = String.format(Locale.UK, "0 0 %.3f %.3f", width, height);
		document.getRootElement().setAttribute(SVG_VIEW_BOX_ATTRIBUTE, newVB);
		// 2 create a g translated (margin, margin)
		final Element group = document.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		final String translate = String.format(Locale.UK, "%s(%f,%f)", SVG_TRANSLATE_VALUE, MARGIN, MARGIN);
		group.setAttribute(SVG_TRANSFORM_ATTRIBUTE, translate);

		// 3 append to the translated group all the g elements in the root
		final List<Node> children = new LinkedList<>();
		for (int i = 0; i < document.getRootElement().getChildNodes().getLength(); i++)
			children.add(document.getRootElement().getChildNodes().item(i));
		children.stream()
				.filter(node -> node.getNodeName().equals(SVG_G_TAG))
				.forEach(group::appendChild);

		document.getRootElement().appendChild(group);

		// Apply factor/scale
		// NOTE: there are 3 ways of scaling:
		//  * 1: set width and height on svg root
		//    2: apply a transform to the elements transform: scale(factor)
		//    3: set width and height on Transcoder
		document.getRootElement().setAttribute(SVG_WIDTH_ATTRIBUTE, String.format(Locale.UK, "%.3f", width * args.getFactor()));
		document.getRootElement().setAttribute(SVG_HEIGHT_ATTRIBUTE, String.format(Locale.UK, "%.3f", height * args.getFactor()));
	}

	private BufferedImage toImage() {
		try {
			final TranscoderInput input = new TranscoderInput(document);
			final BufferedImageTranscoder transcoder = new BufferedImageTranscoder(args);
			transcoder.transcode(input, null);
			return transcoder.getImage();
		} catch (TranscoderException e) {
			throw new EHLDRuntimeException(e.getMessage());
		}
	}

	public void renderToAnimatedGif(OutputStream os) throws IOException {
		if (svgAnalysis.getAnalysisType() != AnalysisType.EXPRESSION)
			throw new IllegalStateException("Only EXPRESSION analysis can be rendered into animated GIFs");

		final AnimatedGifEncoder encoder = new AnimatedGifEncoder();
		encoder.setDelay(1000);
		encoder.setRepeat(0);
		encoder.start(os);
		for (int expressionColumn = 0; expressionColumn < svgAnalysis.getAnalysisResult().getExpression().getColumnNames().size(); expressionColumn++) {
			svgAnalysis.setColumn(expressionColumn);
			final BufferedImage image = toImage();
			encoder.addFrame(image);
		}
		encoder.finish();
	}

	/**
	 * There is no a standard BufferedImageTranscoder, although all Transcorders
	 * use BufferedImages as the raster. This class exposes that BufferedImage,
	 * so no need to store them in a File.
	 */
	private static class BufferedImageTranscoder extends ImageTranscoder {

		private BufferedImage image;
		private String format;
		private Color background;

		BufferedImageTranscoder(RasterArgs args) {
			this.background = args.getBackground() == null
					? Color.WHITE
					: args.getBackground();
			this.format = args.getFormat();
		}

		@Override
		public BufferedImage createImage(int w, int h) {
			if (TRANSPARENT_FORMATS.contains(format)) {
				return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			} else if (NO_TRANSPARENT_FORMATS.contains(format)) {
				final BufferedImage image;
				image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				final Graphics2D graphics = image.createGraphics();
				graphics.setBackground(background);
				graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
				return image;
			} else
				throw new IllegalArgumentException("Unsupported file extension " + format);
		}

		@Override
		public void writeImage(BufferedImage image, TranscoderOutput output) throws TranscoderException {
			this.image = image;
		}

		public BufferedImage getImage() {
			return image;
		}
	}
}
