package abcvtagger.profile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import org.apache.commons.collections15.Transformer;

import abcvtagger.utils.Utils;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMLReader2;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;
import edu.uci.ics.jung.io.graphml.NodeMetadata;

/**
 * File input and output methods for reading and writing graphs
 * in GraphML format.
 * @author Tuomo Kakkonen
 */
public class GraphIO {
	
	/**
	 * Returns GraphML presentation of the graph given
	 * as the parameter.
	 * @param graph Graph to transform into GraphML.
	 * @return String containing GraphML representation of the input graph.
	 */
	public static String getGraphInXML(Graph<SentimentVertex, String> graph) {
		GraphMLWriter<SentimentVertex, String> graphWriter =
            new GraphMLWriter<SentimentVertex, String>();

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(baos);			
			graphWriter.save(graph, out);
			return baos.toString();			
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		} 
		return "";
	}

	/**
	 * Creates the graph object represented by the input string.
	 * @param xmlGraph GraphML representation of the graph.
	 * @return Graph object.
	 */
	public static DirectedSparseMultigraph<SentimentVertex, String> createGraphFromXML(String xmlGraph, final int tokenCount, final int negTokenCount) {
		System.out.println(xmlGraph);
		
		Transformer<NodeMetadata, SentimentVertex> vtrans = new Transformer<NodeMetadata,SentimentVertex>(){
            public SentimentVertex transform(NodeMetadata nmd ){
            	String id = nmd.getId();
            	int ind = id.indexOf(' ');
            	String name = id.substring(0, ind);
            	SentimentVertex v = new SentimentVertex(name);   	
            	id = id.substring(ind, id.length());
            	
            	int freq = Utils.stringToInt(id);
            	if(freq > 0) v.setValues(freq, tokenCount, negTokenCount);            	            	
                return v;
            }
    };
    Transformer<EdgeMetadata, String> etrans = new Transformer<EdgeMetadata,String>(){
            public String transform( EdgeMetadata emd ){
            	String target = emd.getTarget();
            	String source = emd.getSource();
                return source + "->" + target;
            }
    };
		    	
    	Transformer<HyperEdgeMetadata, String> hetrans = new Transformer<HyperEdgeMetadata,String>(){

            public String transform( HyperEdgeMetadata emd ){
                    /*Edge e = new Edge() ;
                    e.type = emd.getProperty("type");
                    e.value = emd.getProperty("value");
                    e.id = Integer.valueOf( emd.getId() );
                    return e;*/
            	return "";
            }
    };
    
    Transformer< GraphMetadata , DirectedSparseMultigraph<SentimentVertex, String>> gtrans = new Transformer<GraphMetadata, DirectedSparseMultigraph<SentimentVertex, String>>(){
            public DirectedSparseMultigraph<SentimentVertex, String> transform( GraphMetadata gmd ){
                    return new DirectedSparseMultigraph<SentimentVertex, String>();
            }
    };



    GraphMLReader2<DirectedSparseMultigraph<SentimentVertex, String> , SentimentVertex, String> gmlr =
            new GraphMLReader2<DirectedSparseMultigraph<SentimentVertex, String> ,SentimentVertex, String>(
            		new StringReader(xmlGraph),
                            gtrans, vtrans, etrans, hetrans);
    	
    	try {
    	    return gmlr.readGraph();
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	return null;
	}	
}
