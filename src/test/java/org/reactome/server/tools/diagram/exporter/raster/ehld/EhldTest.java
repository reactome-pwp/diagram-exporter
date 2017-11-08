package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.RasterExporter;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGSVGElement;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;

public class EhldTest {
	private static final String NAMESPACE = SVGDOMImplementation.SVG_NAMESPACE_URI;

	@Test
	public void testDiagramExporter() {
//		ResourcesFactory.setHost("https://reactomerelease.oicr.on.ca");
		ResourcesFactory.setHost("http://localhost:8080");
		final RasterArgs args = new RasterArgs("R-HSA-449147", "png");
		try {
			args.setFactor(3.);
			RasterExporter.export(args);
		} catch (DiagramJsonNotFoundException | DiagramJsonDeserializationException e) {
			e.printStackTrace();
		} catch (EHLDException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testWithDecorator() {
		ResourcesFactory.setHost("http://localhost:8080");
		final RasterArgs args = new RasterArgs("R-HSA-109581", "png");
		args.setFactor(3.0);
		args.setSelected(Arrays.asList("R-HSA-109606"));
		args.setFlags(Arrays.asList("R-HSA-109606"));
		try {
			RasterExporter.export(args);
		} catch (DiagramJsonNotFoundException | DiagramJsonDeserializationException | EHLDException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testSvgReader1() {
		System.out.println("Apache Batik");
		try {
			final String selected = "R-HSA-109606";
			final String flag = "R-HSA-169911";
			final String parser = XMLResourceDescriptor.getXMLParserClassName();
			final SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			final InputStream resource = getClass().getResourceAsStream("R-HSA-109581.svg");
			final SVGDocument doc = (SVGDocument) f.createDocument(NAMESPACE, resource);
			final SVGSVGElement root = doc.getRootElement();
			final SVGElement selectionFilter = insertSelectionFilter(doc);
			final SVGElement flagFilter = insertFlagFilter(doc);
			final SVGElement flagAndSelectionFilter = combineFilters(doc, flagFilter, selectionFilter, "selection-flag-filter");
			appendFilters(doc, selectionFilter, flagFilter, flagAndSelectionFilter);
//			insertSelectionAndFlagFilter(doc);
			System.out.println(root.getElementsByTagName("title").item(0).getTextContent());
			System.out.printf("%s:%s%n", root.getNodeName(), root.getAttribute("id"));
			final NodeList children = root.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				final Node item = children.item(i);
				if (item.getNodeName().equals("g")) {
					SVGElement element = (SVGElement) item;
					System.out.printf(" - %s:%s%n", item.getNodeName(), element.getAttribute("id"));
					final boolean sel = element.getAttribute("id").equals("REGION-" + selected);
					final boolean fl = element.getAttribute("id").equals("REGION-" + selected);
					if (sel && fl)
						element.setAttribute("style", "filter:url(#selection-flag-filter)");
					else if (sel)
						element.setAttribute("style", "filter:url(#selection-filter)");
					else if (fl)
						element.setAttribute("style", "filter:url(#flag-filter)");
				}
			}

			final String viewBox = root.getAttributes().getNamedItem("viewBox").getNodeValue();
			final Scanner scanner = new Scanner(viewBox);
			scanner.useLocale(Locale.UK);
			float x = scanner.nextFloat();
			float y = scanner.nextFloat();
			float width = scanner.nextFloat();
			float height = scanner.nextFloat();

			System.out.printf("%.0f x %.0f%n", width, height);
			final float factor = 2;
			float fa = 2f;
			final TranscoderInput input = new TranscoderInput(doc);
			final File image = new File("image.png");
			BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
			//			final TranscoderOutput output = new TranscoderOutput(new FileOutputStream(image));
			while (true) {
				transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, width * fa);
				try {
					transcoder.transcode(input, null);
					System.out.println(fa);
				} catch (TranscoderException e) {
					System.out.printf("Max f: %.3f%n", fa);
					final int maxW = (int) (width * (fa - 0.01));
					final int maxH = (int) (height * (fa - 0.01));
					System.out.printf("Max dims: %d x %d (%d)%n", maxW, maxH, maxH * maxW);
//					e.printStackTrace();
					break;
				}
				final BufferedImage bufferedImage = transcoder.getImage();
				ImageIO.write(bufferedImage, "png", image);
				fa += 0.01;
			}

			// To svg file
			SVGTranscoder svgTranscoder = new SVGTranscoder();
			TranscoderOutput textOutput = new TranscoderOutput(new FileWriter("image.svg"));
			svgTranscoder.transcode(input, textOutput);
		} catch (IOException | TranscoderException ex) {
			ex.printStackTrace();
		}
	}

	private SVGElement insertSelectionFilter(SVGDocument root) {
		final Element matrix = root.createElementNS(NAMESPACE, SVGConstants.SVG_FE_COLOR_MATRIX_TAG);
		matrix.setAttribute(SVGConstants.SVG_IN_ATTRIBUTE, SVGConstants.SVG_SOURCE_GRAPHIC_VALUE);
		matrix.setAttribute(SVGConstants.SVG_VALUES_ATTRIBUTE, getMatrix(new Color(0, 0, 255)));
		matrix.setAttribute(SVGConstants.SVG_RESULT_ATTRIBUTE, "selection1");

		final Element morpho = root.createElementNS(NAMESPACE, SVGConstants.SVG_FE_MORPHOLOGY_TAG);
		morpho.setAttribute(SVGConstants.SVG_IN_ATTRIBUTE, "selection1");
		morpho.setAttribute(SVGConstants.SVG_OPERATOR_ATTRIBUTE, SVGConstants.SVG_DILATE_VALUE);
		morpho.setAttribute(SVGConstants.SVG_RADIUS_ATTRIBUTE, "4");
		morpho.setAttribute(SVGConstants.SVG_RESULT_ATTRIBUTE, "selection2");

		final Element merge = root.createElementNS(NAMESPACE, SVGConstants.SVG_FE_COMPOSITE_TAG);
		merge.setAttribute(SVGConstants.SVG_IN_ATTRIBUTE, SVGConstants.SVG_SOURCE_GRAPHIC_VALUE);
		merge.setAttribute(SVGConstants.SVG_IN2_ATTRIBUTE, "selection2");
		merge.setAttribute(SVGConstants.SVG_RESULT_ATTRIBUTE, "selectionResult");

		final SVGElement filter = (SVGElement) root.createElementNS(NAMESPACE, SVGConstants.SVG_FILTER_TAG);
		filter.setId("selection-filter");
		filter.appendChild(matrix);
		filter.appendChild(morpho);
		filter.appendChild(merge);

		return filter;
	}

	private SVGElement insertFlagFilter(SVGDocument root) {
		final Element matrix = root.createElementNS(NAMESPACE, SVGConstants.SVG_FE_COLOR_MATRIX_TAG);
		matrix.setAttribute(SVGConstants.SVG_IN_ATTRIBUTE, SVGConstants.SVG_SOURCE_GRAPHIC_VALUE);
		matrix.setAttribute(SVGConstants.SVG_VALUES_ATTRIBUTE, getMatrix(new Color(255, 0, 255)));
		matrix.setAttribute(SVGConstants.SVG_RESULT_ATTRIBUTE, "flag1");

		final Element morpho = root.createElementNS(NAMESPACE, SVGConstants.SVG_FE_MORPHOLOGY_TAG);
		morpho.setAttribute(SVGConstants.SVG_IN_ATTRIBUTE, "flag1");
		morpho.setAttribute(SVGConstants.SVG_OPERATOR_ATTRIBUTE, SVGConstants.SVG_DILATE_VALUE);
		morpho.setAttribute(SVGConstants.SVG_RADIUS_ATTRIBUTE, "7");
		morpho.setAttribute(SVGConstants.SVG_RESULT_ATTRIBUTE, "flag2");

		final Element merge = root.createElementNS(NAMESPACE, SVGConstants.SVG_FE_COMPOSITE_TAG);
		merge.setAttribute(SVGConstants.SVG_IN_ATTRIBUTE, SVGConstants.SVG_SOURCE_GRAPHIC_VALUE);
		merge.setAttribute(SVGConstants.SVG_IN2_ATTRIBUTE, "flag2");
		merge.setAttribute(SVGConstants.SVG_RESULT_ATTRIBUTE, "flagResult");

		final SVGElement filter = (SVGElement) root.createElementNS(NAMESPACE, SVGConstants.SVG_FILTER_TAG);
		filter.setId("flag-filter");
		filter.appendChild(matrix);
		filter.appendChild(morpho);
		filter.appendChild(merge);

		return filter;
	}

	/**
	 * Creates a new filter by adding a copy of all child of filter1 and filter2
	 * and merging the result of filter1 and filter2. Adds the filter to
	 * root.defs
	 *
	 * @param root    SVG document
	 * @param filter1 first filter
	 * @param filter2 second filter
	 * @param id      id for the filter
	 */
	private SVGElement combineFilters(SVGDocument root, SVGElement filter1, SVGElement filter2, String id) {
		final SVGElement filter = (SVGElement) root.createElementNS(NAMESPACE, SVGConstants.SVG_FILTER_TAG);
		for (int i = 0; i < filter1.getChildNodes().getLength(); i++)
			filter.appendChild(filter1.getChildNodes().item(i).cloneNode(true));
		for (int i = 0; i < filter2.getChildNodes().getLength(); i++)
			filter.appendChild(filter2.getChildNodes().item(i).cloneNode(true));
		final Element merge = root.createElementNS(NAMESPACE, SVGConstants.SVG_FE_MERGE_TAG);
		final Element merge1 = root.createElementNS(NAMESPACE, SVGConstants.SVG_FE_MERGE_NODE_TAG);
		final Element merge2 = root.createElementNS(NAMESPACE, SVGConstants.SVG_FE_MERGE_NODE_TAG);
		final SVGElement last1 = (SVGElement) filter1.getLastChild();
		final SVGElement last2 = (SVGElement) filter2.getLastChild();
		merge1.setAttribute(SVGConstants.SVG_IN_ATTRIBUTE, last1.getAttribute(SVGConstants.SVG_RESULT_ATTRIBUTE));
		merge2.setAttribute(SVGConstants.SVG_IN_ATTRIBUTE, last2.getAttribute(SVGConstants.SVG_RESULT_ATTRIBUTE));
		merge.appendChild(merge1);
		merge.appendChild(merge2);
		filter.appendChild(merge);
		filter.setId(id);
		return filter;
	}

	private void appendFilters(SVGDocument doc, SVGElement... filters) {
		final Node defs = getOrCreateDefs(doc);
		for (SVGElement filter : filters)
			defs.appendChild(filter);
	}

	private Node getOrCreateDefs(SVGDocument doc) {
		final NodeList childNodes = doc.getRootElement().getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node node = childNodes.item(i);
			if (node.getNodeName().equals("defs")) return node;
		}
		final Element defs = doc.createElementNS(NAMESPACE, SVGConstants.SVG_DEFS_TAG);
		doc.getRootElement().appendChild(defs);
		return defs;
	}

	private String getMatrix(Color color) {
		// RR RG RB RA R
		// GR GG GB GA G
		// BR BG BB BA B
		// AR AG AB AA A
		// RG means how much input red to put in output green [0-1]
		final Float[] matrix = {
				0f, 0f, 0f, 0f, color.getRed() / 255.0f,
				0f, 0f, 0f, 0f, color.getGreen() / 255.0f,
				0f, 0f, 0f, 0f, color.getBlue() / 255.0f,
				0f, 0f, 0f, color.getAlpha() / 255.f, 0f // Paints only input shapes
		};
		final List<String> strings = Arrays.stream(matrix)
				.map(value -> String.format(Locale.UK, "%.2f", value))
				.collect(Collectors.toList());
		return String.join(" ", strings);
	}

	public static class BufferedImageTranscoder extends ImageTranscoder {


		private BufferedImage image;

		@Override
		public BufferedImage createImage(int w, int h) {
			return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
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
