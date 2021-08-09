package tgw.evolution.util;

import com.google.gson.JsonObject;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.server.command.EnumArgument;
import tgw.evolution.util.reflection.FieldHandler;

public class EnumArgumentSerializer<T extends Enum<T>> implements IArgumentSerializer<EnumArgument<T>> {

    private static final FieldHandler<EnumArgument, Class> ENUM_CLASS = new FieldHandler<>(EnumArgument.class, "enumClass");

    @Override
    public void func_197072_a(EnumArgument<T> argument, PacketBuffer buffer) {
        buffer.writeString(ENUM_CLASS.get(argument).getName());
    }

    @Override
    public EnumArgument<T> read(PacketBuffer buffer) {
        try {
            String name = buffer.readString();
            return EnumArgument.enumArgument((Class<T>) Class.forName(name));
        }
        catch (Throwable t) {
            //noinspection ReturnOfNull
            return null;
        }
    }

    @Override
    public void write(EnumArgument<T> argument, JsonObject json) {
        json.addProperty("enum", ENUM_CLASS.get(argument).getName());
    }
}
