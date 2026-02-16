package com.test.graph;

public class Edge {
        private Vertex orig;
        private Vertex dest;
        private String edgeProp;

        public Vertex getOrig() {
        return orig;
    }
        public void setOrig(Vertex orig) {
        this.orig = orig;
    }
        public Vertex getDest() {
        return dest;
    }
        public void setDest(Vertex dest) {
        this.dest = dest;
    }

        public String getEdgeProp() {
        return edgeProp;
    }

        public void setEdgeProp(String edgeProp) {
        this.edgeProp = edgeProp;
    }

    public Edge(Vertex orig, Vertex dest,
            String edgeProp) {
        super();
        this.orig = orig;
        this.dest = dest;
        this.edgeProp =edgeProp;
    }

        @Override
        public String toString() {
        return "Edge{" +
                "orig=" + orig +
                ", dest=" + dest +
                ", edgeProp='" + edgeProp + '\'' +
                '}';
    }
}
