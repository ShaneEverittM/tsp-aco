package aco;

import java.util.*;
import java.util.stream.Collectors;

public class Ant {
    // Ant travel history
    private final Deque<Integer> pathTaken;
    private double pathLength;
    private final Set<Integer> visited;
    private final int totalNumNodes;

    // Ant capacity
    private final int maxCapacity;
    private int currentCapacity;

    // Ant name for logging purposes
    private final String name;

    // Random Number Generator
    private final Random rng;

    public Ant(int antNum, int totalNumNodes, int maxCapacity) {
        this.pathTaken = new ArrayDeque<>();
        this.visited = new HashSet<>();
        this.rng = new Random();
        this.maxCapacity = maxCapacity;
        this.currentCapacity = maxCapacity;
        this.totalNumNodes = totalNumNodes;

        this.name = "Ant " + antNum;
        visit(0);
    }


    /**
     * Moves this ant to another node based off distance, pheromones and capacity.
     *
     * @param adjMatrix  an adjacency matrix for the problem this ant is a part of
     * @param pheromones the pheromones on this adjacency matrix
     * @param nodes      the list of nodes of the graph for this problem
     */
    public void moveToNext(Matrix adjMatrix, Matrix pheromones, List<Node> nodes) {
        // Increment the tour length after finding the nextNode and BEFORE marking it as visited
        int curNode = getCurNode();

        if (curNode == 0) {
            // Reset capacity, we were at depot
            this.currentCapacity = this.maxCapacity;
        }

        int nextNode = findNextNode(adjMatrix, pheromones, nodes);

        pathLength += adjMatrix.get(curNode, nextNode);

        this.currentCapacity -= nodes.get(nextNode).demand();

        visit(nextNode);
    }

    /**
     * Determines the next node to move to based off distance, pheromones and capacity.
     *
     * @param adjMatrix  an adjacency matrix for the problem this ant is a part of
     * @param pheromones the pheromones on this adjacency matrix
     * @param nodes      the list of nodes of the graph for this problem
     * @return the index of the node to move to
     */
    private int findNextNode(Matrix adjMatrix, Matrix pheromones, List<Node> nodes) {
        Map<Integer, Double> distribution = new HashMap<>();
        double totalEdgeWeight = 0.0;
        for (int i = 0; i < adjMatrix.getSize(); i++) {
            // Node is NOT yet visited
            if (!visited.contains(i)) {
                double edgeWeight = calcEdgeWeight(
                        adjMatrix.get(getCurNode(), i),
                        pheromones.get(getCurNode(), i)
                );
                totalEdgeWeight += edgeWeight;
                distribution.put(i, edgeWeight);
            }
        }

        Integer nextNode = getNextNodeByProbability(distribution, totalEdgeWeight);

        // Can we even move there
        if (this.currentCapacity < nodes.get(nextNode).demand()) {
            nextNode = 0;
        }

        return nextNode;
    }

    /**
     * Determines the next node based off the weight distribution.
     *
     * @param distribution    the weight distribution
     * @param totalEdgeWeight the total edge weight
     * @return the index of the next node
     */
    private Integer getNextNodeByProbability(Map<Integer, Double> distribution, double totalEdgeWeight) {
        // Copied from https://stackoverflow.com/a/20329901/2950032
        double rand = this.rng.nextDouble();
        double ratio = 1.0f / totalEdgeWeight;
        double tempDist = 0;
        for (Integer i : distribution.keySet()) {
            tempDist += distribution.get(i);
            if (rand / ratio <= tempDist) {
                return i;
            }
        }
        throw new RuntimeException("Vertex not found for some reason");
    }

    /**
     * Given an edge cost and pheromone level, computes the weight.
     *
     * @param edgeCost  the cost from the problem
     * @param pheromone the pheromone on the edge currently
     * @return the calculated weight
     */
    private double calcEdgeWeight(double edgeCost, double pheromone) {
        double e = Math.pow(1.0f / edgeCost, Config.getEdgeWeightStrength());
        double p = Math.pow(pheromone, Config.getPheromoneStrength());
        return e * p;
    }

    /**
     * Visits the given node, updates path taken and visited for this Ant.
     *
     * @param idx the index of the node to visit
     */
    private void visit(int idx) {
        this.pathTaken.add(idx);
        this.visited.add(idx);
    }

    /**
     * Used as a stop condition for this Ant.
     *
     * @return if this ant has hit every node in the graph
     */
    public boolean hasVisitedAllNodes() {
        return this.totalNumNodes == this.visited.size();
    }

    /**
     * Computes the path length this ant has taken
     *
     * @param adjMatrix the adjacency matrix the Ant has been traversing
     * @return the total weight of the path
     */
    public double getPathLength(Matrix adjMatrix) {
        // Full tour length is single path length + cost of edge from last to first node
        return this.pathLength + adjMatrix.get(getCurNode(), this.pathTaken.getFirst());
    }

    public List<Integer> getPathTaken() {
        return this.pathTaken.stream().toList();
    }

    private int getCurNode() {
        return this.pathTaken.getLast();
    }

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return this.pathTaken
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining("->"))
                .concat("->" + this.pathTaken.getFirst());
    }

    public int getCurrentCapacity() {
        return currentCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
}
