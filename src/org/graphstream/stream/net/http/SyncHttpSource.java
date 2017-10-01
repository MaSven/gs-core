package org.graphstream.stream.net.http;

import spark.Route;

public final class SyncHttpSource extends AbstractHttpSource{

	public SyncHttpSource(final String graphId, final int port) {
		super(graphId, port);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Route addNode() {
		return this.addNode;
	}

	@Override
	protected Route addEdge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Route updateNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Route deleteNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Route deleteEdge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Route takeStep() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Route initServer() {
		// TODO Auto-generated method stub
		return null;
	}

}
