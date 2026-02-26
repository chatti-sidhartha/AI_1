package com.vizag.ambulance.controller;

import com.vizag.ambulance.model.Graph;
import com.vizag.ambulance.model.SimulationRequest;
import com.vizag.ambulance.model.SimulationResult;
import com.vizag.ambulance.service.GraphService;
import com.vizag.ambulance.service.RoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for the Ambulance Routing System.
 * Handles API requests from the frontend.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow frontend access
public class RoutingController {

    private final GraphService graphService;
    private final RoutingService routingService;

    public RoutingController(GraphService graphService, RoutingService routingService) {
        this.graphService = graphService;
        this.routingService = routingService;
    }

    /**
     * GET /api/graph
     * Returns the complete graph structure for visualization.
     */
    @GetMapping("/graph")
    public ResponseEntity<Graph> getGraph() {
        return ResponseEntity.ok(graphService.getGraph());
    }

    /**
     * POST /api/simulate
     * Runs a pathfinding simulation with the given parameters.
     * 
     * Request body example:
     * {
     *   "startNode": "G",
     *   "endNode": "K",
     *   "traffic": "MEDIUM",
     *   "algorithm": "A_STAR"
     * }
     */
    @PostMapping("/simulate")
    public ResponseEntity<SimulationResult> simulate(@RequestBody SimulationRequest request) {
        SimulationResult result = routingService.runSimulation(request);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * GET /api/algorithms
     * Returns the list of available algorithms.
     */
    @GetMapping("/algorithms")
    public ResponseEntity<String[]> getAlgorithms() {
        return ResponseEntity.ok(routingService.getAvailableAlgorithms());
    }

    /**
     * GET /api/traffic-conditions
     * Returns the list of available traffic conditions.
     */
    @GetMapping("/traffic-conditions")
    public ResponseEntity<String[]> getTrafficConditions() {
        return ResponseEntity.ok(routingService.getTrafficConditions());
    }

    /**
     * GET /api/health
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Visakhapatnam Ambulance Routing System",
            "nodes", String.valueOf(graphService.getGraph().getNodes().size()),
            "edges", String.valueOf(graphService.getGraph().getEdges().size())
        ));
    }
}
