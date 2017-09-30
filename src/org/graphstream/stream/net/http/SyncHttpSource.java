package org.graphstream.stream.net.http;

import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.entity.ContentType;
import org.graphstream.stream.net.http.response.AcceptedResponse;
import org.graphstream.stream.net.http.response.JsonError;
import org.graphstream.stream.net.http.response.Response;
import org.json.JSONException;
import org.json.JSONObject;

import spark.Route;

public final class SyncHttpSource extends AbstractHttpSource{

	public SyncHttpSource(final String graphId, final int port) {
		super(graphId, port);
		// TODO Auto-generated constructor stub
	}
	/**
	 * Add {@link #callBackURI} on this server.
	 */
	private final Route initServer = (req, resp) -> {
		final String json = req.body();
		final JSONObject jsonObject = new JSONObject(json);
		try {
			final String url = jsonObject.getString("url");
			this.callBackURL = Optional.of(url);
			final Response response = new AcceptedResponse("Accpeted URL", 200);
			resp.status(202);
			resp.type(ContentType.APPLICATION_JSON.toString());
			resp.body(response.toJson());
		} catch (final JSONException e) {
			this.callBackURL = Optional.empty();
			resp.status(400);
			final JsonError error = new JsonError("Missing Parameter url", 400);
			resp.body(error.toJson());
			resp.type(ContentType.APPLICATION_JSON.toString());
		}

		return resp;
	};
	/**
	 * Add Node
	 */
	private final Route addNode = (req, resp) -> {
		this.sendNodeAdded(this.sourceId, req.params(":id"));
		resp.status(200);
		resp.type("text");
		return resp;
	};
	/**
	 * Delete Node
	 */
	private final Route deleteNode = (req, resp) -> {
		this.sendNodeRemoved(this.sourceId, req.params(":id"));
		resp.status(200);
		resp.type("text");
		return resp;
	};
	/**
	 * Add Edge
	 */
	private final Route addEdge = (req, resp) -> {
		this.sendEdgeAdded(this.sourceId, req.params(":id"), req.params(":from"), req.params(":to"),
				Boolean.getBoolean(req.params("directed")));
		resp.status(200);
		resp.type("text");
		return resp;
	};
	/**
	 * Delete Edge
	 */
	private final Route deleteEdge = (req, resp) -> {
		this.sendEdgeRemoved(this.sourceId, req.params(":id"));
		resp.status(200);
		resp.type("text");
		return resp;
	};
	/**
	 * Take given steps
	 */
	private final Route takeStep = (req, resp) -> {
		if (NumberUtils.isCreatable(req.params(":step"))) {
			this.sendStepBegins(this.sourceId, Double.parseDouble(req.params(":step")));
			resp.status(200);
			resp.type("text");
			return resp;
		}
		resp.status(400);
		return resp;

	};
	@Override
	protected Route addNode() {
		return this.addNode;
	}

}
