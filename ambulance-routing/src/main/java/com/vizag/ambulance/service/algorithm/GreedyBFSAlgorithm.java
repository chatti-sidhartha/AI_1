package com.vizag.ambulance.service.algorithm;

import com.vizag.ambulance.model.Graph;
import com.vizag.ambulance.model.Graph.EdgeInfo;
import com.vizag.ambulance.model.SimulationResult;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Greedy Best-First Search Algorithm.
 * 
 * Properties:
 * - Complete: No (can get stuck in loops without proper handling)
 * - Optimal: No (only considers heuristic, not actual cost)
 * - Time Complexity: O(b^m) where b is branching factor, m is max depth
 * - Space Complexity: O(b^m)
 * 
 * Greedy BFS expands nodes based solely on the heuristic (estimated distance to goal).
 * It's fast but doesn't guarantee optimal paths.
 */
@Component
public class GreedyBFSAlgorithm implements SearchAlgorithm {

    @Override
    public String getAlgorithmName() {
        return "Greedy Best-First Search";
    }

    @Override
    public SimulationResult findPath(Graph graph, String startNodeId, String goalNodeId, String trafficCondition) {
        long startTime = System.nanoTime();
        int nodesExplored = 0;

        // Validate nodes exist
        if (graph.getNode(startNodeId) == null || graph.getNode(goalNodeId) == null) {
            return SimulationResult.failure("Invalid start or goal node", getAlgorithmName(), trafficCondition);
        }

        // Priority queue ordered by heuristic value only (h cost)
        PriorityQueue<NodeHeuristic> frontier = new PriorityQueue<>(
            Comparator.comparingDouble(nh -> nh.heuristic));
        
        Map<String, String> cameFrom = new HashMap<>();
        Set<String> visited = new HashSet<>();

        double initialHeuristic = graph.euclideanDistance(startNodeId, goalNodeId);
        frontier.add(new NodeHeuristic(startNodeId, initialHeuristic));
        cameFrom.put(startNodeId, null);

        while (!frontier.isEmpty()) {
            NodeHeuristic current = frontier.poll();
            
            if (visited.contains(current.nodeId)) {
                continue;
            }
            
            visited.add(current.nodeId);
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

                // Calculate actual cost of the path found
                double totalCost = calculatePathCost(graph, path, trafficCondition);

                return SimulationResult.success(path, pathNames, totalCost, nodesExplored, 
                    executionTimeMs, getAlgorithmName(), trafficCondition);
            }

            // Explore neighbors
            for (EdgeInfo neighbor : graph.getNeighbors(current.nodeId)) {
                String neighborId = neighbor.getTargetNodeId();
                if (!visited.contains(neighborId)) {
                    double heuristic = graph.euclideanDistance(neighborId, goalNodeId);
                    frontier.add(new NodeHeuristic(neighborId, heuristic));
                    if (!cameFrom.containsKey(neighborId)) {
                        cameFrom.put(neighborId, current.nodeId);
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
        result.setNodesExplored(nodesExplored);
        result.setExecutionTimeMs(executionTimeMs);
        return result;
    }

    /**
     * Helper class to store node with its heuristic value for priority queue.
     */
    private static class NodeHeuristic {
        String nodeId;
        double heuristic;

        NodeHeuristic(String nodeId, double heuristic) {
            this.nodeId = nodeId;
            this.heuristic = heuristic;
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
