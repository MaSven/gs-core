/*
 * Copyright 2006 - 2016
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry    <julien.baudry@graphstream-project.org>
 *     Antoine Dutot    <antoine.dutot@graphstream-project.org>
 *     Yoann Pigné      <yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin   <guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */
package org.graphstream.stream.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;

import org.graphstream.stream.SourceBase;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * This source allows to control a graph from a web browser. Control is done
 * calling the following url :
 * <code>http://host/graphId/edit?q=ACTION&...</code>. ACTION is one of the
 * following action :
 * <ul>
 * <li>an : add node</li>
 * <li>cn : change node</li>
 * <li>dn : delete node</li>
 * <li>ae : add edge</li>
 * <li>ce : change edge</li>
 * <li>de : delete edge</li>
 * <li>cg : change graph</li>
 * <li>st : step begins</li>
 * <li>clear : clear the whole graph</li>
 * </ul>
 * 
 * Each of these actions needs some argument.
 * <dl>
 * <dt>an</dt>
 * <dd>
 * <ul>
 * <li>id</li>
 * </ul>
 * </dd>
 * <dt>cn</dt>
 * <dd>
 * <ul>
 * <li>id</li>
 * <li>key</li>
 * <li>value</li>
 * </ul>
 * </dd>
 * <dt>dn</dt>
 * <dd>
 * <ul>
 * <li>id</li>
 * </ul>
 * </dd>
 * <dt>ae</dt>
 * <dd>
 * <ul>
 * <li>id</li>
 * <li>from</li>
 * <li>to</li>
 * <li>[directed]</li>
 * </ul>
 * </dd>
 * <dt>ce</dt>
 * <dd>
 * <ul>
 * <li>id</li>
 * <li>key</li>
 * <li>value</li>
 * </ul>
 * </dd>
 * <dt>de</dt>
 * <dd>
 * <ul>
 * <li>id</li>
 * </ul>
 * </dd>
 * <dt>cg</dt>
 * <dd>
 * <ul>
 * <li>key</li>
 * <li>value</li>
 * </ul>
 * </dd>
 * <dt>st</dt>
 * <dd>
 * <ul>
 * <li>step</li>
 * </ul>
 * </dd>
 * </dl>
 */
public class HTTPSource extends SourceBase {

	/**
	 * Http server.
	 */
//	protected final HttpServer server;
	
	

	/**
	 * Create a new http source. The source will be available on
	 * 'http://localhost/graphId' where graphId is passed as parameter of this
	 * constructor.
	 * 
	 * @param graphId
	 *            id of the graph
	 * @param port
	 *            port on which server will be bound
	 * @throws IOException
	 *             if server creation failed.
	 */
	public HTTPSource(String graphId) {
		super(String.format("http//%s", graphId));
	}
	
	/**
	 * 
	 * @param nodeId
	 * @see #sendNodeAdded(String, String)
	 */
	protected void sendNodeAdded(final String nodeId) {
		this.sendNodeAdded(this.sourceId, nodeId);
	}
	/**
	 * 
	 * @param edgeId
	 * @param from
	 * @param to
	 * @param directed
	 * @see #sendEdgeAdded(String, String, String, String, boolean)
	 */
	protected void sendEdgeAdded(final String edgeId,final String from,final String to,final boolean directed) {
		this.sendEdgeAdded(this.sourceId, edgeId, from, to, directed);
	}
	
	protected void sendNodeRemoved(final String nodeId) {
		this.sendNodeRemoved(this.sourceId, nodeId);
	}
	
	protected void sendEdgeRemoved(final String edgeId){
		this.sendEdgeRemoved(this.sourceId, edgeId);
	}
	protected void sendStepBegins(final double step) {
		this.sendStepBegins(this.sourceId, step);
	}
}
