package aco;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ACORunner {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Pass a file name!");
            return;
        }
        runAlgorithm(Collections.singletonList(args[0]));
    }

    private static void runAlgorithm(List<String> filePaths) throws IOException {
        for (String filePath : filePaths) {
            Problem problem = parseInput(filePath);
            ACO aco = new ACO(problem);
            aco.run();
        }
    }

    /**
     * Parse input .vrp file.
     *
     * @param filePath path to the .vrp file
     * @return a new Matrix representing the problem
     * @throws IOException if read fails or the path doesn't exist
     */
    private static Problem parseInput(String filePath) throws IOException, VRPFormatException {
        var lines = Files.readAllLines(Paths.get(filePath));

        // Pull out the dimension
        int dimension = Integer.parseInt(lines.get(3).split(":")[1].strip());

        // Empty adjacency matrix
        var adjMatrix = new Matrix(dimension);

        // Parse coordinates
        int fromIndex = 7;
        int toIndex = 7 + dimension;
        var nodeCoordinates = lines.subList(fromIndex, toIndex).stream().map(NodeCoordinate::fromLine).toList();

        // Populate adjacency matrix with distances
        for (var pointA : nodeCoordinates) {
            for (var pointB : nodeCoordinates) {
                double distance = pointA.distanceFrom(pointB);
                adjMatrix.set(pointA.index, pointB.index, distance);
            }
        }

        // Parse demands for each node
        fromIndex = 8 + dimension;
        toIndex = 8 + dimension * 2;
        List<Demand> nodeDemands = lines.subList(fromIndex, toIndex).stream().map(Demand::fromLine).toList();

        return new Problem(adjMatrix, nodeDemands);
    }

    private static List<String> scanFolder(String folderPath) throws IOException {
        List<String> filePaths = new ArrayList<>();
        String glob = "glob:**/*.txt";
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(
                glob);

        Files.walkFileTree(Paths.get(folderPath), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(path)) {
                    filePaths.add(path.toString());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc)
                    throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return filePaths;
    }

    private static boolean isFolderInput() {
        return System.getProperty("folder", "false").equalsIgnoreCase("true");
    }

}
