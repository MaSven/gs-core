package org.graphstream.stream.net.http.graph;
/**
 * Async interface for an Graph
 * @author Sven Marquardt
 *
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeFactory;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Element;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.NodeFactory;
import org.graphstream.graph.NullAttributeException;
import org.graphstream.graph.Structure;
import org.graphstream.stream.Pipe;
import org.graphstream.ui.view.Viewer;

public interface AsyncGraph extends Element, Pipe,  Structure{
	/**
	 * Get a node by its identifier. This method is implicitly generic and
	 * returns something which extends Node. The return type is the one of the
	 * left part of the assignment. For example, in the following call :
	 * <p>
	 * <pre>
	 * ExtendedNode node = graph.getNode(&quot;...&quot;);
	 * </pre>
	 * <p>
	 * the method will return an ExtendedNode node. If no left part exists,
	 * method will just return a Node.
	 *
	 * @param id Identifier of the node to find.
	 * @return The searched node or null if not found.
	 */
	CompletableFuture<Node> getNode(String id);

	/**
	 * Get an edge by its identifier. This method is implicitly generic and
	 * returns something which extends Edge. The return type is the one of the
	 * left part of the assignment. For example, in the following call :
	 * <p>
	 * <pre>
	 * ExtendedEdge edge = graph.getEdge(&quot;...&quot;);
	 * </pre>
	 * <p>
	 * the method will return an ExtendedEdge edge. If no left part exists,
	 * method will just return an Edge.
	 *
	 * @param id Identifier of the edge to find.
	 * @return The searched edge or null if not found.
	 */
	CompletableFuture<Edge> getEdge(String id);



	/**
	 * Is strict checking enabled? If strict checking is enabled the graph
	 * checks for name space conflicts (e.g. insertion of two nodes with the
	 * same name), removal of non-existing elements, use of non existing
	 * elements (create an edge between two non existing nodes). Graph
	 * implementations are free to respect strict checking or not.
	 *
	 * @return True if enabled.
	 */
	CompletableFuture<Boolean> isStrict();

	/**
	 * Is the automatic creation of missing elements enabled?. If strict
	 * checking is disabled and auto-creation is enabled, when an edge is
	 * created and one or two of its nodes are not already present in the graph,
	 * the nodes are automatically created.
	 *
	 * @return True if enabled.
	 */
	CompletableFuture<Boolean> isAutoCreationEnabled();

	/**
	 * The current step.
	 *
	 * @return The step.
	 */
	CompletableFuture<Double> getStep();

	// Command

	/**
	 * Should a {@link NullAttributeException} be thrown when one tries to
	 * access a non existing attribute, or an attribute whose type is not the
	 * expected one?.
	 *
	 * @param on if true, exceptions will be thrown when accessing a non
	 *           existing attribute.
	 */
	CompletableFuture<Void> setNullAttributesAreErrors(boolean on);

	/**
	 * Set the node factory used to create nodes.
	 *
	 * @param nf the new NodeFactory
	 */
	CompletableFuture<Void> setNodeFactory(NodeFactory<? extends Node> nf);

	/**
	 * Set the edge factory used to create edges.
	 *
	 * @param ef the new EdgeFactory
	 */
	CompletableFuture<Void> setEdgeFactory(EdgeFactory<? extends Edge> ef);

	/**
	 * Enable or disable strict checking.
	 *
	 * @param on True or false.
	 * @see #isStrict()
	 */
	CompletableFuture<Void> setStrict(boolean on);

	/**
	 * Enable or disable the automatic creation of missing elements.
	 *
	 * @param on True or false.
	 * @see #isAutoCreationEnabled()
	 */
	CompletableFuture<Void> setAutoCreate(boolean on);

	// Graph construction

	/**
	 * Empty the graph completely by removing any references to nodes or edges.
	 * Every attribute is also removed. However, listeners are kept.
	 *
	 * @see #clearSinks()
	 */
	CompletableFuture<Void> clear();

	/**
	 * Add a node in the graph.
	 * <p>
	 * This acts as a factory, creating the node instance automatically (and
	 * eventually using the node factory provided). An event is generated toward
	 * the listeners. If strict checking is enabled, and a node already exists
	 * with this identifier, an
	 * {@link org.graphstream.graph.IdAlreadyInUseException} is raised. Else the
	 * error is silently ignored and the already existing node is returned.
	 * </p>
	 * <p>
	 * This method is implicitly generic and returns something which extends
	 * Node. The return type is the one of the left part of the assignment. For
	 * example, in the following call :
	 * <p>
	 * <pre>
	 * ExtendedNode n = graph.addNode(&quot;...&quot;);
	 * </pre>
	 * <p>
	 * the method will return an ExtendedNode. If no left part exists, method
	 * will just return a Node.
	 * </p>
	 *
	 * @param id Arbitrary and unique string identifying the node.
	 * @return The created node (or the already existing node).
	 * @throws IdAlreadyInUseException If strict checking is enabled the identifier is already used.
	 */
	CompletableFuture<Node> addNode(String id) throws IdAlreadyInUseException;

	/**
	 * Adds an undirected edge between nodes.
	 * <p>
	 * <p>
	 * The behavior of this method depends on many conditions. It can be
	 * summarized as follows.
	 * </p>
	 * <p>
	 * <p>
	 * First of all, the method checks if the graph already contains an edge
	 * with the same id. If this is the case and strict checking is enabled,
	 * {@code IdAlreadyInUseException} is thrown. If the strict checking is
	 * disabled the method returns a reference to the existing edge if it has
	 * endpoints {@code node1} and {@code node2} (in the same order if the edge
	 * is directed) or {@code null} otherwise.
	 * </p>
	 * <p>
	 * <p>
	 * In the case when the graph does not contain an edge with the same id, the
	 * method checks if {@code node1} and {@code node2} exist. If one or both of
	 * them do not exist, and strict checking is enabled, {@code
	 * ElementNotFoundException} is thrown. Otherwise if auto-creation is
	 * disabled, the method returns {@code null}. If auto-creation is enabled,
	 * the method creates the missing endpoints.
	 * <p>
	 * <p>
	 * When the edge id is not already in use and the both endpoints exist (or
	 * created), the edge can still be rejected. It may happen for example when
	 * it connects two already connected nodes in a single graph. If the edge is
	 * rejected, the method throws {@code EdgeRejectedException} if strict
	 * checking is enabled or returns {@code null} otherwise. Finally, if the
	 * edge is accepted, it is created using the corresponding edge factory and
	 * a reference to it is returned.
	 * <p>
	 * <p>
	 * An edge creation event is sent toward the listeners. If new nodes are
	 * created, the corresponding events are also sent to the listeners.
	 * </p>
	 * <p>
	 * <p>
	 * This method is implicitly generic and return something which extends
	 * Edge. The return type is the one of the left part of the assignment. For
	 * example, in the following call :
	 * <p>
	 * <pre>
	 * ExtendedEdge e = graph.addEdge(&quot;...&quot;, &quot;...&quot;, &quot;...&quot;);
	 * </pre>
	 * <p>
	 * the method will return an ExtendedEdge. If no left part exists, method
	 * will just return an Edge.
	 * </p>
	 *
	 * @param id    Unique and arbitrary string identifying the edge.
	 * @param node1 The first node identifier.
	 * @param node2 The second node identifier.
	 * @return The newly created edge, an existing edge or {@code null} (see the
	 * detailed description above)
	 * @throws IdAlreadyInUseException  If an edge with the same id already exists and strict
	 *                                  checking is enabled.
	 * @throws ElementNotFoundException If strict checking is enabled, and 'node1' or 'node2' are not
	 *                                  registered in the graph.
	 * @throws EdgeRejectedException    If strict checking is enabled and the edge is not accepted.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	default CompletableFuture<Edge> addEdge(final String id, final String node1, final String node2)
			throws IdAlreadyInUseException, ElementNotFoundException, EdgeRejectedException, InterruptedException, ExecutionException {
		return this.addEdge(id, node1, node2, false);
	}

	/**
	 * Like {@link #addEdge(String, String, String)}, but this edge can be
	 * directed between the two given nodes. If directed, the edge goes in the
	 * 'from' -&gt; 'to' direction. An event is sent toward the listeners.
	 *
	 * @param id       Unique and arbitrary string identifying the edge.
	 * @param from     The first node identifier.
	 * @param to       The second node identifier.
	 * @param directed Is the edge directed?
	 * @return The newly created edge, an existing edge or {@code null} (see the
	 * detailed description in {@link #addEdge(String, String, String)})
	 * @throws IdAlreadyInUseException  If an edge with the same id already exists and strict
	 *                                  checking is enabled.
	 * @throws ElementNotFoundException If strict checking is enabled, and 'node1' or 'node2' are not
	 *                                  registered in the graph.
	 * @throws EdgeRejectedException    If strict checking is enabled and the edge is not accepted.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @see #addEdge(String, String, String)
	 */
	default CompletableFuture<Edge> addEdge(final String id, final String from, final String to, final boolean directed)
			throws IdAlreadyInUseException, ElementNotFoundException, EdgeRejectedException, InterruptedException, ExecutionException {
		Node src = this.getNode(from).get();
		Node dst = this.getNode(to).get();

		if (src == null || dst == null) {
			if (this.isStrict().get()) {
				throw new ElementNotFoundException("Node '%s'", src == null ? from : to);
			}

			if (!this.isAutoCreationEnabled().get()) {
				return null;
			}

			if (src == null) {
				src = this.addNode(from).get();
			}

			if (dst == null) {
				dst = this.addNode(to).get();
			}
		}

		return this.addEdge(id, src, dst, directed);
	}

	/**
	 * <p>
	 * Since dynamic graphs are based on discrete event modifications, the
	 * notion of step is defined to simulate elapsed time between events. So a
	 * step is a event that occurs in the graph, it does not modify it but it
	 * gives a kind of timestamp that allows the tracking of the progress of the
	 * graph over the time.
	 * </p>
	 * <p>
	 * This kind of event is useful for dynamic algorithms that listen to the
	 * dynamic graph and need to measure the time in the graph's evolution.
	 * </p>
	 *
	 * @param time A numerical value that may give a timestamp to track the
	 *             evolution of the graph over the time.
	 */
	CompletableFuture<Void> stepBegins(double time);

	// Source
	// XXX do we put the iterable attributeSinks and elementSinks in Source ?



	// Utility shortcuts (should be mixins or traits, what are you doing Mr Java
	// ?)
	// XXX use a Readable/Writable/Displayable interface for this ?







	/**
	 * Utility method that creates a new graph viewer, and register the graph in
	 * it. Notice that this method is a quick way to see a graph, and only this.
	 * It can be used to prototype a program, but may be limited. This method
	 * automatically launch a graph layout algorithm in its own thread to
	 * compute best node positions.
	 *
	 * @return a graph viewer that allows to command the viewer (it often run in
	 * another thread).
	 * @see org.graphstream.ui.view.Viewer
	 * @see #display(boolean)
	 */
	default CompletableFuture<Viewer> display() {
		return this.display(true);
	}

	/**
	 * Utility method that creates a new graph viewer, and register the graph in
	 * it. Notice that this method is a quick way to see a graph, and only this.
	 * It can be used to prototype a program, but is very limited.
	 *
	 * @param autoLayout If true a layout algorithm is launched in its own thread to
	 *                   compute best node positions.
	 * @return a graph viewer that allows to command the viewer (it often run in
	 * another thread).
	 * @see org.graphstream.ui.view.Viewer
	 * @see #display()
	 */
	CompletableFuture<Viewer> display(boolean autoLayout);

	// New methods

	/**
	 * Get a node by its index. This method is implicitly generic and returns
	 * something which extends Node. The return type is the one of the left part
	 * of the assignment. For example, in the following call :
	 * <p>
	 * <pre>
	 * ExtendedNode node = graph.getNode(index);
	 * </pre>
	 * <p>
	 * the method will return an ExtendedNode node. If no left part exists,
	 * method will just return a Node.
	 *
	 * @param index Index of the node to find.
	 * @return The node with the given index
	 * @throws IndexOutOfBoundsException If the index is negative or greater than {@code
	 *                                   getNodeCount() - 1}.
	 */
	CompletableFuture<Node> getNode(int index) throws IndexOutOfBoundsException;

	/**
	 * Get an edge by its index. This method is implicitly generic and returns
	 * something which extends Edge. The return type is the one of the left part
	 * of the assignment. For example, in the following call :
	 * <p>
	 * <pre>
	 * ExtendedEdge edge = graph.getEdge(index);
	 * </pre>
	 * <p>
	 * the method will return an ExtendedEdge edge. If no left part exists,
	 * method will just return an Edge.
	 *
	 * @param index The index of the edge to find.
	 * @return The edge with the given index
	 * @throws IndexOutOfBoundsException if the index is less than 0 or greater than {@code
	 *                                   getNodeCount() - 1}.
	 */
	CompletableFuture<Edge> getEdge(int index) throws IndexOutOfBoundsException;

	/**
	 * Like {@link #addEdge(String, String, String)} but the nodes are
	 * identified by their indices.
	 *
	 * @param id     Unique and arbitrary string identifying the edge.
	 * @param index1 The first node index
	 * @param index2 The second node index
	 * @return The newly created edge, an existing edge or {@code null}
	 * @throws IndexOutOfBoundsException If node indices are negative or greater than {@code
	 *                                   getNodeCount() - 1}
	 * @throws IdAlreadyInUseException   If an edge with the same id already exists and strict
	 *                                   checking is enabled.
	 * @throws EdgeRejectedException     If strict checking is enabled and the edge is not accepted.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @see #addEdge(String, String, String)
	 */
	default CompletableFuture<Edge> addEdge(final String id, final int index1, final int index2)
			throws IndexOutOfBoundsException, IdAlreadyInUseException, EdgeRejectedException, InterruptedException, ExecutionException {
		return this.addEdge(id, this.getNode(index1).get(), this.getNode(index2).get(), false);
	}

	/**
	 * Like {@link #addEdge(String, String, String, boolean)} but the nodes are
	 * identified by their indices.
	 *
	 * @param id        Unique and arbitrary string identifying the edge.
	 * @param toIndex   The first node index
	 * @param fromIndex The second node index
	 * @param directed  Is the edge directed?
	 * @return The newly created edge, an existing edge or {@code null}
	 * @throws IndexOutOfBoundsException If node indices are negative or greater than {@code
	 *                                   getNodeCount() - 1}
	 * @throws IdAlreadyInUseException   If an edge with the same id already exists and strict
	 *                                   checking is enabled.
	 * @throws EdgeRejectedException     If strict checking is enabled and the edge is not accepted.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @see #addEdge(String, String, String)
	 */
	default CompletableFuture<Edge> addEdge(final String id, final int fromIndex, final int toIndex, final boolean directed)
			throws IndexOutOfBoundsException, IdAlreadyInUseException, EdgeRejectedException, InterruptedException, ExecutionException {
		return this.addEdge(id, this.getNode(fromIndex).get(), this.getNode(toIndex).get(), directed);
	}

	/**
	 * Like {@link #addEdge(String, String, String)} but the node references are
	 * given instead of node identifiers.
	 *
	 * @param id    Unique and arbitrary string identifying the edge.
	 * @param node1 The first node
	 * @param node2 The second node
	 * @return The newly created edge, an existing edge or {@code null}
	 * @throws IdAlreadyInUseException If an edge with the same id already exists and strict
	 *                                 checking is enabled.
	 * @throws EdgeRejectedException   If strict checking is enabled and the edge is not accepted.
	 * @see #addEdge(String, String, String)
	 */
	default CompletableFuture<Edge> addEdge(final String id, final Node node1, final Node node2)
			throws IdAlreadyInUseException, EdgeRejectedException {
		return this.addEdge(id, node1, node2, false);
	}

	/**
	 * Like {@link #addEdge(String, String, String, boolean)} but the node
	 * references are given instead of node identifiers.
	 *
	 * @param id       Unique and arbitrary string identifying the edge.
	 * @param from     The first node
	 * @param to       The second node
	 * @param directed Is the edge directed?
	 * @return The newly created edge, an existing edge or {@code null}
	 * @throws IdAlreadyInUseException If an edge with the same id already exists and strict
	 *                                 checking is enabled.
	 * @throws EdgeRejectedException   If strict checking is enabled and the edge is not accepted.
	 * @see #addEdge(String, String, String)
	 */
	CompletableFuture<Edge> addEdge(String id, Node from, Node to, boolean directed)
			throws IdAlreadyInUseException, EdgeRejectedException;

	/**
	 * Removes an edge with a given index. An event is sent toward the
	 * listeners.
	 * <p>
	 * <p>
	 * This method is implicitly generic and returns something which extends
	 * Edge. The return type is the one of the left part of the assignment. For
	 * example, in the following call :
	 * <p>
	 * <pre>
	 * ExtendedEdge edge = graph.removeEdge(i);
	 * </pre>
	 * <p>
	 * the method will return an ExtendedEdge edge. If no left part exists,
	 * method will just return an Edge.
	 * </p>
	 *
	 * @param index The index of the edge to be removed.
	 * @return The removed edge
	 * @throws IndexOutOfBoundsException if the index is negative or greater than {@code
	 *                                   getEdgeCount() - 1}
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	default CompletableFuture<Edge> removeEdge(final int index) throws IndexOutOfBoundsException, InterruptedException, ExecutionException {
		final Edge edge = this.getEdge(index).get();

		if (edge == null) {
			if (this.isStrict().get()) {
				throw new ElementNotFoundException("Edge #" + index);
			}

			return null;
		}

		return this.removeEdge(edge);
	}

	/**
	 * Removes an edge between two nodes. Like
	 * {@link #removeEdge(String, String)} but the nodes are identified by their
	 * indices.
	 *
	 * @param fromIndex the index of the source node
	 * @param toIndex   the index of the target node
	 * @return the removed edge or {@code null} if no edge is removed
	 * @throws IndexOutOfBoundsException If one of the node indices is negative or greater than
	 *                                   {@code getNodeCount() - 1}.
	 * @throws ElementNotFoundException  if strict checking is enabled and there is no edge between
	 *                                   the two nodes.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @see #removeEdge(String, String)
	 */
	default CompletableFuture<Edge> removeEdge(final int fromIndex, final int toIndex) throws IndexOutOfBoundsException, ElementNotFoundException, InterruptedException, ExecutionException {
		final Node fromNode = this.getNode(fromIndex).get();
		final Node toNode = this.getNode(toIndex).get();

		if (fromNode == null || toNode == null) {
			if (this.isStrict().get()) {
				throw new ElementNotFoundException("Node #%d", fromNode == null ? fromIndex : toIndex);
			}

			return null;
		}

		return this.removeEdge(fromNode, toNode);
	}

	/**
	 * Removes an edge between two nodes. Like
	 * {@link #removeEdge(String, String)} but node references are given instead
	 * of node identifiers.
	 *
	 * @param node1 the first node
	 * @param node2 the second node
	 * @return the removed edge or {@code null} if no edge is removed
	 * @throws ElementNotFoundException if strict checking is enabled and there is no edge between
	 *                                  the two nodes.
	 * @see #removeEdge(String, String)
	 */
	CompletableFuture<Edge> removeEdge(Node node1, Node node2)
			throws ElementNotFoundException;

	/**
	 * Remove an edge given the identifiers of its two endpoints.
	 * <p>
	 * If the edge is directed it is removed only if its source and destination
	 * nodes are identified by 'from' and 'to' respectively. If the graph is a
	 * multi-graph and there are several edges between the two nodes, one of the
	 * edges at random is removed. An event is sent toward the listeners. If
	 * strict checking is enabled and at least one of the two given nodes does
	 * not exist or if they are not connected, a not found exception is raised.
	 * Else the error is silently ignored, and null is returned.
	 * </p>
	 * <p>
	 * This method is implicitly generic and return something which extends
	 * Edge. The return type is the one of the left part of the assignment. For
	 * example, in the following call :
	 * <p>
	 * <pre>
	 * ExtendedEdge e = graph.removeEdge(&quot;...&quot;, &quot;...&quot;);
	 * </pre>
	 * <p>
	 * the method will return an ExtendedEdge. If no left part exists, method
	 * will just return an Edge.
	 * </p>
	 *
	 * @param from The origin node identifier to select the edge.
	 * @param to   The destination node identifier to select the edge.
	 * @return The removed edge, or null if strict checking is disabled and at
	 * least one of the two given nodes does not exist or there is no
	 * edge between them
	 * @throws ElementNotFoundException If the 'from' or 'to' node is not registered in the graph or
	 *                                  not connected and strict checking is enabled.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	default CompletableFuture<Edge> removeEdge(final String from, final String to) throws ElementNotFoundException, InterruptedException, ExecutionException {
		final Node fromNode = this.getNode(from).get();
		final Node toNode = this.getNode(to).get();

		if (fromNode == null || toNode == null) {
			if (this.isStrict().get()) {
				throw new ElementNotFoundException("Node \"%s\"", fromNode == null ? from : to);
			}

			return null;
		}

		return this.removeEdge(fromNode, toNode);
	}

	/**
	 * Removes an edge knowing its identifier. An event is sent toward the
	 * listeners. If strict checking is enabled and the edge does not exist,
	 * {@code ElementNotFoundException} is raised. Otherwise the error is
	 * silently ignored and null is returned.
	 * <p>
	 * This method is implicitly generic and returns something which extends
	 * Edge. The return type is the one of the left part of the assignment. For
	 * example, in the following call :
	 * <p>
	 * <pre>
	 * ExtendedEdge e = graph.removeEdge(&quot;...&quot;);
	 * </pre>
	 * <p>
	 * the method will return an ExtendedEdge. If no left part exists, method
	 * will just return an Edge.
	 * </p>
	 *
	 * @param id Identifier of the edge to remove.
	 * @return The removed edge, or null if strict checking is disabled and the
	 * edge does not exist.
	 * @throws ElementNotFoundException If no edge matches the identifier and strict checking is
	 *                                  enabled.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	default CompletableFuture<Edge> removeEdge(final String id) throws ElementNotFoundException, InterruptedException, ExecutionException {
		final Edge edge = this.getEdge(id).get();

		if (edge == null) {
			if (this.isStrict().get()) {
				throw new ElementNotFoundException("Edge \"" + id + "\"");
			}

			return null;
		}

		return this.removeEdge(edge);
	}

	/**
	 * Removes an edge. An event is sent toward the listeners.
	 * <p>
	 * This method is implicitly generic and returns something which extends
	 * Edge. The return type is the one of the left part of the assignment. For
	 * example, in the following call :
	 * <p>
	 * <pre>
	 * ExtendedEdge e = graph.removeEdge(...);
	 * </pre>
	 * <p>
	 * the method will return an ExtendedEdge. If no left part exists, method
	 * will just return an Edge.
	 * </p>
	 *
	 * @param edge The edge to be removed
	 * @return The removed edge
	 */
	CompletableFuture<Edge> removeEdge(Edge edge);

	/**
	 * Removes a node with a given index.
	 * <p>
	 * An event is generated toward the listeners. Note that removing a node may
	 * remove all edges it is connected to. In this case corresponding events
	 * will also be generated toward the listeners.
	 * </p>
	 * <p>
	 * This method is implicitly generic and return something which extends
	 * Node. The return type is the one of the left part of the assignment. For
	 * example, in the following call :
	 * <p>
	 * <pre>
	 * ExtendedNode n = graph.removeNode(index);
	 * </pre>
	 * <p>
	 * the method will return an ExtendedNode. If no left part exists, method
	 * will just return a Node.
	 * </p>
	 *
	 * @param index The index of the node to be removed
	 * @return The removed node
	 * @throws IndexOutOfBoundsException if the index is negative or greater than {@code
	 *                                   getNodeCount() - 1}.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	default CompletableFuture<Node> removeNode(final int index) throws IndexOutOfBoundsException, InterruptedException, ExecutionException {
		final Node node = this.getNode(index).get();

		if (node == null) {
			if (this.isStrict().get()) {
				throw new ElementNotFoundException("Node #" + index);
			}

			return null;
		}

		return this.removeNode(node);
	}

	/**
	 * Remove a node using its identifier.
	 * <p>
	 * An event is generated toward the listeners. Note that removing a node may
	 * remove all edges it is connected to. In this case corresponding events
	 * will also be generated toward the listeners.
	 * </p>
	 * <p>
	 * This method is implicitly generic and return something which extends
	 * Node. The return type is the one of the left part of the assignment. For
	 * example, in the following call :
	 * <p>
	 * <pre>
	 * ExtendedNode n = graph.removeNode(&quot;...&quot;);
	 * </pre>
	 * <p>
	 * the method will return an ExtendedNode. If no left part exists, method
	 * will just return a Node.
	 * </p>
	 *
	 * @param id The unique identifier of the node to remove.
	 * @return The removed node. If strict checking is disabled, it can return
	 * null if the node to remove does not exist.
	 * @throws ElementNotFoundException If no node matches the given identifier and strict checking
	 *                                  is enabled.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	default CompletableFuture<Node> removeNode(final String id) throws ElementNotFoundException, InterruptedException, ExecutionException {
		final Node node = this.getNode(id).get();

		if (node == null) {
			if (this.isStrict().get()) {
				throw new ElementNotFoundException("Node \"" + id + "\"");
			}

			return null;
		}

		return this.removeNode(node);
	}

	/**
	 * Removes a node.
	 * <p>
	 * An event is generated toward the listeners. Note that removing a node may
	 * remove all edges it is connected to. In this case corresponding events
	 * will also be generated toward the listeners.
	 * </p>
	 * <p>
	 * This method is implicitly generic and return something which extends
	 * Node. The return type is the one of the left part of the assignment. For
	 * example, in the following call :
	 * <p>
	 * <pre>
	 * ExtendedNode n = graph.removeNode(...);
	 * </pre>
	 * <p>
	 * the method will return an ExtendedNode. If no left part exists, method
	 * will just return a Node.
	 * </p>
	 *
	 * @param node The node to be removed
	 * @return The removed node
	 */
	CompletableFuture<Node> removeNode(Node node);




}
