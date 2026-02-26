package com.vizag.ambulance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vizag.ambulance.model.Graph;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * Service for loading and managing the Visakhapatnam road network graph.
 */
@Service
public class GraphService {

    private Graph graph;
    private final ObjectMapper objectMapper;

    public GraphService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Load the graph from JSON file on startup.
     */
    @PostConstruct
    public void init() {
        loadGraph();
    }

    /**
     * Load graph from the graph.json resource file.
     */
    public void loadGraph() {
        try {
            ClassPathResource resource = new ClassPathResource("graph.json");
            InputStream inputStream = resource.getInputStream();
            graph = objectMapper.readValue(inputStream, Graph.class);
            graph.buildAdjacencyList();
            System.out.println("Graph loaded successfully: " + 
                graph.getNodes().size() + " nodes, " + 
                graph.getEdges().size() + " edges");
        } catch (IOException e) {
            System.err.println("Error loading graph: " + e.getMessage());
            throw new RuntimeException("Failed to load graph.json", e);
        }
    }

    /**
     * Get the loaded graph.
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Reload the graph (useful for testing).
     */
    public void reloadGraph() {
        loadGraph();
    }
}
