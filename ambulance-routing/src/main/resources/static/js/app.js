/**
 * Visakhapatnam Ambulance Routing System - Frontend JavaScript
 * Handles graph visualization, API communication, and UI interactions.
 */

// ===== Global State =====
let graphData = null;
let currentPath = [];

// ===== API Base URL =====
const API_BASE = '/api';

// ===== DOM Elements =====
const elements = {
    startNode: document.getElementById('startNode'),
    endNode: document.getElementById('endNode'),
    traffic: document.getElementById('traffic'),
    algorithm: document.getElementById('algorithm'),
    findRouteBtn: document.getElementById('findRouteBtn'),
    clearBtn: document.getElementById('clearBtn'),
    graphCanvas: document.getElementById('graphCanvas'),
    resultsContainer: document.getElementById('resultsContainer'),
    pathDisplay: document.getElementById('pathDisplay'),
    pathSteps: document.getElementById('pathSteps'),
    algorithmDetails: document.getElementById('algorithmDetails'),
    errorToast: document.getElementById('errorToast'),
    errorMessage: document.getElementById('errorMessage'),
    successToast: document.getElementById('successToast'),
    successMessage: document.getElementById('successMessage')
};

// ===== Algorithm Information =====
const algorithmInfo = {
    DFS: {
        name: 'DFS (Depth-First Search)',
        props: ['Complete: Yes', 'Optimal: No', 'Time: O(V+E)', 'Space: O(V)']
    },
    UCS: {
        name: 'UCS (Uniform Cost Search)',
        props: ['Complete: Yes', 'Optimal: Yes', 'Time: O(E log V)', 'Space: O(V)']
    },
    GREEDY: {
        name: 'Greedy Best-First Search',
        props: ['Complete: No', 'Optimal: No', 'Uses: h(n) only', 'Fast but suboptimal']
    },
    A_STAR: {
        name: 'A* Search',
        props: ['Complete: Yes', 'Optimal: Yes', 'Uses: f(n) = g(n) + h(n)', 'Best for weighted graphs']
    },
    GENETIC: {
        name: 'Genetic Algorithm',
        props: ['Complete: No', 'Optimal: No (evolutionary)', 'Population-based', 'Uses crossover & mutation']
    },
    ADVERSARIAL: {
        name: 'Adversarial Search (Minimax)',
        props: ['Complete: Yes', 'Optimal: Yes (vs adversary)', 'Alpha-Beta pruning', 'Game-theoretic approach']
    }
};

// ===== Initialization =====
document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
});

async function initializeApp() {
    try {
        // Load graph data
        await loadGraph();
        
        // Set up event listeners
        setupEventListeners();
        
        // Render initial graph
        renderGraph();
        
        console.log('Application initialized successfully');
    } catch (error) {
        console.error('Initialization failed:', error);
        showError('Failed to initialize application. Please refresh the page.');
    }
}

// ===== API Functions =====
async function loadGraph() {
    const response = await fetch(`${API_BASE}/graph`);
    if (!response.ok) {
        throw new Error('Failed to load graph data');
    }
    graphData = await response.json();
    
    // Populate node dropdowns
    populateNodeDropdowns();
}

async function runSimulation(startNode, endNode, traffic, algorithm) {
    const response = await fetch(`${API_BASE}/simulate`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            startNode,
            endNode,
            traffic,
            algorithm
        })
    });
    
    const result = await response.json();
    return result;
}

// ===== Event Listeners =====
function setupEventListeners() {
    // Find Route button
    elements.findRouteBtn.addEventListener('click', handleFindRoute);
    
    // Clear button
    elements.clearBtn.addEventListener('click', handleClearGraph);
    
    // Algorithm selection change
    elements.algorithm.addEventListener('change', updateAlgorithmInfo);
    
    // Initialize algorithm info
    updateAlgorithmInfo();
}

async function handleFindRoute() {
    const startNode = elements.startNode.value;
    const endNode = elements.endNode.value;
    const traffic = elements.traffic.value;
    const algorithm = elements.algorithm.value;
    
    // Validation
    if (!startNode || !endNode) {
        showError('Please select both start and end locations');
        return;
    }
    
    if (startNode === endNode) {
        showError('Start and end locations must be different');
        return;
    }
    
    // Show loading state
    setLoading(true);
    
    try {
        const result = await runSimulation(startNode, endNode, traffic, algorithm);
        
        if (result.success) {
            // Display results
            displayResults(result);
            
            // Highlight path on graph
            highlightPath(result.path);
            
            showSuccess('Route found successfully!');
        } else {
            showError(result.errorMessage || 'Failed to find a route');
            displayFailure(result);
        }
    } catch (error) {
        console.error('Simulation error:', error);
        showError('An error occurred while finding the route');
    } finally {
        setLoading(false);
    }
}

