package tgw.evolution.entities.event;

import net.minecraft.world.entity.Mob;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.entities.EntityGenericAgeable;

@Cancelable
public class EventSpawnBabyEntity extends Event {
    private final Mob father;
    private final Mob mother;
    private EntityGenericAgeable child;

    public EventSpawnBabyEntity(Mob father, Mob mother, @Nullable EntityGenericAgeable proposedChild) {
        this.father = father;
        this.mother = mother;
        this.child = proposedChild;
    }

    @Nullable
    public EntityGenericAgeable getChild() {
        return this.child;
    }

    public Mob getFather() {
        return this.father;
    }

    public Mob getMother() {
        return this.mother;
    }

    public void setChild(EntityGenericAgeable proposedChild) {
        this.child = proposedChild;
    }
}