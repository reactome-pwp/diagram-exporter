package org.reactome.server.tools.diagram.exporter.raster.api;


import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.util.Set;

/** Arguments for the diagram exporter */
public interface RasterArgs {

	/**
	 * @return diagram stable identifier
	 */
	String getStId();

	/**
	 * @return factor or scale. Resulting diagram dimension will be <em>(width *
	 * factor, height * factor)</em>
	 */
	Double getFactor();

	/**
	 * @return output image format: PNG, JPG, JPEG or GIF. Case insensitive.
	 */
	String getFormat();

	/**
	 * @return analysis token. EXPRESSION or ENRICHMENT supported.
	 */
	String getToken();

	/**
	 * @return list of elements to be flagged: stIds, dbIds, identifiers or gene
	 * names.
	 */
	Set<String> getFlags();

	/**
	 * @return list of elements to be selected: stIds, dbIds, identifiers or
	 * gene names.
	 */
	Set<String> getSelected();

	/**
	 * Names of the color profiles to use.
	 * <p>
	 * <table> <tr> <th>profile</th> <th>values</th> <th>default</th> </tr> <tr>
	 * <td>diagram</td> <td>modern, standard</td> <td>modern</td> </tr> <tr>
	 * <td>analysis</td> <td>standard, copper plus, strosobar</td>
	 * <td>standard</td> </tr> <tr> <td>interactors</td> <td>cyan, teal</td>
	 * <td>cyan</td> </tr> </table>
	 *
	 * @return color profiles (diagram, analysis and interactors)
	 */
	ColorProfiles getProfiles();

	/**
	 * @return for non transparent image formats, color of the background.
	 */
	Color getBackground();

	/**
	 * @return if there is an expression analysis in the token, the column where
	 * to take expression values (0 is the first). If null, first is used (0).
	 * If <strong>format</strong> is <em>gif</em> and <strong>column</strong> is
	 * <em>null</em> then the result will be an animated gif.
	 */
	Integer getColumn();

	String getResource();
}
