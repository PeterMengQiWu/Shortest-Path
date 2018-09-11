import java.util.*;
import java.io.*;
import net.datastructures.Graph;
import net.datastructures.Vertex;
import net.datastructures.Edge;
import net.datastructures.AdjacencyMapGraph;
import net.datastructures.Map;
import net.datastructures.ProbeHashMap;
import net.datastructures.GraphAlgorithms;
import net.datastructures.Entry;
import net.datastructures.PriorityQueue;
import net.datastructures.AdaptablePriorityQueue;
import net.datastructures.HeapAdaptablePriorityQueue;

/*
 * Class made by Peter Wu
 */
public class ParisMetro{
	static Graph<Integer, Integer> sGraph=new AdjacencyMapGraph<Integer,Integer>(true); // the actual graph
	static Hashtable<Integer,Boolean> visited; // the hashtable stores all visited vertices while performing DFS
	static int numOfVertices=0; // stores the total number of vertices
	static int numOfEdges=0; // stores the total number of edges
	static ArrayList<Vertex<Integer>> lineVertices = new ArrayList<Vertex<Integer>>(); // stores the vertices of the line containing malfunction station (explicitly for findLine method)

   /**
   * Read a list of edges from file
   * (Copied from lab 10 solution. This method is slightly modified)
   */
	public static void readMetro() throws Exception, IOException {
	    BufferedReader input = new BufferedReader(new FileReader("metro.txt"));
	    String line=input.readLine();
	    StringTokenizer st = new StringTokenizer(line);
	    numOfVertices = new Integer(st.nextToken().substring(3)); // there seems to be an encoding error, there are 3 characters in front of the first line
	    numOfEdges = new Integer(st.nextToken());
	    while(!(line=input.readLine()).equals("$")){
	    	//skip the station names
		}
		// Create a hash map to store all the vertices read
		Hashtable<Integer, Vertex> vertices = new Hashtable<Integer, Vertex>();

		while((line=input.readLine())!=null){
			st = new StringTokenizer(line);
			if (st.countTokens() != 3)
				throw new IOException("Incorrect input file at line " + line);
			Integer source = new Integer(st.nextToken());
			Integer dest = new Integer(st.nextToken()); 
			Integer weight = new Integer(st.nextToken());
			Vertex<Integer> sv = vertices.get(source);
			if (sv == null) {
				// Source vertex not in graph -- insert
				sv = sGraph.insertVertex(source);
				vertices.put(source, sv);
			}
			Vertex<Integer> dv = vertices.get(dest);
			if (dv == null) {
				// Destination vertex not in graph -- insert
				dv = sGraph.insertVertex(dest);
				vertices.put(dest, dv);
			}
			// check if edge is already in graph
			if (sGraph.getEdge(sv, dv) == null) {
				// edge not in graph -- add
				//e's element is now the distance between the vertices
				if(weight == -1){
					sGraph.insertEdge(sv, dv, 90);
				}
				else{
					sGraph.insertEdge(sv, dv, weight);//copied from method read() from lab 10 solution
				}
			}
		}
	}

	/**
	 * Helper routine to get a Vertex (Position) from a string naming the vertex
	 * (Copied from lab 9 solution. The method is slightly modified)
	 */
	protected static Vertex<Integer> getVertex(Integer vert) throws Exception {
		// Go through vertex list to find vertex -- why is this not a map
		for (Vertex<Integer> vs : sGraph.vertices()) {
			if (vs.getElement().equals(vert)) {
				return vs;
			}
		}
		throw new Exception("Vertex not in graph: " + vert);
	}

