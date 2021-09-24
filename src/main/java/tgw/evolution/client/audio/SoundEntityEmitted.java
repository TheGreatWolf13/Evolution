package tgw.evolution.client.audio;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundEntityEmitted extends TickableSound {

    private final Entity entity;

    public SoundEntityEmitted(Entity entity, SoundEvent soundEvent, SoundCategory category, float volume, float pitch) {
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
        if (this.entity.removed) {
            this.stop();
        }
        else {
            this.x = (float) this.entity.getX();
            this.y = (float) this.entity.getY();
            this.z = (float) this.entity.getZ();
        }
    }
}
