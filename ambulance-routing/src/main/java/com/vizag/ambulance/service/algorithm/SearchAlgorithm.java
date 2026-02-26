package com.vizag.ambulance.service.algorithm;

import com.vizag.ambulance.model.Graph;
import com.vizag.ambulance.model.SimulationResult;

/**
 * Base interface for all search algorithms.
 */
public interface SearchAlgorithm {
    
    /**
     * Find a path from start to goal using this algorithm.
     * 
     * @param graph The road network graph
     * @param startNodeId Starting node ID
     * @param goalNodeId Goal node ID
     * @param trafficCondition Traffic condition (LOW, MEDIUM, HIGH)
     * @return SimulationResult containing path, cost, time, and stats
     */
    SimulationResult findPath(Graph graph, String startNodeId, String goalNodeId, String trafficCondition);
    
    /**
     * Get the algorithm name for display.
     */
    String getAlgorithmName();
}
