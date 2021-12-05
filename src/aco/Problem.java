package aco;


import java.util.List;
import java.util.regex.Pattern;

class VRPFormatException extends RuntimeException {
    public VRPFormatException(String message) {
        super(message);
    }
}

/**
 * Represents the position of node at "index".
 */
record NodeCoordinate(int index, int x, int y) {
    static Pattern pattern = Pattern.compile("\\s*(?<index>\\w+)\\s*(?<x>\\w+)\\s*(?<y>\\w+)\\s*");

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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!NodeCoordinate.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        NodeCoordinate other = (NodeCoordinate) obj;
        return this.x == other.x && this.y == other.y;
    }
}

/**
 * Represents the demand of node at "index".
 */
record Demand(int index, int demand) {
    static Pattern pattern = Pattern.compile("\\s*(?<index>\\w+)\\s*(?<demand>\\w+)\\s*");

    public static Demand fromLine(String line) {
        var matcher = pattern.matcher(line);
        if (matcher.matches()) {
            var index = Integer.parseInt(matcher.group("index")) - 1;
            var demand = Integer.parseInt(matcher.group("demand"));

            return new Demand(index, demand);
        }
        throw new VRPFormatException("Failed to parse Demand from line: \"" + line + "\"");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!Demand.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        Demand other = (Demand) obj;
        return this.demand == other.demand;
    }
}

record Node(int x, int y, int demand) {
}

record Problem(Matrix adjacencyMatrix, List<Node> nodes, int capacity) {
}
