# AI_1 — Web-Based Intelligent Emergency Ambulance Routing System

A Spring Boot + vanilla-JavaScript web application that demonstrates **five classic AI search algorithms** applied to real-world emergency vehicle routing in **Visakhapatnam, India**.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Algorithms Implemented](#algorithms-implemented)
4. [Road Network Data](#road-network-data)
5. [REST API](#rest-api)
6. [Frontend Features](#frontend-features)
7. [How to Run](#how-to-run)
8. [Project Structure](#project-structure)

---

## Project Overview

The system models a subset of Visakhapatnam's road network as a **weighted graph** and lets users route an ambulance from any starting point to any destination while choosing:

* a **traffic condition** (LOW / MEDIUM / HIGH) that inflates road costs, and
* a **search algorithm** to find the optimal or approximate shortest path.

It is intended as an educational tool that lets students compare how different algorithms perform on the same routing problem.

---

## Architecture

```
Browser (HTML/JS/CSS)
        │  HTTP (REST)
        ▼
Spring Boot 3.2 (Java 17)
 ├── RoutingController   ← REST endpoints
 ├── RoutingService      ← algorithm dispatch
 ├── GraphService        ← loads graph.json at startup
 └── algorithms/         ← BFS, DFS, UCS, Greedy BFS, A*
```

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2, Java 17, Maven |
| Frontend | HTML5, CSS3, vanilla JavaScript, SVG |
| Data | JSON graph file loaded via Jackson |

---

## Algorithms Implemented

| Algorithm | Optimal? | Complete? | Key Property |
|-----------|----------|-----------|--------------|
| **BFS** (Breadth-First Search) | Yes (unweighted) | Yes | Explores layer by layer |
| **DFS** (Depth-First Search) | No | No | Explores one branch fully before backtracking |
| **UCS** (Uniform Cost Search) | Yes | Yes | Priority queue ordered by cumulative cost g(n) |
| **Greedy Best-First Search** | No | No | Priority queue ordered by heuristic h(n) only |
| **A*** | Yes | Yes | f(n) = g(n) + h(n); combines UCS optimality with Greedy speed |

**Heuristic:** Euclidean distance between node coordinates.

**Traffic weight formula:**

```
edge_cost = distance_km + traffic_factor
  where traffic_factor: LOW = 1 | MEDIUM = 3 | HIGH = 7
```

---

## Road Network Data

The graph (`graph.json`) models 9 locations and 10 roads in Visakhapatnam:

### Nodes

| ID | Name | Type |
|----|------|------|
| G | GIMSR Hospital | Hospital |
| N | NAD Junction | Junction |
| S | Simhachalam | Landmark |
| H | Hanumanthawaka | Junction |
| L | Lawsons Bay Colony | Residential |
| A | Akkayapalem | Residential |
| M | Maddilapalem | Residential |
| R | RTC Complex | Bus Terminal |
| K | RK Beach | Landmark |

### Edges

| Road | Distance | Default Traffic |
|------|----------|-----------------|
| GIMSR → NAD Junction | 4 km | MEDIUM |
| GIMSR → Akkayapalem | 3 km | LOW |
| NAD Junction → Simhachalam | 6 km | HIGH |
| NAD Junction → Hanumanthawaka | 5 km | MEDIUM |
| NAD Junction → Maddilapalem | 3 km | MEDIUM |
| Hanumanthawaka → Lawsons Bay | 4 km | MEDIUM |
| Maddilapalem → RTC Complex | 4 km | HIGH |
| Akkayapalem → RTC Complex | 5 km | HIGH |
| Lawsons Bay → RK Beach | 2 km | LOW |
| RTC Complex → RK Beach | 6 km | MEDIUM |

---

## REST API

Base URL: `http://localhost:8080`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/graph` | Returns all nodes and edges for visualization |
| `POST` | `/api/simulate` | Run a routing simulation |
| `GET` | `/api/algorithms` | List available algorithm names |
| `GET` | `/api/traffic-conditions` | List traffic condition options |
| `GET` | `/api/health` | Health check with graph statistics |

### Simulate Request Body

```json
{
  "startNode": "G",
  "endNode": "K",
  "traffic": "MEDIUM",
  "algorithm": "A_STAR"
}
```

### Simulate Response

```json
{
  "path": ["G", "N", "M", "R", "K"],
  "totalCost": 22,
  "executionTime": 1,
  "nodesExplored": 7,
  "success": true,
  "algorithm": "A_STAR"
}
```

---

## Frontend Features

* **Interactive SVG graph** — nodes are colour-coded by type; edges are colour-coded by traffic level.
* **Algorithm selector** — choose among BFS, DFS, UCS, Greedy BFS, A*.
* **Traffic condition selector** — dynamically changes road weights.
* **Start / End node selectors** — click dropdowns or click a node on the canvas.
* **Route highlight** — the computed path glows blue on the graph.
* **Metrics panel** — shows total cost, execution time (ms), nodes explored, and the full path.
* **Algorithm info panel** — brief description of the selected algorithm's properties.

---

## How to Run

### Prerequisites

* Java 17+
* Maven 3.8+

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/chatti-sidhartha/AI_1.git
cd AI_1/ambulance-routing

# 2. Build and run
mvn spring-boot:run

# 3. Open the UI
# Navigate to http://localhost:8080 in your browser
```

---

## Project Structure

```
ambulance-routing/
├── pom.xml
└── src/main/
    ├── java/com/vizag/ambulancerouting/
    │   ├── AmbulanceRoutingApplication.java   # Entry point
    │   ├── controller/
    │   │   └── RoutingController.java         # REST endpoints
    │   ├── service/
    │   │   ├── GraphService.java              # Loads graph.json
    │   │   └── RoutingService.java            # Algorithm dispatch
    │   ├── model/
    │   │   ├── Node.java
    │   │   ├── Edge.java
    │   │   ├── Graph.java
    │   │   ├── SimulationRequest.java
    │   │   └── SimulationResult.java
    │   └── algorithm/
    │       ├── SearchAlgorithm.java           # Interface
    │       ├── BFSAlgorithm.java
    │       ├── DFSAlgorithm.java
    │       ├── UCSAlgorithm.java
    │       ├── GreedyBFSAlgorithm.java
    │       └── AStarAlgorithm.java
    └── resources/
        ├── application.properties
        ├── graph.json                         # Road network data
        └── static/
            └── index.html                     # Single-page UI
```