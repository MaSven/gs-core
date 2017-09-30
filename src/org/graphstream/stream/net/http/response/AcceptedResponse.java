package org.graphstream.stream.net.http.response;

/**
 *
 * @author Sven Marquardt
 * @since 2017-07-28
 */
public class AcceptedResponse extends Response {

	public AcceptedResponse(final String message, final int responseCode) {
		super(responseCode, message);

	}

	@Override
	public String toJson() {
		return "{\"responsecode\":" + this.responseCode + ", \"message\":\"" + this.message + "\"}";
	}

}
