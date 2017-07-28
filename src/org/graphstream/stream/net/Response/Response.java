package org.graphstream.stream.net.Response;

public abstract class Response implements ResponseObject {

	final int responseCode;

	final String message;

	public Response(final int responseCode, final String message) {
		super();
		this.responseCode = responseCode;
		this.message = message;
	}

}
