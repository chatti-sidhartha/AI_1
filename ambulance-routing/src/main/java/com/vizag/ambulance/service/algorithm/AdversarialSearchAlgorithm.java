package com.vizag.ambulance.service.algorithm;

import com.vizag.ambulance.model.Graph;
import com.vizag.ambulance.model.Graph.EdgeInfo;
import com.vizag.ambulance.model.SimulationResult;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Adversarial Search (Minimax-based) Algorithm for pathfinding.
 * 
 * Properties:
 * - Complete: Yes (in finite search spaces)
 * - Optimal: Yes (against optimal adversary)
 * - Time Complexity: O(b^m) where b is branching factor, m is max depth
 * - Space Complexity: O(bm)
 * 
 * Adapted for pathfinding: models the problem as a two-player game where
 * the "maximizer" tries to find the shortest path and the "minimizer" (adversary)
 * represents worst-case traffic/obstacles. The algorithm evaluates paths
 * considering adversarial conditions to find robust routes.
 */
@Component
public class AdversarialSearchAlgorithm implements SearchAlgorithm {

    private static final int MAX_DEPTH = 10;

    @Override
    public String getAlgorithmName() {
        return "Adversarial Search (Minimax)";
    }

    @Override
    public SimulationResult findPath(Graph graph, String startNodeId, String goalNodeId, String trafficCondition) {
        long startTime = System.nanoTime();

        if (graph.getNode(startNodeId) == null || graph.getNode(goalNodeId) == null) {
            return SimulationResult.failure("Invalid start or goal node", getAlgorithmName(), trafficCondition);
        }

        int[] nodesExplored = {0};
        Set<String> visited = new HashSet<>();
        visited.add(startNodeId);

        // Use minimax to find the best path
        MinimaxResult result = minimax(graph, startNodeId, goalNodeId, trafficCondition,
            MAX_DEPTH, true, visited, nodesExplored, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        long endTime = System.nanoTime();
        double executionTimeMs = (endTime - startTime) / 1_000_000.0;

        if (result.path == null || result.path.isEmpty()) {
            // Fallback: use BFS to find a path
            List<String> bfsPath = findPathBFS(graph, startNodeId, goalNodeId);
            if (bfsPath != null) {
                double cost = calculatePathCost(graph, bfsPath, trafficCondition);
                List<String> pathNames = bfsPath.stream()
                    .map(id -> graph.getNode(id).getName())
                    .toList();
                return SimulationResult.success(bfsPath, pathNames, cost, nodesExplored[0],
                    executionTimeMs, getAlgorithmName(), trafficCondition);
            }

            SimulationResult failResult = SimulationResult.failure(
                "No path found from " + startNodeId + " to " + goalNodeId,
                getAlgorithmName(), trafficCondition);
            failResult.setNodesExplored(nodesExplored[0]);
            failResult.setExecutionTimeMs(executionTimeMs);
            return failResult;
        }

        List<String> pathNames = result.path.stream()
            .map(id -> graph.getNode(id).getName())
            .toList();

        return SimulationResult.success(result.path, pathNames, result.cost, nodesExplored[0],
            executionTimeMs, getAlgorithmName(), trafficCondition);
    }

    /**
     * Minimax with alpha-beta pruning adapted for pathfinding.
     * Maximizer tries to minimize distance to goal.
     * Minimizer (adversary) tries to maximize cost (worst case).
     */
    private MinimaxResult minimax(Graph graph, String currentNode, String goalNode,
                                   String trafficCondition, int depth, boolean isMaximizer,
                                   Set<String> visited, int[] nodesExplored,
                                   double alpha, double beta) {
        nodesExplored[0]++;

        if (currentNode.equals(goalNode)) {
            List<String> path = new ArrayList<>();
            path.add(currentNode);
            return new MinimaxResult(path, 0);
        }

        if (depth == 0) {
            double heuristic = graph.euclideanDistance(currentNode, goalNode);
            return new MinimaxResult(null, heuristic);
        }

        List<EdgeInfo> neighbors = graph.getNeighbors(currentNode);
        if (neighbors.isEmpty()) {
            return new MinimaxResult(null, Double.MAX_VALUE);
        }

        MinimaxResult bestResult = null;

        if (isMaximizer) {
            // Maximizer wants the LOWEST cost path (best for ambulance)
            double bestValue = Double.MAX_VALUE;

            for (EdgeInfo neighbor : neighbors) {
                String neighborId = neighbor.getTargetNodeId();
                if (visited.contains(neighborId)) continue;

                visited.add(neighborId);
                double edgeCost = neighbor.getWeight(trafficCondition);

                MinimaxResult childResult = minimax(graph, neighborId, goalNode, trafficCondition,
                    depth - 1, !isMaximizer, visited, nodesExplored, alpha, beta);

                visited.remove(neighborId);

                if (childResult.path != null) {
                    double totalCost = edgeCost + childResult.cost;
                    if (totalCost < bestValue) {
                        bestValue = totalCost;
                        List<String> path = new ArrayList<>();
                        path.add(currentNode);
                        path.addAll(childResult.path);
                        bestResult = new MinimaxResult(path, totalCost);
                    }
                    beta = Math.min(beta, totalCost);
                    if (alpha >= beta) break; // Pruning
                }
            }
        } else {
            // Minimizer (adversary) selects path with HIGHEST cost (worst case)
            double bestValue = Double.NEGATIVE_INFINITY;

            for (EdgeInfo neighbor : neighbors) {
                String neighborId = neighbor.getTargetNodeId();
                if (visited.contains(neighborId)) continue;

                visited.add(neighborId);
                double edgeCost = neighbor.getWeight(trafficCondition);

                MinimaxResult childResult = minimax(graph, neighborId, goalNode, trafficCondition,
                    depth - 1, !isMaximizer, visited, nodesExplored, alpha, beta);

                visited.remove(neighborId);

                if (childResult.path != null) {
                    double totalCost = edgeCost + childResult.cost;
                    if (totalCost > bestValue) {
                        bestValue = totalCost;
                        List<String> path = new ArrayList<>();
                        path.add(currentNode);
                        path.addAll(childResult.path);
                        bestResult = new MinimaxResult(path, totalCost);
                    }
                    alpha = Math.max(alpha, totalCost);
                    if (alpha >= beta) break; // Pruning
                }
            }
        }

        return bestResult != null ? bestResult : new MinimaxResult(null, Double.MAX_VALUE);
    }

    private List<String> findPathBFS(Graph graph, String start, String goal) {
        Queue<String> queue = new LinkedList<>();
        Map<String, String> cameFrom = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);
        cameFrom.put(start, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(goal)) {
                List<String> path = new ArrayList<>();
                String node = goal;
                while (node != null) {
                    path.add(node);
                    node = cameFrom.get(node);
                }
                Collections.reverse(path);
                return path;
            }
            for (EdgeInfo n : graph.getNeighbors(current)) {
                if (!visited.contains(n.getTargetNodeId())) {
                    visited.add(n.getTargetNodeId());
                    queue.add(n.getTargetNodeId());
                    cameFrom.put(n.getTargetNodeId(), current);
                }
            }
        }
        return null;
    }

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

    private static class MinimaxResult {
        List<String> path;
        double cost;

        MinimaxResult(List<String> path, double cost) {
            this.path = path;
            this.cost = cost;
        }
    }
}
