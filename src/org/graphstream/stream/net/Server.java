package org.graphstream.stream.net;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import fi.iki.elonen.NanoHTTPD;

public class Server extends NanoHTTPD{
	
	private final String path;
	
	private final HTTPSource source;
	
	

	public Server(final int port,final String graphId){
		super(port);
		this.path="/"+graphId;
		this.source= new HTTPSource(graphId);
		
	}
	
	@Override
	public void start() throws IOException {
		
		super.start(SOCKET_READ_TIMEOUT,false);
	}
	
	@Override
	public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms,
			Map<String, String> files) {
		final String localPath = uri.substring(uri.lastIndexOf('/'));
		final String action = uri.substring(uri.lastIndexOf('/'),uri.length());
		String response = "202";
		if(localPath.equals(this.path)&&method.equals(Method.POST)){
			if("add".equals(action)){
				this.source.sendNodeAdded(parms.get("id"));
			}else if("change".equals(action)){
				final boolean directed = BooleanUtils.toBoolean(parms.get("directed"));
				this.source.sendEdgeAdded(parms.get("id"),parms.get("from"),parms.get("to"),directed);
			}else if("changeGraph".equals(action)){
				//Do Something here was not implemented
			}else if("step".equals(action)){
				final double step = NumberUtils.toDouble(parms.get("step"));
				this.source.sendStepBegins(step);
			}
		}else if(localPath.equals(this.path)&& method.equals(Method.DELETE)){
			if("node".equals(action)){
				this.source.sendNodeRemoved(parms.get("id"));
			}else if("edge".equals(action)){
				this.source.sendEdgeRemoved(parms.get("id"));
			}
		}else{
			response="400";
		}
		return newFixedLengthResponse(response);
	}
	

}
