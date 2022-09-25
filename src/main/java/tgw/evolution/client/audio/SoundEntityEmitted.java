package tgw.evolution.client.audio;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class SoundEntityEmitted extends AbstractTickableSoundInstance {

    private final Entity entity;

    public SoundEntityEmitted(Entity entity, SoundEvent soundEvent, SoundSource category, float volume, float pitch) {
        super(soundEvent, category);
        this.entity = entity;
        this.volume = volume;
        this.pitch = pitch;
        this.x = (float) entity.getX();
        this.y = (float) entity.getY();
        this.z = (float) entity.getZ();
    }

    @Override
    public void tick() {
        if (this.entity.isRemoved()) {
            this.stop();
        }
        else {
            this.x = (float) this.entity.getX();
            this.y = (float) this.entity.getY();
            this.z = (float) this.entity.getZ();
        }
    }
}
