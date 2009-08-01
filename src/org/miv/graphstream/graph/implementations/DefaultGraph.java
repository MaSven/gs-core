/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2006 - 2009
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */

package org.miv.graphstream.graph.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.EdgeFactory;
import org.miv.graphstream.graph.Element;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.GraphAttributesListener;
import org.miv.graphstream.graph.GraphElementsListener;
import org.miv.graphstream.graph.GraphListener;
import org.miv.graphstream.graph.Node;
import org.miv.graphstream.graph.NodeFactory;
import org.miv.graphstream.io.GraphParseException;
import org.miv.graphstream.io2.file.FileInput;
import org.miv.graphstream.io2.file.FileInputFactory;
import org.miv.graphstream.io2.file.FileOutput;
import org.miv.graphstream.io2.file.FileOutputFactory;
import org.miv.graphstream.ui.GraphViewer;
import org.miv.graphstream.ui.GraphViewerRemote;
import org.miv.util.NotFoundException;
import org.miv.util.SingletonException;

/**
 * DefaultGraph.
 * 
 * <p>
 * A graph is a set of graph {@link org.miv.graphstream.graph.Element}s. Graph
 * elements can be nodes (descendants of {@link org.miv.graphstream.graph.Node}),
 * edges (descendants of {@link org.miv.graphstream.graph.Edge}).
 * </p>
 * 
 * <p>
 * A graph contains a set of nodes and edges indexed by their names (id), as
 * well as a set of listeners that are called each time something occurs in the
 * graph:
 * <ul>
 * <li>Topological changes (nodes and edges added or removed);</li>
 * <li>Attributes changes.</li>
 * </ul>.
 * </p>
 * 
 * <p>
 * Nodes have to be added using the {@link #addNode(String)} method. Identically
 * edges are added using the {@link #addEdge(String,String,String,boolean)}
 * method. This allows the graph to work as a factory creating edges and nodes
 * implementation that suit its needs.
 * </p>
 * 
 * <p>
 * This graph tries to ensure its consistency at any time. For this, it:
 * <ul>
 * <li>automatically deletes edges connected to a node when this one disappears;</li>
 * <li>automatically updates the node when an edge disappears;</li>
 * </ul>
 * </p>
 * 
 * @see org.miv.graphstream.graph.GraphListener
 * @see org.miv.graphstream.graph.GraphAttributesListener
 * @see org.miv.graphstream.graph.GraphElementsListener
 * @see org.miv.graphstream.graph.implementations.DefaultNode
 * @see org.miv.graphstream.graph.implementations.DefaultEdge
 * @see org.miv.graphstream.graph.implementations.AbstractElement
 */
public class DefaultGraph extends AbstractElement implements Graph
{
	/**
	 * Set of nodes indexed by their id.
	 */
	protected HashMap<String,Node> nodes = new HashMap<String,Node>();

	/**
	 * Set of edges indexed by their id.
	 */
	protected HashMap<String,Edge> edges = new HashMap<String,Edge>();

	/**
	 * Set of graph attributes listeners.
	 */
	protected HashSet<GraphAttributesListener> attrListeners = new HashSet<GraphAttributesListener>();
	
	/**
	 * Set of graph elements listeners.
	 */
	protected HashSet<GraphElementsListener> eltsListeners = new HashSet<GraphElementsListener>();

	/**
	 * Verify name space conflicts, removal of non-existing elements, use of
	 * non-existing elements.
	 */
	protected boolean strictChecking = true;
	
	/**
	 * Automatically create missing elements. For example, if an edge is created
	 * between two non-existing nodes, create the nodes.
	 */
	protected boolean autoCreate = false;
	
	/**
	 * A queue that allow the management of events (nodes/edge add/delete/change) in the right order. 
	 */
	protected LinkedList<GraphEvent> eventQueue = new LinkedList<GraphEvent>();
	
	/**
	 * A boolean that indicates whether or not an GraphListener event is being sent during another one. 
	 */
	protected boolean eventProcessing = false;
	
	/**
	 * List of listeners to remove if the {@link #removeGraphListener(GraphListener)} is called
	 * inside from the listener. This can happen !! We create this list on demand.
	 */
	protected ArrayList<Object> listenersToRemove;
	
	/**
	 *  Helpful class that dynamically instantiate nodes according to a given class name.
	 */
	protected NodeFactory nodeFactory;
	
	/**
	 *  Helpful class that dynamically instantiate edges according to a given class name.
	 */
	protected EdgeFactory edgeFactory;
	
	
// Constructors

