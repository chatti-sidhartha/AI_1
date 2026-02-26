package com.vizag.ambulance.model;

/**
 * Represents a node (location) in the Visakhapatnam road network.
 */
public class Node {
    private String id;
    private String name;
    private double x;  // Latitude or x-coordinate for visualization
    private double y;  // Longitude or y-coordinate for visualization
    private String type; // Hospital, Junction, Residential, Landmark

    public Node() {}

    public Node(String id, String name, double x, double y, String type) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.type = type;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return "Node{id='" + id + "', name='" + name + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id.equals(node.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
