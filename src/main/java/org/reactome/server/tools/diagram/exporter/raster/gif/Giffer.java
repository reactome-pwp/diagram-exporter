package org.reactome.server.tools.diagram.exporter.raster.gif;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public class Giffer {

	private final ImageWriter defaultWriter = ImageIO.getImageWritersByFormatName("gif").next();
	private boolean first = true;
	private IIOMetadata metadata;
	private ImageOutputStream os;


	public void start(OutputStream outputStream) throws IOException {
		os = ImageIO.createImageOutputStream(outputStream);
//		defaultWriter.setOutput(outputStream);
	}

	public void addFrame(BufferedImage frame) throws IOException {
		if (first) {
			prepare(frame);
			first = false;
		}
		IIOImage temp = new IIOImage(frame, null, metadata);
		try {
			defaultWriter.writeToSequence(temp, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void prepare(BufferedImage frame) throws IOException {
		// Get the whole metadata tree node, the name is javax_imageio_gif_image_1.0
		// Not sure why I need the ImageTypeSpecifier, but it doesn't work without it
		ImageTypeSpecifier img_type = new ImageTypeSpecifier(frame);
		metadata = defaultWriter.getDefaultImageMetadata(img_type, null);
		String native_format = metadata.getNativeMetadataFormatName();
		IIOMetadataNode node_tree = (IIOMetadataNode) metadata.getAsTree(native_format);

		// Set the delay time you can see the format specification on this page
		// https://docs.oracle.com/javase/7/docs/api/javax/imageio/metadata/doc-files/gif_metadata.html
		IIOMetadataNode graphics_node = getOrCreateNode(node_tree, "GraphicControlExtension");
		graphics_node.setAttribute("delayTime", "100");
		graphics_node.setAttribute("disposalMethod", "none");
		graphics_node.setAttribute("userInputFlag", "FALSE");
		IIOMetadataNode app_extensions = getOrCreateNode(node_tree, "ApplicationExtensions");
		IIOMetadataNode app_node = getOrCreateNode(app_extensions, "ApplicationExtension");
		// Loop
		app_node.setAttribute("applicationID", "NETSCAPE");
		app_node.setAttribute("authenticationCode", "2.0");
		app_node.setUserObject(new byte[]{1, 0, 0});

		app_extensions.appendChild(app_node);
		node_tree.appendChild(app_extensions);

		metadata.setFromTree(native_format, node_tree);
		defaultWriter.setOutput(os);
		defaultWriter.prepareWriteSequence(metadata);
	}

	public void finish() throws IOException {
		defaultWriter.endWriteSequence();
//		os.close();

	}

	private IIOMetadataNode getOrCreateNode(IIOMetadataNode root, String name) {
		for (int i = 0; i < root.getLength(); i++) {
			IIOMetadataNode n = (IIOMetadataNode) root.item(i);
			if (n.getNodeName().equalsIgnoreCase(name))
				return n;
		}
		return createNode(root, name);
//		return IntStream.range(0, root.getLength())
//				.mapToObj(i -> (IIOMetadataNode) root.item(i))
//				.filter(n -> n.getNodeName().equalsIgnoreCase(name))
//				.findAny()
//				.orElse(createNode(root, name));
	}

	private IIOMetadataNode createNode(IIOMetadataNode root, String name) {
		IIOMetadataNode node = new IIOMetadataNode(name);
		root.appendChild(node);
		return node;
	}
}