	/**
	 * New empty graph, with the empty string as default identifier.
	 * @see #DefaultGraph(String)
	 * @see #DefaultGraph(boolean, boolean)
	 * @see #DefaultGraph(String, boolean, boolean) 
	 */
	public DefaultGraph()
	{
		this( "" );
	}
	
	/**
	 * New empty graph.
	 * @param id Unique identifier of the graph.
	 * @see #DefaultGraph(boolean, boolean)
	 * @see #DefaultGraph(String, boolean, boolean)
	 */
	public DefaultGraph( String id )
	{
		this( id, true, false );
	}

	/**
	 * New empty graph, with the empty string as default identifier.
	 * @param strictChecking If true any non-fatal error throws an exception.
	 * @param autoCreate If true (and strict checking is false), nodes are
	 *        automatically created when referenced when creating a edge, even
	 *        if not yet inserted in the graph.
	 * @see #DefaultGraph(String, boolean, boolean)
	 * @see #setStrict(boolean)
	 * @see #setAutoCreate(boolean)
	 */
	public DefaultGraph( boolean strictChecking, boolean autoCreate )
	{
		this( "", strictChecking, autoCreate );
	}
	
	/**
	 * New empty graph.
	 * @param id Unique identifier of this graph.
	 * @param strictChecking If true any non-fatal error throws an exception.
	 * @param autoCreate If true (and strict checking is false), nodes are
	 *        automatically created when referenced when creating a edge, even
	 *        if not yet inserted in the graph.
	 * @see #setStrict(boolean)
	 * @see #setAutoCreate(boolean)
	 */
	public DefaultGraph( String id, boolean strictChecking, boolean autoCreate )
	{
		super( id );
		setStrict( strictChecking );
		setAutoCreate( autoCreate );
		
		// Factories that dynamically create nodes and edges.

		nodeFactory = new NodeFactory()
		{
			public Node newInstance( String id, Graph graph )
			{
				return new SingleNode(graph,id);
			}
		};

		edgeFactory = new EdgeFactory()
		{
			public Edge newInstance( String id, Node src, Node trg, boolean  directed )
			{
				return new SingleEdge(id,src,trg, directed);
			}
		};
		
	}

// Access -- Nodes

	/**
	 * @complexity O(1).
	 */
	public Node getNode( String id )
	{
		return nodes.get( id );
	}
	
	/**
	 * @complexity O(1)
	 */
	public Edge getEdge( String id )
	{
		return edges.get( id );
	}

	/**
	 * @complexity O(1)
	 */
	public int getNodeCount()
	{
		return nodes.size();
	}

	/**
	 * @complexity O(1)
	 */
	public int getEdgeCount()
	{
		return edges.size();
	}

	/**
	 * @complexity O(1)
	 */
	public Iterator<Node> getNodeIterator()
	{
		return new ElementIterator<Node>( this, nodes, true );
	}
	
	public Iterator<Node> iterator()
	{
		return new ElementIterator<Node>( this, nodes, true );
	}

	/**
	 * @complexity O(1)
	 */
	public Iterator<Edge> getEdgeIterator()
	{
		return new ElementIterator<Edge>( this, edges, false );
	}
	
	/**
	 * @complexity O(1)
	 */
	public Iterable<Node> nodeSet()
	{
		return nodes.values();
	}

	/**
	 * @complexity O(1)
	 */
	public Iterable<Edge> edgeSet()
	{
		return edges.values();
	}

	public EdgeFactory edgeFactory()
	{
		return edgeFactory;
	}
	
	public void setEdgeFactory( EdgeFactory ef )
	{
		this.edgeFactory = ef;
	}

	public NodeFactory nodeFactory()
	{
		return nodeFactory;
	}
	
	public void setNodeFactory( NodeFactory nf )
	{
		this.nodeFactory = nf;
	}

// Commands

	/**
	 * @complexity O(1)
	 */
	public void clear()
	{
		for( GraphElementsListener listener: eltsListeners )
			listener.graphCleared( getId() );
		
		nodes.clear();
		edges.clear();
		clearAttributes();
	}
	
	/**
	 * @complexity O(1)
	 */
	public void clearListeners()
	{
		eltsListeners.clear();
		attrListeners.clear();
	}
	
	public boolean isStrict()
	{
		return strictChecking;
	}
	
	public boolean isAutoCreationEnabled()
	{
		return autoCreate;
	}

	public Iterable<GraphAttributesListener> getGraphAttributesListeners()
	{
		return attrListeners;
	}
	
	public Iterable<GraphElementsListener> getGraphElementsListeners()
	{
		return eltsListeners;
	}

// Commands -- Nodes

	public void setStrict( boolean on )
	{
		strictChecking = on;
	}
	
