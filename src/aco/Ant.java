package aco;

import java.util.*;
import java.util.stream.Collectors;

public class Ant {
    private final List<Integer> pathTaken;
    private int curIdxInPath = 0;
    private final Set<Integer> visited;
    private double tourLength;
    private final int maxCapacity;
    private int currentCapacity;
    private final int numNodes;

    private final String name;
    private final Random rand;

    public Ant(int antNum, int numNodes, int maxCapacity) {
        this.pathTaken = new ArrayList<>();
        this.visited = new HashSet<>();
        this.rand = new Random();
        this.maxCapacity = maxCapacity;
        this.currentCapacity = maxCapacity;
        this.numNodes = numNodes;

        this.name = "Ant " + antNum;
        visit(0);
    }

    public void moveToNext(Matrix adjMatrix, Matrix pheromones, List<Demand> demands) {
        // Increment the tour length after finding the nextNode and BEFORE marking it as visited
        int curNode = getCurNode();

        if (curNode == 0) {
            // Reset capacity, we were at depot
            this.currentCapacity = this.maxCapacity;
        }

        int nextNode = findNextNode(adjMatrix, pheromones, demands);

        tourLength += adjMatrix.get(curNode, nextNode);

        this.currentCapacity -= demands.get(nextNode).demand;

        visit(nextNode);
    }

    public double getTourLength(Matrix adjMatrix) {
        // Full tour length is single path length + cost of edge from last to first node
        return this.tourLength + adjMatrix.get(getCurNode(), this.pathTaken.get(0));
    }

    public String getName() {
        return this.name;
    }

    public List<Integer> getPathTaken() {
        return this.pathTaken;
    }

    public String getPath() {
        return this.pathTaken
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining("->"))
                .concat("->" + this.pathTaken.get(0));
    }

    private int findNextNode(Matrix adjMatrix, Matrix pheromones, List<Demand> demands) {
        Map<Integer, Double> distribution = new HashMap<>();
        double totalEdgeWeightage = 0.0;
        for (int i = 0; i < adjMatrix.getSize(); i++) {
            // Node is NOT yet visited
            if (!visited.contains(i)) {
                double edgeWeightage = calcEdgeWeightage(
                        adjMatrix.get(getCurNode(), i),
                        pheromones.get(getCurNode(), i)
                );
                totalEdgeWeightage += edgeWeightage;
                distribution.put(i, edgeWeightage);
            }
        }

        Integer nextNode = getNextNodeByProbability(distribution, totalEdgeWeightage);

        // Can we even move there
        if (this.currentCapacity < demands.get(nextNode).demand) {
            nextNode = 0;
        }

        return nextNode;
    }

    private Integer getNextNodeByProbability(Map<Integer, Double> distribution, double totalEdgeWeightage) {
        // Copied from https://stackoverflow.com/a/20329901/2950032
        double rand = this.rand.nextDouble();
        double ratio = 1.0f / totalEdgeWeightage;
        double tempDist = 0;
        for (Integer i : distribution.keySet()) {
            tempDist += distribution.get(i);
            if (rand / ratio <= tempDist) {
                return i;
            }
        }
        throw new RuntimeException("Vertex not found for some reason");
    }

    private int getCurNode() {
        return this.pathTaken.get(curIdxInPath -1);
    }

    private void visit(int idx) {
        this.pathTaken.add(idx);
        curIdxInPath++;
        this.visited.add(idx);
    }

    private double calcEdgeWeightage(double edgeCost, double pheromone) {
        double e = Math.pow(1.0f / edgeCost, Config.getEdgeWeightStrength());
        double p = Math.pow(pheromone, Config.getPheromoneStrength());
        return e * p;
    }

    public boolean hasVisitedAllNodes() {
        return this.numNodes == this.visited.size();
    }

    public int getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(int currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
}
