package com.vizag.ambulance.model;

import java.util.*;

/**
 * Represents the complete road network graph of Visakhapatnam.
 */
public class Graph {
    private List<Node> nodes;
    private List<Edge> edges;
    
    // Adjacency list for efficient traversal (built from edges)
    private transient Map<String, List<EdgeInfo>> adjacencyList;

    public Graph() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public Graph(List<Node> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
        buildAdjacencyList();
    }

    // Getters and Setters
    public List<Node> getNodes() { return nodes; }
    public void setNodes(List<Node> nodes) { this.nodes = nodes; }

    public List<Edge> getEdges() { return edges; }
    public void setEdges(List<Edge> edges) { 
        this.edges = edges; 
        buildAdjacencyList();
    }

    /**
     * Build adjacency list from edges for efficient graph traversal.
     * Graph is undirected, so add both directions.
     */
    public void buildAdjacencyList() {
        adjacencyList = new HashMap<>();
        
        // Initialize adjacency list for all nodes
        for (Node node : nodes) {
            adjacencyList.put(node.getId(), new ArrayList<>());
        }
        
        // Add edges (undirected - both directions)
        for (Edge edge : edges) {
            adjacencyList.get(edge.getFrom()).add(
                new EdgeInfo(edge.getTo(), edge.getDistance(), edge.getDefaultTraffic())
            );
            adjacencyList.get(edge.getTo()).add(
                new EdgeInfo(edge.getFrom(), edge.getDistance(), edge.getDefaultTraffic())
            );
        }
    }

    /**
     * Get neighbors of a node with edge weights adjusted for traffic.
     */
    public List<EdgeInfo> getNeighbors(String nodeId) {
        if (adjacencyList == null) {
            buildAdjacencyList();
        }
        return adjacencyList.getOrDefault(nodeId, Collections.emptyList());
    }

    /**
     * Get a node by its ID.
     */
    public Node getNode(String nodeId) {
        return nodes.stream()
            .filter(n -> n.getId().equals(nodeId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Calculate Euclidean distance between two nodes (for heuristic).
     */
    public double euclideanDistance(String nodeId1, String nodeId2) {
        Node n1 = getNode(nodeId1);
        Node n2 = getNode(nodeId2);
        if (n1 == null || n2 == null) return Double.MAX_VALUE;
        
        double dx = n1.getX() - n2.getX();
        double dy = n1.getY() - n2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Inner class to hold edge information for adjacency list.
     */
    public static class EdgeInfo {
        private final String targetNodeId;
        private final double distance;
        private final String defaultTraffic;

        public EdgeInfo(String targetNodeId, double distance, String defaultTraffic) {
            this.targetNodeId = targetNodeId;
            this.distance = distance;
            this.defaultTraffic = defaultTraffic;
        }

        public String getTargetNodeId() { return targetNodeId; }
        public double getDistance() { return distance; }
        public String getDefaultTraffic() { return defaultTraffic; }

        /**
         * Get weight with traffic factor applied.
         */
        public double getWeight(String trafficCondition) {
            int trafficFactor = switch (trafficCondition.toUpperCase()) {
                case "LOW" -> 1;
                case "MEDIUM" -> 3;
                case "HIGH" -> 7;
                default -> 3;
            };
            return distance + trafficFactor;
        }
    }
}