	public void setAutoCreate( boolean on )
	{
		autoCreate = on;
	}
	
	protected Node addNode_( String tag ) throws SingletonException
	{
		Node n = nodes.get( tag );
		
		if( n == null )	// Avoid recursive calls when synchronising graphs.
		{
			DefaultNode node = (DefaultNode) nodeFactory.newInstance(tag,this);
			DefaultNode old  = (DefaultNode) nodes.put( tag, node );
			
			n = node;

			assert( old == null );
			
			nodes.put( tag, node );
			afterNodeAddEvent( node );
		}
		else if( strictChecking )
		{
			throw new SingletonException( "id '" + tag + "' already used, cannot add node" );
		}

		return n;
		
//		DefaultNode node = (DefaultNode) nodeFactory.newInstance(tag,this);
//		DefaultNode old  = (DefaultNode) nodes.put( tag, node );
//
//		if( old != null  )
//		{
//			nodes.put( tag, old );
//			
//			if( strictChecking )
//			{
//				throw new SingletonException( "id '"+tag+
//						"' already used, cannot add node" );
//			}
//			else
//			{
//				node = old;
//			}
//		}
//		else
//		{
//			afterNodeAddEvent( (DefaultNode)node );
//		}
//
//		return (DefaultNode)node;
	}
	

	/**
	 * @complexity O(1)
	 */
	public Node addNode( String id ) throws SingletonException
	{
		return addNode_( id ) ; 
	}
	
	protected Node removeNode_( String tag, boolean fromNodeIterator ) throws NotFoundException
	{
		// The fromNodeIterator flag allows to know if this remove node call was
		// made from inside a node iterator or not. If from a node iterator,
		// we must not remove the node from the nodes set, this is done by the
		// iterator.
		
		DefaultNode node = (DefaultNode) nodes.get( tag );
		
		if( node != null )
		{
			node.disconnectAllEdges();
			beforeNodeRemoveEvent( node );
			
			if( ! fromNodeIterator )
				nodes.remove( tag );
			
			return node;
		}

		if( strictChecking )
			throw new NotFoundException( "node id '"+tag+"' not found, cannot remove" );
		
		return null;
	}

	/**
	 * @complexity O(1)
	 */
	public Node removeNode( String id ) throws NotFoundException
	{
		return removeNode_( id, false );
	}

	protected Edge addEdge_( String tag, String from, String to, boolean directed )
		throws SingletonException, NotFoundException
	{
		Node src;
		Node trg;

		src = nodes.get( from );
		trg = nodes.get( to );

		if( src == null )
		{
			if( strictChecking )
			{
				throw new NotFoundException( "cannot make edge from '"
						+from+"' to '"+to+"' since node '"
						+from+"' is not part of this graph" );
			}
			else if( autoCreate )
			{
				src = addNode( from );
			}
		}

		if( trg == null )
		{
			if( strictChecking )
			{
				throw new NotFoundException( "cannot make edge from '"
						+from+"' to '"+to+"' since node '"+to
						+"' is not part of this graph" );
			}
			else if( autoCreate )
			{
				trg = addNode( to );
			}
		}

		if( src != null && trg != null )
		{
			Edge e = edges.get( tag );
			
			if( e == null )	// Avoid recursive calls when synchronising graphs.
			{
				DefaultEdge edge = (DefaultEdge) ((DefaultNode)src).addEdgeToward( tag, (DefaultNode)trg, directed );
				edges.put( edge.getId(), (DefaultEdge) edge );
				e = edge;
			}
			else if( strictChecking )
			{
				throw new SingletonException( "cannot add edge '" + tag + "', identifier already exists" );
			}
			
			return e;
		}
		
		return null;
	}

	/**
	 * @complexity O(1)
	 */
	public Edge addEdge( String id, String node1, String node2 )
		throws SingletonException, NotFoundException
	{
		return addEdge( id, node1, node2, false );
	}
	
	/**
	 * @complexity O(1)
	 */
	public Edge addEdge( String id, String from, String to, boolean directed )
		throws SingletonException, NotFoundException
	{
		Edge edge = addEdge_( id, from, to, directed );
		// An explanation for this strange "if": in the SingleGraph implementation
		// when a directed edge between A and B is added with id AB, if a second
		// directed edge between B and A is added with id BA, the second edge is
		// erased, its id is not remembered and the edge with id AB is transformed
		// in undirected edge. Therefore, sometimes the id is not the same as the
		// edge.getId(). Nevertheless only one edge exists and so no event must
		// be generated.
		// TODO: this strange behaviour should disappear ! Adding BA should cause
		// an error. Use changeOrientation instead.
		if( edge.getId().equals( id ) )
			afterEdgeAddEvent( edge );
		return edge;
	}

