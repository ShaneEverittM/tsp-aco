package aco;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ACO {
    private final Matrix adjMatrix;
    private final List<Demand> demands;
    private final int capacity;
    private final int numOfCities;
    private final int numAnts;
    private final int numOfCycles;
    private int curCycle = 0;
    private Matrix pheromones;
    private Ant[] ants;
    private double bestTourLength = Double.MAX_VALUE;
    private List<Integer> bestTourPath;

    public ACO(Problem problem) {
        adjMatrix = problem.adjacencyMatrix;
        demands = problem.demands;
        capacity = problem.capacity;
        numOfCities = adjMatrix.getSize();
        numOfCycles = 2000;
        numAnts = 2;
    }

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

    private void updateAnts() {
        for (Ant ant : ants) {
            while (!ant.hasVisitedAllNodes()) {
                ant.moveToNext(adjMatrix, pheromones, demands);
            }
        }
    }

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
            lines.add("Route #" + (i + 1) + ": " +  line);
        }

        return String.join("\n", lines);
    }

    private void findBestTour() {
        boolean isBetterFound = false;
        for (Ant ant : ants) {
            double antTourLength = ant.getTourLength(adjMatrix);
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

    private void evaporate() {
        for (int i = 0; i < numOfCities; i++) {
            for (int j = i + 1; j < numOfCities; j++) {
                pheromones.set(i, j, pheromones.get(i, j) * Config.getRateOfEvaporation());
                pheromones.set(j, i, pheromones.get(j, i) * Config.getRateOfEvaporation());
            }
        }
    }

    private void updatePheromones() {
        for (Ant ant : ants) {
            double pheromone = Config.getQ3() / ant.getTourLength(adjMatrix);
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

    private void initPheromones() {
        pheromones = new Matrix(numOfCities);
        for (int i = 0; i < numOfCities; i++) {
            for (int j = i + 1; j < numOfCities; j++) {
                pheromones.set(i, j, 1);
                pheromones.set(j, i, 1);
            }
        }
    }

    private void initAnts() {
        ants = new Ant[numAnts];
        for (int i = 0; i < numAnts; i++) {
            ants[i] = new Ant(i, numOfCities, capacity);
        }
    }

    private boolean shouldContinue() {
        return curCycle++ < numOfCycles;
    }
}

