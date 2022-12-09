package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Creeper;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.items.IMelee;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.HMCreeper;
import tgw.evolution.util.hitbox.hrs.HRCreeper;

public final class HitboxCreeper extends HitboxEntity<Creeper> implements HMCreeper<Creeper>, HRCreeper {

    private final Hitbox head;
    private final Hitbox legFL;
    private final Hitbox legFR;
    private final Hitbox legHL;
    private final Hitbox legHR;

    public HitboxCreeper() {
        this.head = this.addBox(HitboxType.HEAD, HitboxLib.HUMANOID_HEAD, 0, 18, 0, this);
        this.addBox(HitboxType.CHEST, box(-4, -12, -2, 8, 12, 4), 0, 18, 0, this);
        this.legFL = this.addBox(HitboxType.LEG_FRONT_LEFT, HitboxLib.CREEPER_LEG, -2, 6, -4, this);
        this.legFR = this.addBox(HitboxType.LEG_FRONT_RIGHT, HitboxLib.CREEPER_LEG, 2, 6, -4, this);
        this.legHL = this.addBox(HitboxType.LEG_HIND_LEFT, HitboxLib.CREEPER_LEG, -2, 6, 4, this);
        this.legHR = this.addBox(HitboxType.LEG_HIND_RIGHT, HitboxLib.CREEPER_LEG, 2, 6, 4, this);
        this.finish();
    }

    @Override
    protected @Nullable Hitbox childGetEquipFor(Creeper creeper, IMelee.@Nullable IAttackType type, HumanoidArm arm) {
        return null;
    }

    @Override
    public void childInit(Creeper entity, float partialTicks) {
        this.renderOrInit(entity, this, partialTicks);
    }

    @Override
    public HM head() {
        return this.head;
    }

    @Override
    protected Hitbox headOrRoot() {
        return this.head;
    }

    @Override
    public HM leftFrontLeg() {
        return this.legFL;
    }

    @Override
    public HM leftHindLeg() {
        return this.legHL;
    }

    @Override
    public HMCreeper<Creeper> model() {
        return this;
    }

    @Override
    protected double relativeHeadOrRootX() {
        return 0;
    }

    @Override
    protected double relativeHeadOrRootY() {
        return 4 / 16.0;
    }

    @Override
    protected double relativeHeadOrRootZ() {
        return -4 / 16.0;
    }

    @Override
    public HM rightFrontLeg() {
        return this.legFR;
    }

    @Override
    public HM rightHindLeg() {
        return this.legHR;
    }

    @Override
    public void setAgeInTicks(float ageInTicks) {
        this.ageInTicks = ageInTicks;
    }
}
