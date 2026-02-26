package com.vizag.ambulance.service.algorithm;

import com.vizag.ambulance.model.Graph;
import com.vizag.ambulance.model.Graph.EdgeInfo;
import com.vizag.ambulance.model.SimulationResult;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * A* (A-Star) Search Algorithm.
 * 
 * Properties:
 * - Complete: Yes
 * - Optimal: Yes (with admissible heuristic)
 * - Time Complexity: O(E log V) with priority queue
 * - Space Complexity: O(V)
 * 
 * A* uses f(n) = g(n) + h(n) where:
 * - g(n) = actual cost from start to current node
 * - h(n) = heuristic estimate from current to goal (Euclidean distance)
 * 
 * Combines the benefits of UCS (optimality) and Greedy BFS (speed).
 */
@Component
public class AStarAlgorithm implements SearchAlgorithm {

    @Override
    public String getAlgorithmName() {
        return "A* Search";
    }

    @Override
    public SimulationResult findPath(Graph graph, String startNodeId, String goalNodeId, String trafficCondition) {
        long startTime = System.nanoTime();
        int nodesExplored = 0;

        // Validate nodes exist
        if (graph.getNode(startNodeId) == null || graph.getNode(goalNodeId) == null) {
            return SimulationResult.failure("Invalid start or goal node", getAlgorithmName(), trafficCondition);
        }

        // Priority queue ordered by f = g + h
        PriorityQueue<NodePriority> frontier = new PriorityQueue<>(
            Comparator.comparingDouble(np -> np.fCost));
        
        Map<String, Double> gCost = new HashMap<>();  // Actual cost from start
        Map<String, String> cameFrom = new HashMap<>();
        Set<String> closedSet = new HashSet<>();

        double hStart = graph.euclideanDistance(startNodeId, goalNodeId);
        frontier.add(new NodePriority(startNodeId, 0, hStart, 0 + hStart));
        gCost.put(startNodeId, 0.0);
        cameFrom.put(startNodeId, null);

        while (!frontier.isEmpty()) {
            NodePriority current = frontier.poll();
            
            // Skip if already processed with better cost
            if (closedSet.contains(current.nodeId)) {
                continue;
            }
            
            closedSet.add(current.nodeId);
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

                return SimulationResult.success(path, pathNames, current.gCost, nodesExplored, 
                    executionTimeMs, getAlgorithmName(), trafficCondition);
            }

            // Explore neighbors
            for (EdgeInfo neighbor : graph.getNeighbors(current.nodeId)) {
                String neighborId = neighbor.getTargetNodeId();
                
                if (closedSet.contains(neighborId)) {
                    continue;
                }

                double tentativeG = current.gCost + neighbor.getWeight(trafficCondition);

                if (tentativeG < gCost.getOrDefault(neighborId, Double.MAX_VALUE)) {
                    // This path to neighbor is better
                    gCost.put(neighborId, tentativeG);
                    cameFrom.put(neighborId, current.nodeId);
                    
                    double h = graph.euclideanDistance(neighborId, goalNodeId);
                    double f = tentativeG + h;
                    
                    frontier.add(new NodePriority(neighborId, tentativeG, h, f));
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
     * Helper class to store node with its priority values for the queue.
     */
    private static class NodePriority {
        String nodeId;
        double gCost;   // Actual cost from start
        double hCost;   // Heuristic estimate to goal
        double fCost;   // f = g + h

        NodePriority(String nodeId, double gCost, double hCost, double fCost) {
            this.nodeId = nodeId;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = fCost;
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
