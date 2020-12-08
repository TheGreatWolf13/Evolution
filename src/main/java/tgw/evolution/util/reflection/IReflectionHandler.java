package tgw.evolution.util.reflection;

import tgw.evolution.Evolution;

public interface IReflectionHandler {

    static void throwError(Exception exception, String message) {
        Evolution.LOGGER.error(message + ": " + exception);
    }

    void init();
}
