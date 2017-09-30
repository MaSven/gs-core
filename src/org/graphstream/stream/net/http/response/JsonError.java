package org.graphstream.stream.net.http.response;

/**
 *
 * @author Sven Marquardt
 * @since 2017-07-28
 */
public class JsonError extends Response {

	/**
	 * @param reason
	 * @param errorCode
	 * @author Sven Marquardt
	 * @since 2017-07-28
	 */
	public JsonError(final String reason, final int errorCode) {
		super(errorCode, reason);
	}

	@Override
	public String toJson() {
		return "{\"errorcode\":" + this.responseCode + ",\"reason\":\"" + this.message + "\"}";
	}

}
