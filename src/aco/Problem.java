package aco;


import java.util.List;
import java.util.regex.Pattern;

class VRPFormatException extends RuntimeException {
    public VRPFormatException(String message) {
        super(message);
    }

    public VRPFormatException(Throwable cause) {
        super(cause);
    }

    public VRPFormatException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

class NodeCoordinate {
    static Pattern pattern = Pattern.compile("\\s*(?<index>\\w+)\\s*(?<x>\\w+)\\s*(?<y>\\w+)\\s*");
    int index;
    int x;
    int y;

    public NodeCoordinate(int index, int x, int y) {
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public double distanceFrom(NodeCoordinate other) {
        return Math.sqrt((Math.pow(other.y - this.y, 2) + Math.pow(other.x - this.x, 2)));
    }

    public static NodeCoordinate fromLine(String line) {
        var matcher = pattern.matcher(line);
        if (matcher.find()) {
            var index = Integer.parseInt(matcher.group("index")) - 1;
            var x = Integer.parseInt(matcher.group("x"));
            var y = Integer.parseInt(matcher.group("y"));

            return new NodeCoordinate(index, x, y);
        }
        throw new VRPFormatException("Failed to parse NodeCoordinate from line: \"" + line + "\"");
    }
}


class Demand {
    static Pattern pattern = Pattern.compile("\\s*(?<index>\\w+)\\s*(?<demand>\\w+)\\s*");
    int index;
    int demand;

    public Demand(int index, int demand) {
        this.index = index;
        this.demand = demand;
    }

    public static Demand fromLine(String line) {
        var matcher = pattern.matcher(line);
        if (matcher.matches()) {
            var index = Integer.parseInt(matcher.group("index")) - 1;
            var demand = Integer.parseInt(matcher.group("demand"));

            return new Demand(index, demand);
        }
        throw new VRPFormatException("Failed to parse Demand from line: \"" + line + "\"");
    }
}

class Problem {
    Matrix adjacencyMatrix;
    List<Demand> demands;
    int capacity;


    public Problem(Matrix adjacencyMatrix, List<Demand> demands, int capacity) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.demands = demands;
        this.capacity = capacity;
    }
}
