package aco.path.optimize;

import aco.Matrix;
import aco.path.RouteOptimizationStrategy;

import java.util.List;

public class TwoOptStrategy extends RouteOptimizationStrategy {

    @Override
    public OptimizedPathInfo optimize(List<Integer> path, Matrix adjMatrix) {
        List<List<Integer>> paths = convertToMultiplePaths(path);
        List<Integer> newPath = convertToSinglePath(paths);

        double newPathLength = 0.0;
        return new OptimizedPathInfo(newPath, newPathLength);
    }

}
