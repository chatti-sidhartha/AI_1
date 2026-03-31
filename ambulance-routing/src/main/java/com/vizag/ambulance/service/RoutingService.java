package com.vizag.ambulance.service;

import com.vizag.ambulance.model.SimulationRequest;
import com.vizag.ambulance.model.SimulationResult;
import com.vizag.ambulance.service.algorithm.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for executing pathfinding simulations with different algorithms.
 */
@Service
public class RoutingService {

    private final GraphService graphService;
    private final Map<String, SearchAlgorithm> algorithms;

    public RoutingService(
            GraphService graphService,
            DFSAlgorithm dfs,
            UCSAlgorithm ucs,
            GreedyBFSAlgorithm greedy,
            AStarAlgorithm aStar,
            GeneticAlgorithm genetic,
            AdversarialSearchAlgorithm adversarial) {
        
        this.graphService = graphService;
        Map<String, SearchAlgorithm> map = new HashMap<>();
        map.put("DFS", dfs);
        map.put("UCS", ucs);
        map.put("GREEDY", greedy);
        map.put("A_STAR", aStar);
        map.put("GENETIC", genetic);
        map.put("ADVERSARIAL", adversarial);
        this.algorithms = map;
    }

    /**
     * Run a pathfinding simulation with the specified parameters.
     */
    public SimulationResult runSimulation(SimulationRequest request) {
        // Validate algorithm
        SearchAlgorithm algorithm = algorithms.get(request.getAlgorithm().toUpperCase());
        if (algorithm == null) {
            return SimulationResult.failure(
                "Unknown algorithm: " + request.getAlgorithm() + 
                ". Valid options: DFS, UCS, GREEDY, A_STAR, GENETIC, ADVERSARIAL",
                request.getAlgorithm(),
                request.getTraffic()
            );
        }

        // Validate traffic condition
        String traffic = request.getTraffic().toUpperCase();
        if (!traffic.equals("LOW") && !traffic.equals("MEDIUM") && !traffic.equals("HIGH")) {
            return SimulationResult.failure(
                "Invalid traffic condition: " + request.getTraffic() + 
                ". Valid options: LOW, MEDIUM, HIGH",
                request.getAlgorithm(),
                request.getTraffic()
            );
        }

        // Execute the algorithm
        return algorithm.findPath(
            graphService.getGraph(),
            request.getStartNode(),
            request.getEndNode(),
            traffic
        );
    }

    /**
     * Get available algorithm names.
     */
    public String[] getAvailableAlgorithms() {
        return new String[]{"DFS", "UCS", "GREEDY", "A_STAR", "GENETIC", "ADVERSARIAL"};
    }

    /**
     * Get available traffic conditions.
     */
    public String[] getTrafficConditions() {
        return new String[]{"LOW", "MEDIUM", "HIGH"};
    }
}
