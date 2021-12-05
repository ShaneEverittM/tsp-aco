package aco;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ACO {
    // Problem description
    private final Matrix adjMatrix;
    private final List<Node> nodes;
    private final int capacity;
    private final int numNodes;

    // Ant tracking
    private Ant[] ants;
    private static final int numAnts = 2;
    private Matrix pheromones;

    // Time out after some number of cycles
    private static final int maxCycles = 2000;
    private int curCycle = 0;

    // Best Hamiltonian cycles found by any ant
    private double bestTourLength = Double.MAX_VALUE;
    private List<Integer> bestTourPath;

    public ACO(Problem problem) {
        adjMatrix = problem.adjacencyMatrix();
        nodes = problem.nodes();
        capacity = problem.capacity();
        numNodes = adjMatrix.getSize();
    }

    /**
     * Runs the simulation for numCycles iterations.
     */
    public void run() {
        initPheromones();
        while (shouldContinue()) {
            initAnts();
            updateAnts();
            findBestTour();
            evaporate();
            updatePheromones();
        }
        System.out.printf("Best found VRP solution of cost %f visiting %s%n", bestTourLength, bestTourPath);
    }

    /**
     * For each ant being simulated, if it has not been everywhere, compute its next hop.
     */
    private void updateAnts() {
        for (Ant ant : ants) {
            while (!ant.hasVisitedAllNodes()) {
                ant.moveToNext(adjMatrix, pheromones, nodes);
            }
        }
    }

    /**
     * Given a path, formats it for visualization.
     *
     * @param path a list of ints representing the path taken
     * @return a string formatted as distinct routes
     */
    private String formatPath(List<Integer> path) {
        int curPathIdx = -1;
        List<List<Integer>> paths = new ArrayList<>();

        for (int i : path) {
            if (i == 0) {
                paths.add(new ArrayList<>());
                ++curPathIdx;
            } else {
                paths.get(curPathIdx).add(i);
            }
        }

        List<String> lines = new ArrayList<>();

        for (int i = 0; i < paths.size(); i++) {
            List<Integer> curPath = paths.get(i);
            String line = curPath.stream().map(String::valueOf).collect(Collectors.joining(" "));
            lines.add("Route #" + (i + 1) + ": " + line);
        }

        return String.join("\n", lines);
    }


    /**
     * Checks each ant to see if it has a new best tour length, caches the length and path.
     */
    private void findBestTour() {
        boolean isBetterFound = false;
        for (Ant ant : ants) {
            double antTourLength = ant.getPathLength(adjMatrix);
            if (bestTourLength > antTourLength) {
                isBetterFound = true;
                bestTourLength = antTourLength;
                bestTourPath = ant.getPathTaken();
            }
        }

        if (isBetterFound) {
            System.out.printf("New best found VRP solution of cost %f visiting%n", bestTourLength);
            System.out.println("Current Paths: \n" + formatPath(bestTourPath));
        }
    }

    /**
     * Evaporates the pheromones along all edges.
     */
    private void evaporate() {
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                pheromones.set(i, j, pheromones.get(i, j) * Config.getRateOfEvaporation());
                pheromones.set(j, i, pheromones.get(j, i) * Config.getRateOfEvaporation());
            }
        }
    }

    /**
     * Updates the pheromones according to the path ants have taken.
     */
    private void updatePheromones() {
        for (Ant ant : ants) {
            double pheromone = Config.getQ3() / ant.getPathLength(adjMatrix);
            List<Integer> tabu = ant.getPathTaken();

            // tabu.length - 1 is important because we don't want to go till the last node
            // and then overflow the i + 1 value
            for (int i = 0; i < tabu.size() - 1; i++) {
                int u = tabu.get(i), v = tabu.get(i + 1);
                // Add pheromone to the edges
                pheromones.set(u, v, pheromones.get(u, v) + pheromone);
                pheromones.set(v, u, pheromones.get(v, u) + pheromone);
            }
            // Add pheromone to the last edge
            int first = tabu.get(0);
            int last = tabu.get(tabu.size() - 1);
            pheromones.set(first, last, pheromones.get(first, last) + pheromone);
            pheromones.set(last, first, pheromones.get(last, first) + pheromone);
        }
    }

    /**
     * Populates the edges with some initial pheromone value.
     */
    private void initPheromones() {
        pheromones = new Matrix(numNodes);
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                pheromones.set(i, j, 1);
                pheromones.set(j, i, 1);
            }
        }
    }

    /**
     * Spawns numAnts at the origin.
     */
    private void initAnts() {
        ants = new Ant[numAnts];
        for (int i = 0; i < numAnts; i++) {
            ants[i] = new Ant(i, numNodes, capacity);
        }
    }

    /**
     * Checks whether the algorithm has exceeded maximum number of cycles.
     */
    private boolean shouldContinue() {
        return curCycle++ < maxCycles;
    }
}

