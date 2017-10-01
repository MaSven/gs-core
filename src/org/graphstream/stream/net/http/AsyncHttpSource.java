package org.graphstream.stream.net.http;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.http.entity.ContentType;
import org.graphstream.stream.net.http.response.AcceptedResponse;
import org.graphstream.stream.net.http.response.JsonError;
import org.graphstream.stream.net.http.response.Response;
import org.json.JSONException;
import org.json.JSONObject;

import spark.Route;

public class AsyncHttpSource extends AbstractHttpSource{

	/**
	 * URL of the client where updates will be send
	 *
	 * Only used when in Async mode. Each time when a given process finsiched e.g
	 * adding a node, this URL will be called to notify the client about the update.
	 * If this is {@link Optional#empty()} all call are sync processed
	 */
	protected Optional<String> callBackURL = Optional.empty();
	/**
	 *
	 * @param graphId
	 * @param port
	 */
	public AsyncHttpSource(final String graphId, final int port) {
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


	@Override
	protected Route addEdge() {
		return (req,resp)->{
			CompletableFuture.supplyAsync(()->{
				this.sendNodeAdded(this.sourceId, req.params(":id"));

			});
			return resp;
		};
	}

	@Override
	protected Route updateNode() {
		return this.updateNode();
	}

	@Override
	protected Route addNode() {
		return this.addNode;
	}

	@Override
	protected Route deleteNode() {
		return this.deleteNode;
	}

	@Override
	protected Route deleteEdge() {
		return this.deleteEdge;
	}

	@Override
	protected Route takeStep() {
		return this.takeStep;
	}

	@Override
	protected Route initServer() {
		return this.initServer;
	}

}