function handleClearGraph() {
    currentPath = [];
    renderGraph();
    
    // Reset results display
    elements.resultsContainer.innerHTML = '<p class="placeholder-text">Run a simulation to see results</p>';
    elements.pathDisplay.style.display = 'none';
    elements.pathSteps.innerHTML = '';
}

function updateAlgorithmInfo() {
    const algorithm = elements.algorithm.value;
    const info = algorithmInfo[algorithm];
    
    if (info) {
        elements.algorithmDetails.innerHTML = `
            <p><strong>${info.name}</strong></p>
            <ul>
                ${info.props.map(prop => `<li>${prop}</li>`).join('')}
            </ul>
        `;
    }
}

// ===== UI Functions =====
function populateNodeDropdowns() {
    const nodeOptions = graphData.nodes.map(node => 
        `<option value="${node.id}">${node.name} (${node.id})</option>`
    ).join('');
    
    elements.startNode.innerHTML = '<option value="">-- Select Start --</option>' + nodeOptions;
    elements.endNode.innerHTML = '<option value="">-- Select End --</option>' + nodeOptions;
    
    // Default: GIMSR as start (hospital)
    elements.startNode.value = 'G';
}

function setLoading(isLoading) {
    elements.findRouteBtn.disabled = isLoading;
    const btnText = elements.findRouteBtn.querySelector('.btn-text');
    const btnLoader = elements.findRouteBtn.querySelector('.btn-loader');
    
    if (isLoading) {
        btnText.style.display = 'none';
        btnLoader.style.display = 'inline';
    } else {
        btnText.style.display = 'inline';
        btnLoader.style.display = 'none';
    }
}

function displayResults(result) {
    elements.resultsContainer.innerHTML = `
        <div class="metric-card success">
            <div class="label">Total Path Cost</div>
            <div class="value">${result.totalCost.toFixed(2)}<span class="unit">units</span></div>
        </div>
        <div class="metric-card info">
            <div class="label">Execution Time</div>
            <div class="value">${result.executionTimeMs.toFixed(3)}<span class="unit">ms</span></div>
        </div>
        <div class="metric-card warning">
            <div class="label">Nodes Explored</div>
            <div class="value">${result.nodesExplored}<span class="unit">nodes</span></div>
        </div>
        <div class="metric-card info">
            <div class="label">Algorithm Used</div>
            <div class="value" style="font-size: 1rem;">${result.algorithm}</div>
        </div>
        <div class="metric-card">
            <div class="label">Traffic Condition</div>
            <div class="value" style="font-size: 1rem;">${result.traffic}</div>
        </div>
    `;
    
    // Display path
    elements.pathDisplay.style.display = 'block';
    elements.pathSteps.innerHTML = result.pathNames.map((name, index) => {
        const step = `<span class="path-step">${name}</span>`;
        const arrow = index < result.pathNames.length - 1 ? '<span class="path-arrow">→</span>' : '';
        return step + arrow;
    }).join('');
}

function displayFailure(result) {
    elements.resultsContainer.innerHTML = `
        <div class="metric-card" style="border-left-color: var(--error-color);">
            <div class="label">Status</div>
            <div class="value" style="font-size: 1rem; color: var(--error-color);">No Path Found</div>
        </div>
        <div class="metric-card warning">
            <div class="label">Nodes Explored</div>
            <div class="value">${result.nodesExplored || 0}<span class="unit">nodes</span></div>
        </div>
        <div class="metric-card info">
            <div class="label">Execution Time</div>
            <div class="value">${(result.executionTimeMs || 0).toFixed(3)}<span class="unit">ms</span></div>
        </div>
    `;
    elements.pathDisplay.style.display = 'none';
}

