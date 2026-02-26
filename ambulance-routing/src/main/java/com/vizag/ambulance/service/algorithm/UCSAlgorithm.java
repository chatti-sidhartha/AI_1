package com.vizag.ambulance.service.algorithm;

import com.vizag.ambulance.model.Graph;
import com.vizag.ambulance.model.Graph.EdgeInfo;
import com.vizag.ambulance.model.SimulationResult;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Uniform Cost Search (UCS) Algorithm.
 * 
 * Properties:
 * - Complete: Yes
 * - Optimal: Yes (finds the lowest cost path)
 * - Time Complexity: O(E log V) with priority queue
 * - Space Complexity: O(V)
 * 
 * UCS expands nodes in order of their path cost from the start.
 * It guarantees the optimal solution for weighted graphs.
 */
@Component
public class UCSAlgorithm implements SearchAlgorithm {

    @Override
    public String getAlgorithmName() {
        return "UCS (Uniform Cost Search)";
    }

    @Override
    public SimulationResult findPath(Graph graph, String startNodeId, String goalNodeId, String trafficCondition) {
        long startTime = System.nanoTime();
        int nodesExplored = 0;

        // Validate nodes exist
        if (graph.getNode(startNodeId) == null || graph.getNode(goalNodeId) == null) {
            return SimulationResult.failure("Invalid start or goal node", getAlgorithmName(), trafficCondition);
        }

        // Priority queue ordered by path cost (g cost)
        PriorityQueue<NodeCost> frontier = new PriorityQueue<>(Comparator.comparingDouble(nc -> nc.cost));
        Map<String, Double> costSoFar = new HashMap<>();
        Map<String, String> cameFrom = new HashMap<>();

        frontier.add(new NodeCost(startNodeId, 0));
        costSoFar.put(startNodeId, 0.0);
        cameFrom.put(startNodeId, null);

        while (!frontier.isEmpty()) {
            NodeCost current = frontier.poll();
            nodesExplored++;

            // Goal check
            if (current.nodeId.equals(goalNodeId)) {
                long endTime = System.nanoTime();
                double executionTimeMs = (endTime - startTime) / 1_000_000.0;

                // Reconstruct path
                List<String> path = reconstructPath(cameFrom, goalNodeId);
                List<String> pathNames = path.stream()
                    .map(id -> graph.getNode(id).getName())
                    .toList();

                return SimulationResult.success(path, pathNames, current.cost, nodesExplored, 
                    executionTimeMs, getAlgorithmName(), trafficCondition);
            }

            // Skip if we've found a better path to this node already
            if (current.cost > costSoFar.getOrDefault(current.nodeId, Double.MAX_VALUE)) {
                continue;
            }

            // Explore neighbors
            for (EdgeInfo neighbor : graph.getNeighbors(current.nodeId)) {
                String neighborId = neighbor.getTargetNodeId();
                double newCost = current.cost + neighbor.getWeight(trafficCondition);

                if (newCost < costSoFar.getOrDefault(neighborId, Double.MAX_VALUE)) {
                    costSoFar.put(neighborId, newCost);
                    cameFrom.put(neighborId, current.nodeId);
                    frontier.add(new NodeCost(neighborId, newCost));
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
     * Helper class to store node with its cost for priority queue.
     */
    private static class NodeCost {
        String nodeId;
        double cost;

        NodeCost(String nodeId, double cost) {
            this.nodeId = nodeId;
            this.cost = cost;
        }
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
}