	/**
	 * @complexity O(1)
	 */
	public Edge removeEdge( String from, String to )
		throws NotFoundException
	{
		try
		{
			Node src = nodes.get( from );
			Node trg = nodes.get( to );

			if( src == null )
			{
				if( strictChecking )
					throw new NotFoundException( "error while removing edge '"
							+from+"->"+to+"' node '"+from+"' cannot be found" );
			}

			if( trg == null )
			{
				if( strictChecking )
					throw new NotFoundException( "error while removing edge '"
							+from+"->"+to+"' node '"+to+"' cannot be found" );
			}

			Edge edge = null;
			
			if( src != null && trg != null )
			{
				edge = src.getEdgeToward( to );
			}

			if( edge != null )
			{
				// We cannot execute the edge remove event here since edges, at
				// the contrary of other elements can disappear automatically
				// when the nodes that is linked by them disappear.
				//		beforeEdgeRemoveEvent( edge );

				edges.remove( ( (AbstractElement) edge ).getId() );
				((DefaultEdge)edge).unbind();

				return edge;
			}
		}
		catch( IllegalStateException e )
		{
			if( strictChecking )
				throw new NotFoundException(
						"illegal edge state while removing edge between nodes '"
						+from+"' and '"+to+"'" );
		}

		if( strictChecking )
			throw new NotFoundException( "no edge between nodes '"
					+from+"' and '"+to+"'" );
	
		return null;
	}

	/**
	 * @complexity O(1)
	 */
	public Edge removeEdge( String id ) throws NotFoundException
	{
		return removeEdge_( id, false );
	}
	
	protected Edge removeEdge_( String id, boolean fromEdgeIterator )
	{
		try
		{
			DefaultEdge edge = null;
			
			if( fromEdgeIterator )
			     edge = (DefaultEdge) edges.get( id );
			else edge = (DefaultEdge) edges.remove( id );

			if( edge != null )
			{
				edge.unbind();
				return edge;
			}
		}
		catch( IllegalStateException e )
		{
			if( strictChecking )
				throw new NotFoundException( "illegal edge state while removing edge '"+id+"'" );
		}

		if( strictChecking )
			throw new NotFoundException( "edge '"+id+"' does not exist, cannot remove" );
		
		return null;
	}

	public void stepBegins(double time)
	{
		for( GraphElementsListener l : eltsListeners )
			l.stepBegins( getId(), time );
	}
	
// Events

	public void addGraphListener( GraphListener listener )
	{
		attrListeners.add( listener );
		eltsListeners.add( listener );
	}
	
	public void addGraphAttributesListener( GraphAttributesListener listener )
	{
		attrListeners.add( listener );
	}
	
	public void addGraphElementsListener( GraphElementsListener listener )
	{
		eltsListeners.add( listener );
	}

	public void removeGraphListener( GraphListener listener )
	{
		if( eventProcessing )
		{
			// We cannot remove the listener while processing events !!!
			removeListenerLater( listener );
		}
		else
		{
			attrListeners.remove( listener );
			eltsListeners.remove( listener );
		}
	}
	
	public void removeGraphAttributesListener( GraphAttributesListener listener )
	{
		if( eventProcessing )
		{
			// We cannot remove the listener while processing events !!!
			removeListenerLater( listener );
		}
		else
		{
			attrListeners.remove( listener );
		}		
	}
	
	public void removeGraphElementsListener( GraphElementsListener listener )
	{
		if( eventProcessing )
		{
			// We cannot remove the listener while processing events !!!
			removeListenerLater( listener );
		}
		else
		{
			eltsListeners.remove( listener );
		}
	}
	
	protected void removeListenerLater( Object listener )
	{
		if( listenersToRemove == null )
			listenersToRemove = new ArrayList<Object>();
		
		listenersToRemove.add( listener );	
	}
	
	protected void checkListenersToRemove()
	{
		if( listenersToRemove != null && listenersToRemove.size() > 0 )
		{
			for( Object listener: listenersToRemove )
			{
				if( listener instanceof GraphListener )
					removeGraphListener( (GraphListener) listener );
				else if( listener instanceof GraphAttributesListener )
					removeGraphAttributesListener( (GraphAttributesListener) listener );
				else if( listener instanceof GraphElementsListener )
					removeGraphElementsListener( (GraphElementsListener) listener );
			}

			listenersToRemove.clear();
			listenersToRemove = null;
		}
	}