// ===== Graph Rendering =====
function renderGraph() {
    if (!graphData) return;
    
    const svg = elements.graphCanvas;
    svg.innerHTML = '';
    
    // Create groups for layering
    const edgesGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    const pathGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    const nodesGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    const labelsGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    
    edgesGroup.id = 'edges';
    pathGroup.id = 'path';
    nodesGroup.id = 'nodes';
    labelsGroup.id = 'labels';
    
    // Get current traffic setting for edge colors
    const currentTraffic = elements.traffic.value;
    
    // Draw edges
    graphData.edges.forEach(edge => {
        const fromNode = graphData.nodes.find(n => n.id === edge.from);
        const toNode = graphData.nodes.find(n => n.id === edge.to);
        
        if (fromNode && toNode) {
            // Edge line
            const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
            line.setAttribute('x1', fromNode.x);
            line.setAttribute('y1', fromNode.y);
            line.setAttribute('x2', toNode.x);
            line.setAttribute('y2', toNode.y);
            line.setAttribute('class', `edge-line ${edge.defaultTraffic.toLowerCase()}`);
            line.setAttribute('data-from', edge.from);
            line.setAttribute('data-to', edge.to);
            edgesGroup.appendChild(line);
            
            // Edge label (distance)
            const midX = (fromNode.x + toNode.x) / 2;
            const midY = (fromNode.y + toNode.y) / 2;
            
            const label = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            label.setAttribute('x', midX);
            label.setAttribute('y', midY - 8);
            label.setAttribute('class', 'edge-label');
            label.textContent = `${edge.distance}km`;
            labelsGroup.appendChild(label);
        }
    });
    
    // Draw nodes
    graphData.nodes.forEach(node => {
        // Node circle
        const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
        circle.setAttribute('cx', node.x);
        circle.setAttribute('cy', node.y);
        circle.setAttribute('r', 20);
        circle.setAttribute('class', `node-circle ${node.type.toLowerCase().replace(' ', '-')}`);
        circle.setAttribute('data-id', node.id);
        
        // Click to select node
        circle.addEventListener('click', () => selectNode(node.id));
        
        nodesGroup.appendChild(circle);
        
        // Node ID inside circle
        const idText = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        idText.setAttribute('x', node.x);
        idText.setAttribute('y', node.y + 4);
        idText.setAttribute('class', 'node-id');
        idText.textContent = node.id;
        labelsGroup.appendChild(idText);
        
        // Node name label below
        const nameText = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        nameText.setAttribute('x', node.x);
        nameText.setAttribute('y', node.y + 38);
        nameText.setAttribute('class', 'node-label');
        nameText.textContent = node.name;
        labelsGroup.appendChild(nameText);
    });
    
    // Append groups in order (edges first, then path, then nodes, then labels)
    svg.appendChild(edgesGroup);
    svg.appendChild(pathGroup);
    svg.appendChild(nodesGroup);
    svg.appendChild(labelsGroup);
}

function highlightPath(path) {
    if (!path || path.length < 2) return;
    
    currentPath = path;
    
    // Re-render graph first to clear any existing path
    renderGraph();
    
    // Get path group
    const pathGroup = document.getElementById('path');
    
    // Draw path segments
    for (let i = 0; i < path.length - 1; i++) {
        const fromId = path[i];
        const toId = path[i + 1];
        
        const fromNode = graphData.nodes.find(n => n.id === fromId);
        const toNode = graphData.nodes.find(n => n.id === toId);
        
        if (fromNode && toNode) {
            const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
            line.setAttribute('x1', fromNode.x);
            line.setAttribute('y1', fromNode.y);
            line.setAttribute('x2', toNode.x);
            line.setAttribute('y2', toNode.y);
            line.setAttribute('class', 'path-line');
            pathGroup.appendChild(line);
        }
    }
    
    // Highlight path nodes
    path.forEach(nodeId => {
        const nodeCircle = document.querySelector(`.node-circle[data-id="${nodeId}"]`);
        if (nodeCircle) {
            nodeCircle.style.stroke = 'var(--path-color)';
            nodeCircle.style.strokeWidth = '4';
        }
    });
}

function selectNode(nodeId) {
    // If start is not selected, set as start
    if (!elements.startNode.value) {
        elements.startNode.value = nodeId;
    } else if (!elements.endNode.value) {
        elements.endNode.value = nodeId;
    } else {
        // Both are selected, replace end
        elements.endNode.value = nodeId;
    }
}

// ===== Toast Notifications =====
function showError(message) {
    elements.errorMessage.textContent = message;
    elements.errorToast.style.display = 'flex';
    
    setTimeout(() => {
        hideToast();
    }, 5000);
}

function hideToast() {
    elements.errorToast.style.display = 'none';
}

function showSuccess(message) {
    elements.successMessage.textContent = message;
    elements.successToast.style.display = 'flex';
    
    setTimeout(() => {
        hideSuccessToast();
    }, 3000);
}

function hideSuccessToast() {
    elements.successToast.style.display = 'none';
}

// Make hide functions global for onclick handlers
window.hideToast = hideToast;
window.hideSuccessToast = hideSuccessToast;
