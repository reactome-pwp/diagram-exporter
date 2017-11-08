package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import static org.apache.batik.anim.dom.SVGDOMImplementation.SVG_NAMESPACE_URI;
import static org.apache.batik.util.SVGConstants.SVG_CLASS_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_DEFS_TAG;

public class SVGUtil {

	public static void addInlineStyle(SVGDocument document, String aClass, String style) {
		final NodeList styles = document.getElementsByTagName(SVGConstants.SVG_STYLE_TAG);
		if (styles!=null && styles.getLength() > 0) {
			final Node item = styles.item(0);
			item.setTextContent(item.getTextContent() + "." + aClass + style);
		}
	}

	public static void addClass(Element node, String aClass) {
		if (node == null) return;
		final String classAtt = node.getAttribute(SVG_CLASS_ATTRIBUTE);
		if (classAtt == null || classAtt.isEmpty())
			node.setAttribute(SVG_CLASS_ATTRIBUTE, aClass);
		else if (!classAtt.contains(aClass)) {
			node.setAttribute(SVG_CLASS_ATTRIBUTE, classAtt + " " + aClass);
		}
	}

	public static String toURL(String text) {
		return String.format("url(#%s)", text);
	}

	static void appendToDefs(SVGDocument document, Element... elements) {
		final Node defs = getOrCreateDefs(document);
		for (Element element : elements)
			defs.appendChild(element);
	}

	private static Node getOrCreateDefs(SVGDocument document) {
		final NodeList childNodes = document.getRootElement().getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node node = childNodes.item(i);
			if (node.getNodeName().equals("defs")) return node;
		}
		final Element defs = document.createElementNS(SVG_NAMESPACE_URI, SVG_DEFS_TAG);
		document.getRootElement().appendChild(defs);
		return defs;
	}
}