	/**
	 * If in "event processing mode", ensure all pending events are processed.
	 */
	protected void manageEvents()
	{
		if( eventProcessing )
		{
			while( ! eventQueue.isEmpty() )
				manageEvent( eventQueue.remove() );
		}
	}

	protected void afterNodeAddEvent( Node node )
	{
		if( ! eventProcessing )
		{
			eventProcessing = true;
			manageEvents();

			for( GraphElementsListener l: eltsListeners )
				l.nodeAdded( getId(), node.getId() );

			manageEvents();
			eventProcessing = false;
			checkListenersToRemove();
		}
		else 
		{
			eventQueue.add( new AfterNodeAddEvent(node) );
		}
	}

	protected void beforeNodeRemoveEvent( Node node )
	{
		if( ! eventProcessing )
		{
			eventProcessing = true;
			manageEvents();

			for( GraphElementsListener l: eltsListeners )
				l.nodeRemoved( getId(), node.getId() );

			manageEvents();
			eventProcessing = false;
			checkListenersToRemove();
		}
		else 
		{
			eventQueue.add( new BeforeNodeRemoveEvent( node ) );
		}
	}

	protected void afterEdgeAddEvent( Edge edge )
	{
		if( ! eventProcessing )
		{
			eventProcessing = true;
			manageEvents();

			for( GraphElementsListener l: eltsListeners )
				l.edgeAdded( getId(), edge.getId(), edge.getNode0().getId(), edge.getNode1().getId(), edge.isDirected() );

			manageEvents();
			eventProcessing = false;
			checkListenersToRemove();
		}
		else 
		{
//			printPosition( "AddEdge in EventProc" );
			eventQueue.add( new AfterEdgeAddEvent(edge) );
		}
	}

	protected void beforeEdgeRemoveEvent( Edge edge )
	{
		if( ! eventProcessing )
		{
			eventProcessing = true;
			manageEvents();

			for( GraphElementsListener l: eltsListeners )
				l.edgeRemoved( getId(), edge.getId() );

			manageEvents();
			eventProcessing = false;
			checkListenersToRemove();
		}
		else {
//			printPosition( "DelEdge in EventProc" );
			eventQueue.add( new BeforeEdgeRemoveEvent( edge ) );
		}
	}

	@Override
	protected void attributeChanged( String attribute, AttributeChangeEvent event, Object oldValue, Object newValue )
	{
		attributeChangedEvent( this, attribute, event, oldValue, newValue );
	}

	protected void attributeChangedEvent( Element element, String attribute, AttributeChangeEvent event, Object oldValue, Object newValue )
	{
		if( ! eventProcessing )
		{
			eventProcessing = true;
			manageEvents();

			if( event == AttributeChangeEvent.ADD )
			{
				if( element instanceof Node )
				{
					for( GraphAttributesListener l: attrListeners )
						l.nodeAttributeAdded( getId(), element.getId(), attribute, newValue );
				}
				else if( element instanceof Edge )
				{
					for( GraphAttributesListener l: attrListeners )
						l.edgeAttributeAdded( getId(), element.getId(), attribute, newValue );
				}
				else
				{
					for( GraphAttributesListener l: attrListeners )
						l.graphAttributeAdded( getId(), attribute, newValue );					
				}
			}
			else if( event == AttributeChangeEvent.REMOVE )
			{
				if( element instanceof Node )
				{
					for( GraphAttributesListener l: attrListeners )
						l.nodeAttributeRemoved( getId(), element.getId(), attribute );
				}
				else if( element instanceof Edge )
				{
					for( GraphAttributesListener l: attrListeners )
						l.edgeAttributeRemoved( getId(), element.getId(), attribute );
				}
				else
				{
					for( GraphAttributesListener l: attrListeners )
						l.graphAttributeRemoved( getId(), attribute );					
				}								
			}
			else
			{
				if( element instanceof Node )
				{
					for( GraphAttributesListener l: attrListeners )
						l.nodeAttributeChanged( getId(), element.getId(), attribute, oldValue, newValue );
				}
				else if( element instanceof Edge )
				{
					for( GraphAttributesListener l: attrListeners )
						l.edgeAttributeChanged( getId(), element.getId(), attribute, oldValue, newValue );
				}
				else
				{
					for( GraphAttributesListener l: attrListeners )
						l.graphAttributeChanged( getId(), attribute, oldValue, newValue );					
				}				
			}

			manageEvents();
			eventProcessing = false;
			checkListenersToRemove();
		}
		else
		{
//			printPosition( "ChgEdge in EventProc" );
			eventQueue.add( new AttributeChangedEvent( element, attribute, event, oldValue, newValue ) );
		}
	}

// Commands -- Utility

