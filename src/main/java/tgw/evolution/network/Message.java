package tgw.evolution.network;

public final class Message {

    private Message() {
    }

    public enum C2S {
        OPEN_INVENTORY,
        STOP_USING_ITEM
    }

    public enum S2C {
        GC,
        HITMARKER_KILL,
        HITMARKER_NORMAL,
        MULTIPLAYER_PAUSE,
        MULTIPLAYER_RESUME
    }
}
