package aco.path;

import aco.Matrix;
import aco.path.optimize.OptimizedPathInfo;

import java.util.ArrayList;
import java.util.List;

public abstract class RouteOptimizationStrategy {

    protected List<List<Integer>> convertToMultiplePaths(List<Integer> path) {
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

        // Add 0 before and after to get the complete paths for optimizations
        for (List<Integer> p : paths) {
            p.add(0);
            p.add(0, 0);
        }

        paths.remove(paths.size() - 1);

        return paths;
    }

    protected List<Integer> convertToSinglePath(List<List<Integer>> paths) {
        var path = paths.stream().reduce(new ArrayList<>(paths.size()), (subArray, array) -> {
            // Remove the last zero
            for (int i = 0; i < array.size() - 1; i++) {
                subArray.add(array.get(i));
            }
            return subArray;
        });
        // Add 0 to the end again
        path.add(0);

        return path;
    }

    protected double getPathLength(List<Integer> path, Matrix adjMatrix) {
        double length = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            length += adjMatrix.get(path.get(i), path.get(i + 1));
        }

        return length;
    }

    public abstract OptimizedPathInfo optimize(List<Integer> path, Matrix adjMatrix);
}
