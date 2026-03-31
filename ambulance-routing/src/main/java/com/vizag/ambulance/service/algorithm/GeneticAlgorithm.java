package com.vizag.ambulance.service.algorithm;

import com.vizag.ambulance.model.Graph;
import com.vizag.ambulance.model.Graph.EdgeInfo;
import com.vizag.ambulance.model.SimulationResult;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Genetic Algorithm for pathfinding.
 * 
 * Properties:
 * - Complete: No (probabilistic)
 * - Optimal: No (approximate, evolutionary approach)
 * - Time Complexity: O(generations × populationSize × pathLength)
 * - Space Complexity: O(populationSize × pathLength)
 * 
 * Uses evolutionary principles - selection, crossover, mutation -
 * to evolve a population of candidate paths toward optimal solutions.
 */
@Component
public class GeneticAlgorithm implements SearchAlgorithm {

    private static final int POPULATION_SIZE = 50;
    private static final int MAX_GENERATIONS = 200;
    private static final double MUTATION_RATE = 0.3;
    private static final double CROSSOVER_RATE = 0.7;

    @Override
    public String getAlgorithmName() {
        return "Genetic Algorithm";
    }

    @Override
    public SimulationResult findPath(Graph graph, String startNodeId, String goalNodeId, String trafficCondition) {
        long startTime = System.nanoTime();
        int nodesExplored = 0;

        if (graph.getNode(startNodeId) == null || graph.getNode(goalNodeId) == null) {
            return SimulationResult.failure("Invalid start or goal node", getAlgorithmName(), trafficCondition);
        }

        Random random = new Random(42);

        // Generate initial population of valid paths
        List<List<String>> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            List<String> path = generateRandomPath(graph, startNodeId, goalNodeId, random);
            if (path != null) {
                population.add(path);
                nodesExplored += path.size();
            }
        }

        if (population.isEmpty()) {
            // Try one deterministic path
            List<String> initialPath = findPathBFS(graph, startNodeId, goalNodeId);
            if (initialPath == null) {
                long endTime = System.nanoTime();
                double executionTimeMs = (endTime - startTime) / 1_000_000.0;
                SimulationResult result = SimulationResult.failure(
                    "No path found from " + startNodeId + " to " + goalNodeId,
                    getAlgorithmName(), trafficCondition);
                result.setNodesExplored(nodesExplored);
                result.setExecutionTimeMs(executionTimeMs);
                return result;
            }
            population.add(initialPath);
            nodesExplored += initialPath.size();
        }

        // Evolve over generations
        for (int gen = 0; gen < MAX_GENERATIONS; gen++) {
            // Calculate fitness for each individual (lower cost = higher fitness)
            List<Double> fitness = new ArrayList<>();
            for (List<String> path : population) {
                double cost = calculatePathCost(graph, path, trafficCondition);
                fitness.add(1.0 / (1.0 + cost));
            }

            List<List<String>> newPopulation = new ArrayList<>();

            // Elitism: keep the best solution
            int bestIdx = 0;
            for (int i = 1; i < fitness.size(); i++) {
                if (fitness.get(i) > fitness.get(bestIdx)) bestIdx = i;
            }
            newPopulation.add(new ArrayList<>(population.get(bestIdx)));

            while (newPopulation.size() < POPULATION_SIZE) {
                // Selection: tournament selection
                List<String> parent1 = tournamentSelect(population, fitness, random);
                List<String> parent2 = tournamentSelect(population, fitness, random);

                List<String> child;
                if (random.nextDouble() < CROSSOVER_RATE) {
                    child = crossover(graph, parent1, parent2, startNodeId, goalNodeId, random);
                } else {
                    child = new ArrayList<>(parent1);
                }

                if (child != null && random.nextDouble() < MUTATION_RATE) {
                    child = mutate(graph, child, startNodeId, goalNodeId, random);
                }

                if (child != null && isValidPath(graph, child)) {
                    newPopulation.add(child);
                    nodesExplored++;
                }
            }

            population = newPopulation;
        }

        // Find the best path in final population
        List<String> bestPath = null;
        double bestCost = Double.MAX_VALUE;
        for (List<String> path : population) {
            double cost = calculatePathCost(graph, path, trafficCondition);
            if (cost < bestCost) {
                bestCost = cost;
                bestPath = path;
            }
        }

        long endTime = System.nanoTime();
        double executionTimeMs = (endTime - startTime) / 1_000_000.0;

