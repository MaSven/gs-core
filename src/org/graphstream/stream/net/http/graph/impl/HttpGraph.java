package org.graphstream.stream.net.http.graph.impl;

import java.util.concurrent.CompletableFuture;

import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeFactory;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.NodeFactory;
import org.graphstream.stream.net.http.graph.AsyncGraph;
import org.graphstream.ui.view.Viewer;

public class HttpGraph implements AsyncGraph{

	private final Graph graph;

	public HttpGraph(final Graph graph,final String id) {
		this.graph = graph;
	}



	@Override
	public CompletableFuture<Node> getNode(final String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Edge> getEdge(final String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> isStrict() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> isAutoCreationEnabled() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Double> getStep() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> setNullAttributesAreErrors(final boolean on) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> setNodeFactory(final NodeFactory<? extends Node> nf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> setEdgeFactory(final EdgeFactory<? extends Edge> ef) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> setStrict(final boolean on) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> setAutoCreate(final boolean on) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> clear() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Node> addNode(final String id) throws IdAlreadyInUseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> stepBegins(final double time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Viewer> display(final boolean autoLayout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Node> getNode(final int index) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Edge> getEdge(final int index) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Edge> addEdge(final String id, final Node from, final Node to, final boolean directed)
			throws IdAlreadyInUseException, EdgeRejectedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Edge> removeEdge(final Node node1, final Node node2) throws ElementNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Edge> removeEdge(final Edge edge) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Node> removeNode(final Node node) {
		// TODO Auto-generated method stub
		return null;
	}

}
