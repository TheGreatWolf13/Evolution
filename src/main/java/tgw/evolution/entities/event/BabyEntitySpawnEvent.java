package tgw.evolution.entities.event;

import net.minecraft.entity.MobEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import tgw.evolution.entities.AgeableEntity;
import javax.annotation.Nullable;

@Cancelable
public class BabyEntitySpawnEvent extends Event {
    private final MobEntity father;
    private final MobEntity mother;
    private AgeableEntity child;

    public BabyEntitySpawnEvent(MobEntity father, MobEntity mother, @Nullable AgeableEntity proposedChild) {
    	this.father = father;
        this.mother = mother;
        this.child = proposedChild;
    }

    public MobEntity getFather() {
        return this.father;
    }

    public MobEntity getMother() {
        return this.mother;
    }

    @Nullable
    public AgeableEntity getChild() {
        return this.child;
    }

    public void setChild(AgeableEntity proposedChild) {
        this.child = proposedChild;
    }
}