	/**
	 * Computes shortest-path distances from src vertex to all reachable vertices of g.
	 *
	 * This implementation uses Dijkstra's algorithm.
	 *
	 * The edge's element is assumed to be its integral weight.
	 * (Copied from shortestPathsLength method from net.datastructures.GraphAlgorithms implemented by Goodrich et al. This method is slightly modified)
	 */
	public static void ShortestDistances(int start, int end, int malfunction) throws Exception{
		Vertex<Integer> vSource = getVertex(start);
		Vertex<Integer> vGoal = getVertex(end);
		Vertex<Integer> vNot = (malfunction == -1) ? null : getVertex(malfunction); 
		 // d.get(v) is upper bound on distance from vSource to v
	    Map<Vertex<Integer>, Integer> d = new ProbeHashMap<>();
	    // map reachable v to its d value
	    Map<Vertex<Integer>, Integer> cloud = new ProbeHashMap<>();
	    // pq will have vertices as elements, with d.get(v) as key
	    AdaptablePriorityQueue<Integer, Vertex<Integer>> pq;
	    pq = new HeapAdaptablePriorityQueue<>();
	    // maps from vertex to its pq locator
	    Map<Vertex<Integer>, Entry<Integer,Vertex<Integer>>> pqTokens;
	    pqTokens = new ProbeHashMap<>();

	    if(vNot != null){
	    	// find the line containing vNot
	    	findLine(vNot);
	    }
	    // for each vertex v of the graph, add an entry to the priority queue, with
	    // the source having distance 0 and all others having infinite distance
	    for (Vertex<Integer> v : sGraph.vertices()) {
	      	if (v == vSource){
	        	d.put(v,0);
	      	}
	      	else{
	        	d.put(v, Integer.MAX_VALUE);
	      	}
	      	
	      	pqTokens.put(v, pq.insert(d.get(v), v));
	      	       // save entry for future updates
	    }
	    int[] vertices = new int[numOfVertices]; // an array that stores every previous vertex of every vertex on shortest path. Each index is a vertex and the content of each index is the previous vertex
	    while (!pq.isEmpty()) {
	      
	      Entry<Integer, Vertex<Integer>> entry = pq.removeMin();
	      int key = entry.getKey();
	      Vertex<Integer> u = entry.getValue();
	      cloud.put(u, key);                             // this is actual distance to u
	      pqTokens.remove(u);                           // u is no longer in pq
	      for (Edge<Integer> e : sGraph.outgoingEdges(u)) {
	        	Vertex<Integer> v = sGraph.opposite(u,e);
	        	if(lineVertices.contains(v)){           // if v is in the line containing the malfunction station then skip v so that v will not be in our cloud
	        		continue;
	        	}
	        	if (cloud.get(v) == null) {
	          		// perform relaxation step on edge (u,v)
	          		int wgt = e.getElement();
	          	if (d.get(u) + wgt < d.get(v)) {              	// better path to v?
	            	d.put(v, d.get(u) + wgt);                   // update the distance
	            	pq.replaceKey(pqTokens.get(v), d.get(v));   // update the pq entry
	            	vertices[v.getElement()] = u.getElement();  // add the previous vertex into the array at index v
	  				
	  				
	          	}	
	        }
	      }
	      
	      	
	    }	
	    printPath(vertices, vGoal.getElement(), vSource.getElement());
	    System.out.print(vGoal.getElement()+"\n");
	    
		System.out.println("Time: "+cloud.get(vGoal));

	}

	/**
	 * Helper routine to print the shortest path.
	 */
	private static void printPath(int[] vertices, int i, int src){
		if(i==src){
			return;
		}
		printPath(vertices, vertices[i], src);
		System.out.print(vertices[i]+" ");
	}

	/**
	 * Finds the line containing malfunction station by doing DFS and store the vertices in lineVertices.
	 */
	public static void findLine(Vertex<Integer> v){
		visited = new Hashtable<>();
		lineVertices.add(v);
		findLineRecurse(v);
	}

	private static void findLineRecurse(Vertex<Integer> v){
		if(visited.get(v.getElement())!=null) return;
		visited.put(v.getElement(), Boolean.TRUE);
		for(Edge<Integer> e : sGraph.outgoingEdges(v)){
			Vertex<Integer> oppo = sGraph.opposite(v,e);
			if(e.getElement()!=90){
				lineVertices.add(oppo);
				findLineRecurse(oppo);
			}
		}
	}

	/**
	 * Prints the path.
	 */
	public static void printLine(int s) throws Exception{
		if(getVertex(s) == null){
			throw new Exception("Vertex not found");
		}
		visited = new Hashtable<>();
		printLineRecurse(getVertex(s));
	}

	/**
	* Performs Depth-First Traversal recursively starting from the source vertex to the destination vertex.
	* (Copied from lab 9 solution. This method is slightly modified)
	*/
	private static void printLineRecurse(Vertex<Integer> v) throws Exception{ //DFS
		 if(visited.get(v.getElement())!=null) return;
   	 	visited.put(v.getElement(), Boolean.TRUE);
   	 	Visit(v);
		for(Edge<Integer> e : sGraph.outgoingEdges(v)){
			Vertex<Integer> oppo = sGraph.opposite(v,e);
			if(e.getElement()!=90){
				printLineRecurse(oppo);
			}
		}
		
	}
  	private static void Visit( Vertex<Integer> v ) {
    	System.out.print( v.getElement() +" ");
  	}
 
	public static void main(String args[]){
		try{
			readMetro();
		}catch(IOException e){
			System.err.println(e);
			e.printStackTrace();
		}catch(Exception f){
			System.err.println(f);
			f.printStackTrace();
		}
		if ( args.length < 1 ) { // if the user does not enter anything 
      		System.err.println( "Usage: java ParisMetro vertices" );
      		System.exit(-1);
    	}
		int N1 = Integer.parseInt(args[0]);
		if(args.length == 1){
			try{
				printLine(N1);
			}catch(Exception e){
				System.err.println(e);
				e.printStackTrace();
			}
		}
		else{
			int N2 = Integer.parseInt(args[1]);
			if(args.length == 2){
				try{
					ShortestDistances(N1,N2,-1);
				}catch(Exception e){
					System.err.println(e);
					e.printStackTrace();
				}
			}
			else{
				int N3 = Integer.parseInt(args[2]);
				try{
					ShortestDistances(N1,N2,N3);
				}catch(Exception e){
					System.err.println(e);
					e.printStackTrace();
				}
			}
		}
	}

}