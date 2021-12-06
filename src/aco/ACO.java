package aco;

import java.util.*;
import java.util.stream.Collectors;

public class ACO {
    // Problem description
    private final Matrix adjMatrix;
    private final List<Node> nodes;
    private final int capacity;
    private final int numNodes;

    // Ant tracking
    private Ant[] ants;
    private final int numAnts;
    private Matrix pheromones;
    private static final int numElites = 3;

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
        numAnts = numNodes;
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
        OptionalDouble maybeAverage = Arrays.stream(ants)
                .mapToDouble(ant -> ant.getPathLength(adjMatrix))
                .average();

        if (maybeAverage.isEmpty())
            throw new RuntimeException("Should never have no ants, yet here we are");

        double evaporationFactor = Config.getRateOfEvaporation() + (Config.getTHETA() / maybeAverage.getAsDouble());

        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                pheromones.set(i, j, pheromones.get(i, j) * evaporationFactor);
                pheromones.set(j, i, pheromones.get(j, i) * evaporationFactor);
            }
        }
    }

    /**
     * Updates the pheromones according the "best of the best" Ants.
     */
    private void updatePheromones() {
        Arrays.sort(ants, Comparator.comparingDouble(ant -> ant.getPathLength(adjMatrix)));

        // Update path taken by * Ant with the most pheromones
        Ant starAnt = ants[0];
        var starPath = starAnt.getPathTaken();
        for (int i = 0; i < starPath.size() - 1; i++) {
            double pheromone = numElites / starAnt.getPathLength(adjMatrix);
            int u = starPath.get(i);
            int v = starPath.get(i + 1);
            pheromones.update(u, v, val -> val + pheromone);
        }

        // Decreasingly update next best lambda ants
        for (int lambda = 1; lambda < numElites; lambda++) {
            Ant curAnt = ants[lambda];
            double pheromone = (numElites - lambda) / curAnt.getPathLength(adjMatrix);

            var pathTaken = curAnt.getPathTaken();

            for (int i = 0; i < pathTaken.size() - 1; i++) {
                int u = pathTaken.get(i), v = pathTaken.get(i + 1);

                // Add pheromone to the edges
                pheromones.update(u, v, val -> val + pheromone);
            }
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

