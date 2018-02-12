package org.reactome.server.tools.diagram.exporter.common.analysis;

import org.reactome.server.analysis.core.result.utils.TokenUtils;

public class AnalysisClient {
	public static TokenUtils token = null;

	public static void initialise(String tokensFolder) {
		AnalysisClient.token = new TokenUtils(tokensFolder);
	}

}
