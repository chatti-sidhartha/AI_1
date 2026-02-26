package com.vizag.ambulance.service.algorithm;

import com.vizag.ambulance.model.Graph;
import com.vizag.ambulance.model.Graph.EdgeInfo;
import com.vizag.ambulance.model.SimulationResult;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Depth-First Search (DFS) Algorithm.
 * 
 * Properties:
 * - Complete: Yes (in finite graphs)
 * - Optimal: No (does not guarantee shortest path)
 * - Time Complexity: O(V + E)
 * - Space Complexity: O(V)
 * 
 * Note: DFS explores as deep as possible before backtracking.
 * It finds A path, but not necessarily the best path.
 */
@Component
public class DFSAlgorithm implements SearchAlgorithm {

    @Override
    public String getAlgorithmName() {
        return "DFS (Depth-First Search)";
    }

    @Override
    public SimulationResult findPath(Graph graph, String startNodeId, String goalNodeId, String trafficCondition) {
        long startTime = System.nanoTime();
        int[] nodesExplored = {0}; // Use array for mutation in recursive call

        // Validate nodes exist
        if (graph.getNode(startNodeId) == null || graph.getNode(goalNodeId) == null) {
            return SimulationResult.failure("Invalid start or goal node", getAlgorithmName(), trafficCondition);
        }

        // DFS using iterative approach with Stack (LIFO)
        Stack<String> frontier = new Stack<>();
        Map<String, String> cameFrom = new HashMap<>();
        Set<String> visited = new HashSet<>();

        frontier.push(startNodeId);
        cameFrom.put(startNodeId, null);

        while (!frontier.isEmpty()) {
            String current = frontier.pop();
            
            if (visited.contains(current)) {
                continue;
            }
            
            visited.add(current);
            nodesExplored[0]++;

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

                return SimulationResult.success(path, pathNames, totalCost, nodesExplored[0], 
                    executionTimeMs, getAlgorithmName(), trafficCondition);
            }

            // Explore neighbors (add to stack in reverse order for consistent traversal)
            List<EdgeInfo> neighbors = graph.getNeighbors(current);
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                EdgeInfo neighbor = neighbors.get(i);
                String neighborId = neighbor.getTargetNodeId();
                if (!visited.contains(neighborId)) {
                    frontier.push(neighborId);
                    if (!cameFrom.containsKey(neighborId)) {
                        cameFrom.put(neighborId, current);
                    }
                }
            }
        }

        // No path found
        long endTime = System.nanoTime();
        double executionTimeMs = (endTime - startTime) / 1_000_000.0;
        SimulationResult result = SimulationResult.failure(
            "No path found from " + startNodeId + " to " + goalNodeId, 
            getAlgorithmName(), trafficCondition);
        result.setNodesExplored(nodesExplored[0]);
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
