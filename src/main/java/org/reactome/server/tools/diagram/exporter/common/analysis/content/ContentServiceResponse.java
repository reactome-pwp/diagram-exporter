package org.reactome.server.tools.diagram.exporter.common.analysis.content;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ContentServiceResponse {
	private String stId;

	@JsonIgnore
	private Long dbId;

	@JsonIgnore
	private String displayName;

	@JsonIgnore
	private String schemaClass;

	public String getStId() {
		return stId;
	}

	public Long getDbId() {
		return dbId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getSchemaClass() {
		return schemaClass;
	}
}
