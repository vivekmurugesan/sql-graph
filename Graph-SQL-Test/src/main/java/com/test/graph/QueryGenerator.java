package com.test.graph;

import java.util.List;

public class QueryGenerator {
        private Vertex start;
        private String idField;
        private String idValue;
        private List<Edge> edgeList;

        private String DEL_PREFIX = "DELETE FROM ";
        private String JOIN_REGEX = "%s INNER JOIN %s ON %s";
        private String WHERE_REGEX = " WHERE %s.%s = %s";

    public QueryGenerator(Vertex start, String idField, String idValue,
            List<Edge> edgeList) {
        this.start = start;
        this.idField = idField;
        this.idValue = idValue;
        this.edgeList = edgeList;
    }

        public String generateQuery(Vertex target, List<Vertex> path){
        String delStmt=null;
        String whereCondition = String.format(WHERE_REGEX,
                start.getId(), idField, idValue);
        if(target.getId().equals(start.getId())){
            delStmt = DEL_PREFIX + start.getId() + whereCondition;
        }else {
            int size = path.size();
            String joinStr = null;
            System.out.println(".. path for::" + target);
            System.out.println(path);
            for (int i = 0; i < size - 1; i++) {
                Vertex current = path.get(i);
                Vertex next = path.get(i + 1);
                if (joinStr == null) {
                    joinStr = String.format(JOIN_REGEX, current, next,
                            getOnCondition(current, next));
                } else {
                    joinStr = String.format(JOIN_REGEX, joinStr, next,
                            getOnCondition(current, next));
                }
            }
            delStmt = DEL_PREFIX + joinStr + whereCondition;
        }

        return delStmt;
    }

        private String getOnCondition(Vertex dest, Vertex orig){
        Edge result = null;
        for(Edge e : this.edgeList){
            if(e.getDest().getId().equals(dest.getId()) &&
                    e.getOrig().getId().equals(orig.getId())) {
                result = e;
                break;
            }
        }
        if(result != null)
            return result.getEdgeProp();
        else {
            System.err.println("Edge for:" + dest + "::" +orig +".. not found.");
            return null;
        }
    }
}
