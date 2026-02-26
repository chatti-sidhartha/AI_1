package com.vizag.ambulance.model;

/**
 * Request payload for running a simulation.
 */
public class SimulationRequest {
    private String startNode;
    private String endNode;
    private String traffic;    // LOW, MEDIUM, HIGH
    private String algorithm;  // BFS, DFS, UCS, GREEDY, A_STAR

    public SimulationRequest() {}

    public SimulationRequest(String startNode, String endNode, String traffic, String algorithm) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.traffic = traffic;
        this.algorithm = algorithm;
    }

    // Getters and Setters
    public String getStartNode() { return startNode; }
    public void setStartNode(String startNode) { this.startNode = startNode; }

    public String getEndNode() { return endNode; }
    public void setEndNode(String endNode) { this.endNode = endNode; }

    public String getTraffic() { return traffic; }
    public void setTraffic(String traffic) { this.traffic = traffic; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    @Override
    public String toString() {
        return "SimulationRequest{" +
                "startNode='" + startNode + '\'' +
                ", endNode='" + endNode + '\'' +
                ", traffic='" + traffic + '\'' +
                ", algorithm='" + algorithm + '\'' +
                '}';
    }
}