	public void read( FileInput input, String filename ) throws IOException, GraphParseException
    {
		input.readAll( filename );
    }

	public void read( String filename )
		throws IOException, GraphParseException, NotFoundException
	{
		FileInput input = FileInputFactory.inputFor( filename );
		input.addGraphListener( this );
		read( input, filename );
//		GraphReaderListenerHelper listener = new GraphReaderListenerHelper( this );
//		GraphReader reader = GraphReaderFactory.readerFor( filename );
//		reader.addGraphReaderListener( listener );
//		reader.read( filename );
	}
/*	
	public void read( GraphReader reader, String filename )
		throws IOException, GraphParseException
	{
		GraphReaderListenerHelper listener = new GraphReaderListenerHelper( this );
		reader.addGraphReaderListener( listener );
		reader.read( filename );
	}
*/
	public void write( FileOutput output, String filename ) throws IOException
    {
		output.writeAll( this, filename );
    }
	
	public void write( String filename )
		throws IOException
	{
		FileOutput output = FileOutputFactory.outputFor( filename );
		write( output, filename );
//		GraphWriterHelper gwh = new GraphWriterHelper( this );
//		gwh.write( filename );
	}
/*	
	public void write( GraphWriter writer, String filename )
		throws IOException
	{
		GraphWriterHelper gwh = new GraphWriterHelper( this );
		gwh.write( filename, writer );
	}
	
	public int readPositionFile( String posFileName )
		throws IOException
	{
		if( posFileName == null )
			throw new IOException( "no filename given" );
		
		Scanner scanner = new Scanner( new BufferedInputStream( new FileInputStream( posFileName ) ) );
		int     ignored = 0;
		int     mapped  = 0;
		int     line    = 1;
		String  id      = null;
		float   x = 0, y = 0, z = 0;

		scanner.useLocale( Locale.US );
		scanner.useDelimiter( "\\s|\\n|:" );

		try
		{
			while( scanner.hasNext() )
			{
				id = scanner.next();

				x  = scanner.nextFloat();
				y  = scanner.nextFloat();
				z  = scanner.nextFloat();

				line++;
				
				DefaultNode node = (DefaultNode) nodes.get( id );

				if( node != null )
				{
					node.addAttribute( "x", x );
					node.addAttribute( "y", y );
					node.addAttribute( "z", z );
					mapped++;
				}
				else
				{
					ignored++;
				}
			}
		}
		catch( InputMismatchException e )
		{
			e.printStackTrace();
			throw new IOException( "parse error '"+posFileName+"':"+line+": " + e.getMessage() );
		}
		catch( NoSuchElementException e )
		{
			throw new IOException( "unexpected end of file '"+posFileName+"':"+line+": " + e.getMessage() );
		}
		catch( IllegalStateException e )
		{
			throw new IOException( "scanner error '"+posFileName+"':"+line+": " + e.getMessage() );
		}

		scanner.close();

		return ignored;
	}
*/	
	public GraphViewerRemote display()
	{
		return display( true );
	}

	public GraphViewerRemote display( boolean autoLayout )
	{
		String viewerClass = "org.miv.graphstream.ui.swing.SwingGraphViewer";
		
		try
        {
	        Class<?> c      = Class.forName( viewerClass );
	        Object   object = c.newInstance();
	        
	        if( object instanceof GraphViewer )
	        {
	        	GraphViewer gv = (GraphViewer) object;
	        	
	        	gv.open(  this, autoLayout );
	        	
	        	return gv.newViewerRemote();
	        }
	        else
	        {
	        	System.err.printf( "Viewer class '%s' is not a 'GraphViewer'%n", object );
	        }
        }
        catch( ClassNotFoundException e )
        {
	        e.printStackTrace();
        	System.err.printf( "Cannot display graph, 'GraphViewer' class not found : " + e.getMessage() );
        }
        catch( InstantiationException e )
        {
            e.printStackTrace();
        	System.err.printf( "Cannot display graph, class '"+viewerClass+"' error : " + e.getMessage() );
        }
        catch( IllegalAccessException e )
        {
            e.printStackTrace();
        	System.err.printf( "Cannot display graph, class '"+viewerClass+"' illegal access : " + e.getMessage() );
        }
        
        return null;
	}
	
	@Override
	public String toString()
	{
		return String.format( "[graph %s (%d nodes %d edges)]", getId(),
				nodes.size(), edges.size() );
	}

	/**
	 * An internal class that represent an iterator able to browse edge or node sets
	 * and to remove correctly elements. This is tricky since removing a node or edge
	 * does more than only altering the node or edge sets (events for example).
	 * @param <T> Can be an Edge or a Node.
	 */
	static class ElementIterator<T extends Element> implements Iterator<T>
	{
		/**
		 * If true, acts on the node set, else on the edge set.
		 */
		boolean onNodes;
		
