package com.vizag.ambulance.model;

/**
 * Represents an edge (road) connecting two nodes in the road network.
 */
public class Edge {
    private String from;
    private String to;
    private double distance;      // Base distance in km
    private String defaultTraffic; // LOW, MEDIUM, HIGH

    public Edge() {}

    public Edge(String from, String to, double distance, String defaultTraffic) {
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.defaultTraffic = defaultTraffic;
    }

    // Getters and Setters
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public String getDefaultTraffic() { return defaultTraffic; }
    public void setDefaultTraffic(String defaultTraffic) { this.defaultTraffic = defaultTraffic; }

    /**
     * Calculate the effective weight based on traffic condition.
     * Formula: weight = distance + traffic_factor
     * Traffic factors: LOW=1, MEDIUM=3, HIGH=7
     */
    public double getWeight(String trafficCondition) {
        int trafficFactor = switch (trafficCondition.toUpperCase()) {
            case "LOW" -> 1;
            case "MEDIUM" -> 3;
            case "HIGH" -> 7;
            default -> 3; // Default to medium
        };
        return distance + trafficFactor;
    }

    @Override
    public String toString() {
        return "Edge{" + from + " -> " + to + ", distance=" + distance + "km}";
    }
}
