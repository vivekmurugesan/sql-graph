package com.test.graph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ERModelProcessor {
        private static final String DELIM = ",";

        private Map<String, Vertex> vertices;

        private String fileName;
        private String startStr = "User";
        private String idField;
        private String idVal;

        private Map<Vertex, Boolean> marked;
        private int count;

    public ERModelProcessor(String fileName, String startVertex,
            String idField, String idVal) {
        this.fileName = fileName;
        this.startStr = startVertex;
        this.idField = idField;
        this.idVal = idVal;
        this.marked = new HashMap<>();
        this.count = 0;
    }
        ;
        public static void main(String[] args) {

        if(args.length < 4){
            System.err.println("Usage:: ERModelProcessor <edges_file_name> " +
                    "<start_vertex_name> <id_field> <id_val>");
            return;
        }

        ERModelProcessor processor = new ERModelProcessor(args[0], args[1],
                args[2], args[3]);
        processor.process();
    }

        public void process(){
        AdjList graph = this.constructGraph(fileName);
        Vertex start = this.vertices.get(startStr);
        Map<Vertex, Vertex> parents = this.bfs(graph, start);
        System.out.println("Parents::" + parents);
        Map<Vertex, List<Vertex>> paths = this.constructPaths(graph, parents,
                start);

        QueryGenerator generator = new QueryGenerator(start, idField, idVal,
                graph.getEdges());
        // Del stmt generation
        Map<Vertex, String> delStmtMap = new HashMap<>();
        System.out.println("Generating del stmt");
        /*for(Vertex v : paths.keySet()){
            String delStmt = generator.generateQuery(v, paths.get(v));
            System.out.println("Del stmt generated for:" + v);
            System.out.println(delStmt);
            delStmtMap.put(v, delStmt);
        }*/

        LinkedList<Vertex> topoSorted = this.topoSortHelper(graph, start);
        System.out.println(topoSorted);
        Vertex current = topoSorted.removeLast();
        int order = 1;
        TreeMap<Integer, Vertex> orderMap=new TreeMap<>();
        do {
            String delStmt = generator.generateQuery(current, paths.get(current));
            System.out.println("Del stmt generated for:" + current);
            System.out.println(delStmt);
            delStmtMap.put(current, delStmt);
            orderMap.put(order++,current);
            if(topoSorted.size()<=0)
                break;
            current = topoSorted.removeLast();
        }while(current != null);

        for(int key : orderMap.keySet()){
            System.out.printf("Order : %d, query: %s\n",
                    key, delStmtMap.get(orderMap.get(key)));
        }
    }

        public AdjList constructGraph(String fileName){
        this.vertices = new HashMap<>();
        List<Edge> edgeList = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = br.readLine();
            do{
                String[] tokens = line.split(DELIM);
                if(tokens.length < 4){
                    System.err.println("Ignoring line:" + line);
                    continue;
                }
                String orig = tokens[0];
                String dest = tokens[1];
                String edgeProp = tokens[2] + "=" + tokens[3];
                Vertex origVertex = checkAndCreateVertex(vertices, orig);
                Vertex destVertex = checkAndCreateVertex(vertices, dest);
                Edge edge = new Edge(origVertex, destVertex, edgeProp);
                edgeList.add(edge);
                line = br.readLine();
            }while(line != null && !line.isBlank());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Vertex> vertexList = new ArrayList<>();
        vertexList.addAll(vertices.values());

        System.out.println("Vertices:\n" + vertices);
        System.out.println("Vertices:\n" + vertexList);
        System.out.println("Edges:\n" + edgeList);

        AdjList graph = new AdjList(vertexList, edgeList);

        return  graph;
    }

        private Vertex checkAndCreateVertex(Map<String, Vertex> vertices, String id){
        Vertex result = null;
        if(vertices.containsKey(id))
            result = vertices.get(id);
        else{
            result = new Vertex(id);
            vertices.put(id, result);
        }

        return result;
    }

        public Map<Vertex, Vertex> bfs(AdjList graph, Vertex start){
        LinkedList<Vertex> queue = new LinkedList<Vertex>();

        Map<Vertex,Boolean> discovered = new HashMap<Vertex,Boolean>();
        Map<Vertex,Boolean> processed = new HashMap<Vertex,Boolean>();
        Map<Vertex, Vertex> parent = new HashMap<Vertex,Vertex>();
        queue.add(start);
        discovered.put(start, true);

        while(!queue.isEmpty()){
            Vertex v = queue.removeFirst();
            processed.put(v, true);
            List<Vertex> connVertices = graph.getConnectedVertices(v);
            if(connVertices != null){
                for(Vertex y : connVertices){
                    if(discovered.get(y) == null || !discovered.get(y)){
                        queue.addLast(y);
                        discovered.put(y, true);
                        parent.put(y,v);
                    }
                }
            }
        }

        return parent;
    }

        public Map<Vertex, List<Vertex>> constructPaths(AdjList graph,
            Map<Vertex,Vertex> parents,
            Vertex start){
        List<Vertex> vertices = graph.getVertices();
        Map<Vertex, List<Vertex>> paths = new HashMap<>();
        System.out.println("Start vertex:" + start);
        for(Vertex v : vertices){
            System.out.println("Constructing path for ::" + v);
            if(v.getId().equals(start.getId())){
                List<Vertex> path = new ArrayList<>();
                path.add(v);
                paths.put(v, path);
            }else{
                List<Vertex> path = new ArrayList<>();
                path.add(v);
                Vertex parent = parents.get(v);
                while(!parent.getId().equals(start.getId())){
                    path.add(parent);
                    parent = parents.get(parent);
                }
                path.add(start);
                paths.put(v, path);
            }
        }
        System.out.println(".. paths returned.. " + paths);
        return paths;
    }

        public LinkedList<Vertex> topoSortHelper(AdjList graph, Vertex start){
        this.marked.clear();
        this.count=0;
        LinkedList<Vertex> stack = new LinkedList<>();
        this.topoSort(graph, start, stack);
        return stack;
    }

        public void topoSort(AdjList graph, Vertex start, LinkedList<Vertex> stack){
        marked.put(start,true);
        count++;
        List<Vertex> connVertices = graph.getConnectedVertices(start);
        if(connVertices != null){
            for(Vertex v:connVertices){
                if(marked.get(v) == null || !marked.get(v))
                    topoSort(graph, v, stack);
            }
        }
        stack.push(start);

    }
}
