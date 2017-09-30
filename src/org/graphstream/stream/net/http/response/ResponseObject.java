package org.graphstream.stream.net.http.response;

public interface ResponseObject {
	/**
	 * Creates String representation of the Object
	 *
	 * @return String as Json
	 * @author Sven Marquardt
	 * @since 2017-07-28
	 */
	String toJson();

}
