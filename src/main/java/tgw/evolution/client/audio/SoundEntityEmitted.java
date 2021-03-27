package tgw.evolution.client.audio;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class SoundEntityEmitted extends TickableSound {

    private final Entity entity;

    public SoundEntityEmitted(Entity entity, SoundEvent soundEvent, SoundCategory category, float volume, float pitch) {
        super(soundEvent, category);
        this.entity = entity;
        this.volume = volume;
        this.pitch = pitch;
        this.x = (float) entity.posX;
        this.y = (float) entity.posY;
        this.z = (float) entity.posZ;
    }

    @Override
    public void tick() {
        if (this.entity.removed) {
            this.donePlaying = true;
        }
        else {
            this.x = (float) this.entity.posX;
            this.y = (float) this.entity.posY;
            this.z = (float) this.entity.posZ;
        }
    }
}
