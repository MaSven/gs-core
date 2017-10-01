package org.graphstream.stream.net.http;
/**
 * Async interface for an Graph
 * @author Sven Marquardt
 *
 */

import java.util.concurrent.CompletableFuture;

import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;

public interface AsyncGraph {
	/**
	 * Get id of the Graph
	 * @return	{@link String}
	 */
	public String getId();
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
	CompletableFuture<Node> getNode();
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
	CompletableFuture<Node> addNode(String id);


}
