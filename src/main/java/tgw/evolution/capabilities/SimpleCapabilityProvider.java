package tgw.evolution.capabilities;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class SimpleCapabilityProvider<C> implements ICapabilityProvider {

    protected final Capability<C> capability;

    protected final Direction direction;

    protected final C handler;

    protected final LazyOptional<C> lazyOptional;

    public SimpleCapabilityProvider(Capability<C> capability, @Nullable Direction direction, @Nullable C handler) {
        this.capability = capability;
        this.direction = direction;
        this.handler = handler;
        if (this.handler != null) {
            this.lazyOptional = LazyOptional.of(() -> this.handler);
        }
        else {
            this.lazyOptional = LazyOptional.empty();
        }
    }

    /**
     * Retrieves the handler for the capability requested on the specific side.
     * The return value CAN be null if the object does not support the capability.
     * The return value CAN be the same for multiple faces.
     *
     * @param capability The capability to check
     * @param direction  The Side to check from:
     *                   CAN BE NULL. Null is defined to represent 'internal' or 'self'
     * @return A lazy optional containing the handler, if this object supports the capability.
     */
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
        return this.capability.orEmpty(capability, this.lazyOptional);
    }

    /**
     * Get the {@link Capability} instance to provide the handler for.
     *
     * @return The Capability instance
     */
    public final Capability<C> getCapability() {
        return this.capability;
    }

    /**
     * Get the {@link Direction} to provide the handler for.
     *
     * @return The Direction to provide the handler for
     */
    @Nullable
    public Direction getDirection() {
        return this.direction;
    }

    /**
     * Get the handler instance.
     *
     * @return A lazy optional containing the handler instance
     */
    @Nullable
    public final C getHandler() {
        return this.handler;
    }
}
