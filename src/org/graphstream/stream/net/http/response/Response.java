package org.graphstream.stream.net.http.response;

public abstract class Response implements ResponseObject {

	final int responseCode;

	final String message;

	public Response(final int responseCode, final String message) {
		super();
		this.responseCode = responseCode;
		this.message = message;
	}

}
