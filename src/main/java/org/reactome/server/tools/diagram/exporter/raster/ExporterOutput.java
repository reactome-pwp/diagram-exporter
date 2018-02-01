package org.reactome.server.tools.diagram.exporter.raster;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.w3c.dom.svg.SVGDocument;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;

public class ExporterOutput {

	public static void save(SVGDocument document, File file) throws IOException, TranscoderException {
		SVGTranscoder transcoder = new SVGTranscoder();
		TranscoderInput input = new TranscoderInput(document);
		FileWriter writer = new FileWriter(file);
		TranscoderOutput output = new TranscoderOutput(writer);
		transcoder.transcode(input, output);
	}

	public static void save(SVGDocument document, OutputStream os) throws TranscoderException {
		SVGTranscoder transcoder = new SVGTranscoder();
		TranscoderInput input = new TranscoderInput(document);
		TranscoderOutput output = new TranscoderOutput(new OutputStreamWriter(os));
		transcoder.transcode(input, output);
	}

	public static void save(SVGDocument document, HttpURLConnection connection) throws TranscoderException, IOException {
		SVGTranscoder transcoder = new SVGTranscoder();
		TranscoderInput input = new TranscoderInput(document);
		OutputStream os = connection.getOutputStream();
		TranscoderOutput output = new TranscoderOutput(new OutputStreamWriter(os));
		transcoder.transcode(input, output);
	}

	public static void save(BufferedImage image, String ext, File file) throws IOException {
		ImageIO.write(image, ext, file);
	}

	public static void save(BufferedImage image, String ext, OutputStream os) throws IOException {
		ImageIO.write(image, ext, os);
	}

	public static void save(BufferedImage image, String ext, HttpURLConnection connection) throws IOException {
		connection.setDoOutput(true);  // your url must support writing
		OutputStream os = connection.getOutputStream();
		ImageIO.write(image, ext, os);
	}
}
