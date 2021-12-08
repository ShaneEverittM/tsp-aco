package aco.path.optimize;

import aco.Matrix;
import aco.path.RouteOptimizationStrategy;

import java.util.ArrayList;
import java.util.List;

public class TwoOptStrategy extends RouteOptimizationStrategy {

    @Override
    public OptimizedPathInfo optimize(List<Integer> path, Matrix adjMatrix) {
        List<List<Integer>> paths = convertToMultiplePaths(path);

        paths = paths.stream()
                .map(singlePath -> this.optimizePath(singlePath, adjMatrix))
                .toList();

        List<Integer> newPath = convertToSinglePath(paths);

        return new OptimizedPathInfo(newPath, getPathLength(newPath, adjMatrix));
    }

    // Referring https://en.wikipedia.org/wiki/2-opt
    private List<Integer> optimizePath(List<Integer> path, Matrix adjMatrix) {
        for (int i = 0; i < path.size() - 2; i++) {
            for (int k = i + 1; k < path.size() - 1; k++) {
                double removedEdgesCost = adjMatrix.get(path.get(i), path.get(i + 1)) +
                        adjMatrix.get(path.get(k), path.get(k + 1));
                double newEdgesCost =
                        adjMatrix.get(path.get(i), path.get(k)) +
                                adjMatrix.get(path.get(i + 1), path.get(k + 1));

                if (removedEdgesCost - newEdgesCost > 1.0) {
                    return optimizePath(swap(path, i, k), adjMatrix);
                }
            }
        }

        return path;
    }

    private List<Integer> swap(List<Integer> path, int i, int k) {
        List<Integer> swappedPath = new ArrayList<>(path.size());

        // Add first i elements
        for (int start = 0; start <= i; start++) {
            swappedPath.add(path.get(start));
        }

        // Add reversed elements from k to i + 1
        for (int rev = k; rev >= i + 1; rev--) {
            swappedPath.add(path.get(rev));
        }

        // Add remaining
        for (int rest = k + 1; rest < path.size(); rest++) {
            swappedPath.add(path.get(rest));
        }

        return swappedPath;
    }

}
