package tgw.evolution.util.reflection;

import tgw.evolution.Evolution;

public interface IReflectionHandler {
    Object[] EMPTY_ARGS = new Object[0];
    Class<?>[] EMPTY_CLAZZ = new Class[0];

    static void throwError(Exception exception, String message) {
        Evolution.error(message + ": " + exception);
    }

    void init();
}