		/**
		 * Iterator on the set of elements to browse / remove.
		 */
		Iterator<? extends T> iterator;
		
		/**
		 * The last browsed element via next(). This allows to get the
		 * element id to (eventually) remove it.
		 */
		T current;
		
		/**
		 * The graph reference to remove elements.
		 */
		DefaultGraph graph;

		/**
		 * New iterator on elements of hash maps.
		 * @param graph The graph the set of elements pertains to, this reference is used for
		 *    removing elements.
		 * @param elements The elements set to browse.
		 * @param onNodes If true the set is a node set, else it is an edge set.
		 */
		ElementIterator( DefaultGraph graph, HashMap<String, ? extends T> elements, boolean onNodes )
		{
			iterator = elements.values().iterator();
			this.graph   = graph;
			this.onNodes = onNodes;
		}
		
		/**
		 * New iterator on elements of hash maps.
		 * @param graph The graph the set of elements pertains to, this reference is used for
		 *    removing elements.
		 * @param elements The elements set to browse.
		 * @param onNodes If true the set is a node set, else it is an edge set.
		 */
		ElementIterator( DefaultGraph graph, ArrayList<T> elements, boolean onNodes )
		{
			iterator = elements.iterator();
			this.graph   = graph;
			this.onNodes = onNodes;
		}

		public boolean hasNext()
		{
			return iterator.hasNext();
		}

		public T next()
		{
			current = iterator.next();
			return current;
		}

		public void remove()
		{
			if( onNodes )
			{
				if( current != null )
				{
					graph.removeNode_( current.getId(), true );
					iterator.remove();
				}
			}
			else	// On edges.
			{
				if( current != null )
				{
					graph.removeEdge_( current.getId(), true );
					iterator.remove();
				}
			}
		}
	}
	
// Events Management

	/**
	 * Interface that provide general purpose classification for evens involved
	 * in graph modifications
	 */
	interface GraphEvent {}

	class AfterEdgeAddEvent implements GraphEvent
	{
		Edge edge;

		AfterEdgeAddEvent( Edge edge )
		{
			this.edge = edge;
		}
	}

	class BeforeEdgeRemoveEvent implements GraphEvent
	{
		Edge edge;

		BeforeEdgeRemoveEvent( Edge edge )
		{
			this.edge = edge;
		}
	}

	class AfterNodeAddEvent implements GraphEvent
	{
		Node node;

		AfterNodeAddEvent( Node node )
		{
			this.node = node;
		}
	}

	class BeforeNodeRemoveEvent implements GraphEvent
	{
		Node node;

		BeforeNodeRemoveEvent( Node node )
		{
			this.node = node;
		}
	}

	class BeforeGraphClearEvent implements GraphEvent
	{
	}

	class AttributeChangedEvent implements GraphEvent
	{
		Element element;

		String attribute;
		
		AttributeChangeEvent event;

		Object oldValue;

		Object newValue;

		AttributeChangedEvent( Element element, String attribute, AttributeChangeEvent event, Object oldValue, Object newValue )
		{
			this.element   = element;
			this.attribute = attribute;
			this.event     = event;
			this.oldValue  = oldValue;
			this.newValue  = newValue;
		}
	}
	
