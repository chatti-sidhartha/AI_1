package com.vizag.ambulance.service.algorithm;

import com.vizag.ambulance.model.Graph;
import com.vizag.ambulance.model.Graph.EdgeInfo;
import com.vizag.ambulance.model.SimulationResult;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Breadth-First Search (BFS) Algorithm.
 * 
 * Properties:
 * - Complete: Yes
 * - Optimal: Yes (for unweighted graphs - finds shortest path by edge count)
 * - Time Complexity: O(V + E)
 * - Space Complexity: O(V)
 * 
 * Note: BFS finds the shortest path in terms of number of edges,
 * not necessarily the lowest cost path in weighted graphs.
 */
@Component
public class BFSAlgorithm implements SearchAlgorithm {

    @Override
    public String getAlgorithmName() {
        return "BFS (Breadth-First Search)";
    }

    @Override
    public SimulationResult findPath(Graph graph, String startNodeId, String goalNodeId, String trafficCondition) {
        long startTime = System.nanoTime();
        int nodesExplored = 0;

        // Validate nodes exist
        if (graph.getNode(startNodeId) == null || graph.getNode(goalNodeId) == null) {
            return SimulationResult.failure("Invalid start or goal node", getAlgorithmName(), trafficCondition);
        }

        // BFS uses a Queue (FIFO)
        Queue<String> frontier = new LinkedList<>();
        Map<String, String> cameFrom = new HashMap<>(); // For path reconstruction
        Set<String> visited = new HashSet<>();

        frontier.add(startNodeId);
        visited.add(startNodeId);
        cameFrom.put(startNodeId, null);

        while (!frontier.isEmpty()) {
            String current = frontier.poll();
            nodesExplored++;

            // Goal check
            if (current.equals(goalNodeId)) {
                long endTime = System.nanoTime();
                double executionTimeMs = (endTime - startTime) / 1_000_000.0;

                // Reconstruct path
                List<String> path = reconstructPath(cameFrom, goalNodeId);
                List<String> pathNames = path.stream()
                    .map(id -> graph.getNode(id).getName())
                    .toList();

                // Calculate total cost along the path
                double totalCost = calculatePathCost(graph, path, trafficCondition);

                return SimulationResult.success(path, pathNames, totalCost, nodesExplored, 
                    executionTimeMs, getAlgorithmName(), trafficCondition);
            }

            // Explore neighbors
            for (EdgeInfo neighbor : graph.getNeighbors(current)) {
                String neighborId = neighbor.getTargetNodeId();
                if (!visited.contains(neighborId)) {
                    visited.add(neighborId);
                    frontier.add(neighborId);
                    cameFrom.put(neighborId, current);
                }
            }
        }

        // No path found
        long endTime = System.nanoTime();
        double executionTimeMs = (endTime - startTime) / 1_000_000.0;
        SimulationResult result = SimulationResult.failure(
            "No path found from " + startNodeId + " to " + goalNodeId, 
            getAlgorithmName(), trafficCondition);
        result.setNodesExplored(nodesExplored);
        result.setExecutionTimeMs(executionTimeMs);
        return result;
    }

    /**
     * Reconstruct the path from start to goal using the cameFrom map.
     */
    private List<String> reconstructPath(Map<String, String> cameFrom, String goalNodeId) {
        List<String> path = new ArrayList<>();
        String current = goalNodeId;
        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Calculate the total cost of a path considering traffic.
     */
    private double calculatePathCost(Graph graph, List<String> path, String trafficCondition) {
        double totalCost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            String current = path.get(i);
            String next = path.get(i + 1);
            for (EdgeInfo edge : graph.getNeighbors(current)) {
                if (edge.getTargetNodeId().equals(next)) {
                    totalCost += edge.getWeight(trafficCondition);
                    break;
                }
            }
        }
        return totalCost;
    }
}
