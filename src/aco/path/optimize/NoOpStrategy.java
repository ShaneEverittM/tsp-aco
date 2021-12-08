package aco.path.optimize;

import aco.Matrix;
import aco.path.RouteOptimizationStrategy;

import java.util.List;

public class NoOpStrategy extends RouteOptimizationStrategy {
    @Override
    public OptimizedPathInfo optimize(List<Integer> path, Matrix adjMatrix) {
        return new OptimizedPathInfo(path, getPathLength(path, adjMatrix));
    }
}