        if (bestPath == null) {
            SimulationResult result = SimulationResult.failure(
                "No path found from " + startNodeId + " to " + goalNodeId,
                getAlgorithmName(), trafficCondition);
            result.setNodesExplored(nodesExplored);
            result.setExecutionTimeMs(executionTimeMs);
            return result;
        }

        List<String> pathNames = bestPath.stream()
            .map(id -> graph.getNode(id).getName())
            .toList();

        return SimulationResult.success(bestPath, pathNames, bestCost, nodesExplored,
            executionTimeMs, getAlgorithmName(), trafficCondition);
    }

    private List<String> generateRandomPath(Graph graph, String start, String goal, Random random) {
        List<String> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        String current = start;
        path.add(current);
        visited.add(current);

        int maxSteps = 50;
        while (!current.equals(goal) && maxSteps-- > 0) {
            List<EdgeInfo> neighbors = graph.getNeighbors(current);
            List<EdgeInfo> unvisited = new ArrayList<>();
            for (EdgeInfo n : neighbors) {
                if (!visited.contains(n.getTargetNodeId())) {
                    unvisited.add(n);
                }
            }
            if (unvisited.isEmpty()) return null;

            EdgeInfo chosen = unvisited.get(random.nextInt(unvisited.size()));
            current = chosen.getTargetNodeId();
            path.add(current);
            visited.add(current);
        }

        return current.equals(goal) ? path : null;
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

    private List<String> tournamentSelect(List<List<String>> population, List<Double> fitness, Random random) {
        int i = random.nextInt(population.size());
        int j = random.nextInt(population.size());
        return fitness.get(i) >= fitness.get(j) ? population.get(i) : population.get(j);
    }

    private List<String> crossover(Graph graph, List<String> parent1, List<String> parent2,
                                    String start, String goal, Random random) {
        // Find common nodes between parents
        Set<String> common = new HashSet<>(parent1);
        common.retainAll(new HashSet<>(parent2));
        common.remove(start);
        common.remove(goal);

        if (common.isEmpty()) return new ArrayList<>(parent1);

        // Pick a random common node as crossover point
        List<String> commonList = new ArrayList<>(common);
        String crossPoint = commonList.get(random.nextInt(commonList.size()));

        // Take first part from parent1, second from parent2
        int idx1 = parent1.indexOf(crossPoint);
        int idx2 = parent2.indexOf(crossPoint);

        List<String> child = new ArrayList<>(parent1.subList(0, idx1));
        child.addAll(parent2.subList(idx2, parent2.size()));

        // Remove cycles
        Set<String> seen = new HashSet<>();
        List<String> cleanChild = new ArrayList<>();
        for (String node : child) {
            if (!seen.contains(node) || node.equals(goal)) {
                cleanChild.add(node);
                seen.add(node);
                if (node.equals(goal)) break;
            }
        }

        return cleanChild.get(cleanChild.size() - 1).equals(goal) ? cleanChild : null;
    }

    private List<String> mutate(Graph graph, List<String> path, String start, String goal, Random random) {
        if (path.size() <= 2) return path;

        int idx = 1 + random.nextInt(path.size() - 2);
        String nodeBefore = path.get(idx - 1);

        List<EdgeInfo> neighbors = graph.getNeighbors(nodeBefore);
        if (neighbors.isEmpty()) return path;

        EdgeInfo chosen = neighbors.get(random.nextInt(neighbors.size()));
        String newNode = chosen.getTargetNodeId();

        // Find path from newNode to goal
        List<String> pathToGoal = findPathBFS(graph, newNode, goal);
        if (pathToGoal == null) return path;

        List<String> mutated = new ArrayList<>(path.subList(0, idx));
        Set<String> visited = new HashSet<>(mutated);
        for (String node : pathToGoal) {
            if (!visited.contains(node) || node.equals(goal)) {
                mutated.add(node);
                visited.add(node);
                if (node.equals(goal)) break;
            }
        }

        return mutated.get(mutated.size() - 1).equals(goal) ? mutated : path;
    }

    private boolean isValidPath(Graph graph, List<String> path) {
        if (path == null || path.size() < 2) return false;
        for (int i = 0; i < path.size() - 1; i++) {
            String current = path.get(i);
            String next = path.get(i + 1);
            boolean connected = false;
            for (EdgeInfo edge : graph.getNeighbors(current)) {
                if (edge.getTargetNodeId().equals(next)) {
                    connected = true;
                    break;
                }
            }
            if (!connected) return false;
        }
        return true;
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
}
