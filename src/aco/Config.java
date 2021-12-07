package aco;

public class Config {
    // Power of the pheromone, helps decide how much emphasis on existing trails.
    // Typically, lesser than beta to protect against not discovering more paths.
    private static final double ALPHA = 2.0;

    // Power of the attractiveness
    private static final double BETA = 5.0;
    // Power of savings, helps decide how much emphasis on going to a
    // node vs going back to depot and then the node
    private static final double GAMMA = 9.0;

    // Rate of evaporation, to help reduce emphasis on existing paths
    private static final double RHO = 0.5;

    // Constant that influences evaporation rate
    private static final double THETA = 80;

    // Just a constant
    private static final double Q3 = 100;

    public static double getPheromoneStrength() {
        return ALPHA;
    }

    public static double getSavingsStrength() {
        return GAMMA;
    }

    public static double getRateOfEvaporation() {
        return RHO;
    }

    public static double getTHETA() {return THETA;}

    public static double getQ3() {
        return Q3;
    }

    public static double getAttractivenessStrength() {
         return BETA;
    }
}
