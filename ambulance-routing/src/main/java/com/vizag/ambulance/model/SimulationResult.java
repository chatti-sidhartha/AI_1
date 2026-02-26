package com.vizag.ambulance.model;

import java.util.List;

/**
 * Result of a pathfinding simulation.
 */
public class SimulationResult {
    private List<String> path;           // Ordered list of node IDs in the path
    private List<String> pathNames;      // Ordered list of node names for display
    private double totalCost;            // Total path cost (distance + traffic)
    private int nodesExplored;           // Number of nodes expanded during search
    private double executionTimeMs;      // Execution time in milliseconds
    private String algorithm;            // Algorithm used
    private String traffic;              // Traffic condition used
    private boolean success;             // Whether a path was found
    private String errorMessage;         // Error message if path not found

    public SimulationResult() {}

    // Static factory methods for success and failure
    public static SimulationResult success(List<String> path, List<String> pathNames, 
            double totalCost, int nodesExplored, double executionTimeMs, 
            String algorithm, String traffic) {
        SimulationResult result = new SimulationResult();
        result.path = path;
        result.pathNames = pathNames;
        result.totalCost = totalCost;
        result.nodesExplored = nodesExplored;
        result.executionTimeMs = executionTimeMs;
        result.algorithm = algorithm;
        result.traffic = traffic;
        result.success = true;
        return result;
    }

    public static SimulationResult failure(String errorMessage, String algorithm, String traffic) {
        SimulationResult result = new SimulationResult();
        result.success = false;
        result.errorMessage = errorMessage;
        result.algorithm = algorithm;
        result.traffic = traffic;
        result.nodesExplored = 0;
        result.executionTimeMs = 0;
        result.totalCost = 0;
        return result;
    }

    // Getters and Setters
    public List<String> getPath() { return path; }
    public void setPath(List<String> path) { this.path = path; }

    public List<String> getPathNames() { return pathNames; }
    public void setPathNames(List<String> pathNames) { this.pathNames = pathNames; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public int getNodesExplored() { return nodesExplored; }
    public void setNodesExplored(int nodesExplored) { this.nodesExplored = nodesExplored; }

    public double getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(double executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getTraffic() { return traffic; }
    public void setTraffic(String traffic) { this.traffic = traffic; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