	/**
	 * Private method that manages the events stored in the {@link #eventQueue}.
	 * These event where created while being invoked from another event
	 * invocation.
	 * @param event
	 */
	private void 
	manageEvent( GraphEvent event )
	{
		if( event.getClass() == AttributeChangedEvent.class )
		{
			AttributeChangedEvent ev = (AttributeChangedEvent)event;
			
			if( ev.event == AttributeChangeEvent.ADD )
			{
				if( ev.element instanceof Node )
				{
					for( GraphAttributesListener l: attrListeners )
						l.nodeAttributeAdded( getId(), ev.element.getId(), ev.attribute, ev.newValue );
				}
				else if( ev.element instanceof Edge )
				{
					for( GraphAttributesListener l: attrListeners )
						l.edgeAttributeAdded( getId(), ev.element.getId(), ev.attribute, ev.newValue );					
				}
				else
				{
					for( GraphAttributesListener l: attrListeners )
						l.graphAttributeAdded( getId(), ev.attribute, ev.newValue );										
				}
			}
			else if( ev.event == AttributeChangeEvent.REMOVE )
			{
				if( ev.element instanceof Node )
				{
					for( GraphAttributesListener l: attrListeners )
						l.nodeAttributeRemoved( getId(), ev.element.getId(), ev.attribute );
				}
				else if( ev.element instanceof Edge )
				{
					for( GraphAttributesListener l: attrListeners )
						l.edgeAttributeRemoved( getId(), ev.element.getId(), ev.attribute );					
				}
				else
				{
					for( GraphAttributesListener l: attrListeners )
						l.graphAttributeRemoved( getId(), ev.attribute );										
				}
			}
			else
			{
				if( ev.element instanceof Node )
				{
					for( GraphAttributesListener l: attrListeners )
						l.nodeAttributeChanged( getId(), ev.element.getId(), ev.attribute, ev.oldValue, ev.newValue );
				}
				else if( ev.element instanceof Edge )
				{
					for( GraphAttributesListener l: attrListeners )
						l.edgeAttributeChanged( getId(), ev.element.getId(), ev.attribute, ev.oldValue, ev.newValue );					
				}
				else
				{
					for( GraphAttributesListener l: attrListeners )
						l.graphAttributeChanged( getId(), ev.attribute, ev.oldValue, ev.newValue );										
				}				
			}
		}
		
		// Elements events
		
		else if( event.getClass() == AfterEdgeAddEvent.class )
		{
			Edge e = ((AfterEdgeAddEvent)event).edge;
			
			for( GraphElementsListener l: eltsListeners )
				l.edgeAdded( getId(), e.getId(), e.getNode0().getId(), e.getNode1().getId(), e.isDirected() );
		}
		else if( event.getClass() == AfterNodeAddEvent.class )
		{
			for( GraphElementsListener l: eltsListeners )
				l.nodeAdded( getId(), ((AfterNodeAddEvent)event).node.getId() );
		}
		else if( event.getClass() == BeforeEdgeRemoveEvent.class )
		{
			for( GraphElementsListener l: eltsListeners )
				l.edgeRemoved( getId(), ((BeforeEdgeRemoveEvent)event).edge.getId() );
		}
		else if( event.getClass() == BeforeNodeRemoveEvent.class )
		{
			for( GraphElementsListener l: eltsListeners )
				l.nodeRemoved( getId(), ((BeforeNodeRemoveEvent)event).node.getId() );
		}
	}
	
// Output

	public void edgeAdded( String graphId, String edgeId, String fromNodeId, String toNodeId,
            boolean directed )
    {
		addEdge( edgeId, fromNodeId, toNodeId, directed );
    }

	public void edgeRemoved( String graphId, String edgeId )
    {
		removeEdge( edgeId );
    }

	public void graphCleared()
    {
		clear();
    }

	public void nodeAdded( String graphId, String nodeId )
    {
		addNode( nodeId );
    }

	public void nodeRemoved( String graphId, String nodeId )
    {
		removeNode( nodeId );
    }

	public void stepBegins( String graphId, double time )
    {
		stepBegins( time );
    }

	public void graphCleared( String graphId )
    {
		clear();
    }

	public void edgeAttributeAdded( String graphId, String edgeId, String attribute, Object value )
    {
		Edge edge = getEdge( edgeId );
		
		if( edge != null )
			edge.addAttribute( attribute, value );
    }

	public void edgeAttributeChanged( String graphId, String edgeId, String attribute,
            Object oldValue, Object newValue )
    {
		Edge edge = getEdge( edgeId );
		
		if( edge != null )
			edge.changeAttribute( attribute, newValue );
    }

	public void edgeAttributeRemoved( String graphId, String edgeId, String attribute )
    {
		Edge edge = getEdge( edgeId );
		
		if( edge != null )
			edge.removeAttribute( attribute );
    }

	public void graphAttributeAdded( String graphId, String attribute, Object value )
    {
		addAttribute( attribute, value );
    }

	public void graphAttributeChanged( String graphId, String attribute, Object oldValue,
            Object newValue )
    {
		changeAttribute( attribute, newValue );
    }

	public void graphAttributeRemoved( String graphId, String attribute )
    {
		removeAttribute( attribute );
    }

	public void nodeAttributeAdded( String graphId, String nodeId, String attribute, Object value )
    {
		Node node = getNode( nodeId );
		
		if( node != null )
			node.addAttribute( attribute, value );
    }

	public void nodeAttributeChanged( String graphId, String nodeId, String attribute,
            Object oldValue, Object newValue )
    {
		Node node = getNode( nodeId );
		
		if( node != null )
			node.changeAttribute( attribute, newValue );
    }

	public void nodeAttributeRemoved( String graphId, String nodeId, String attribute )
    {
		Node node = getNode( nodeId );
		
		if( node != null )
			node.removeAttribute( attribute );
    }
}