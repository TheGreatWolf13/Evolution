package tgw.evolution.util.hitbox.hitboxes;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.items.IMelee;
import tgw.evolution.util.hitbox.Hitbox;
import tgw.evolution.util.hitbox.HitboxType;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.LegacyHMSpider;
import tgw.evolution.util.hitbox.hrs.LegacyHRSpider;

public class LegacyHitboxSpider<T extends Spider> extends HitboxEntity<T> implements LegacyHMSpider<T>, LegacyHRSpider<T> {

    protected final Hitbox head;
    protected final Hitbox legFL;
    protected final Hitbox legFML;
    protected final Hitbox legFMR;
    protected final Hitbox legFR;
    protected final Hitbox legHL;
    protected final Hitbox legHML;
    protected final Hitbox legHMR;
    protected final Hitbox legHR;

    public LegacyHitboxSpider() {
        this.head = this.addBox(HitboxType.HEAD, box(-4, -4, -8, 8, 8, 8), 0, 9, -3, this);
        this.addBox(HitboxType.CHEST, box(-3, -3, -3, 6, 6, 6), 0, 9, 0, this);
        this.addBox(HitboxType.CHEST, box(-5, -4, -6, 10, 8, 12), 0, 9, 9, this);
        AABB legR = box(-1, -1, -1, 16, 2, 2);
        AABB legL = box(-15, -1, -1, 16, 2, 2);
        this.legHR = this.addBox(HitboxType.LEG_HIND_RIGHT, legR, 4, 9, 1, this);
        this.legHL = this.addBox(HitboxType.LEG_HIND_LEFT, legL, -4, 9, 1, this);
        this.legHMR = this.addBox(HitboxType.LEG_HIND_MIDDLE_RIGHT, legR, 4, 9, 0, this);
        this.legHML = this.addBox(HitboxType.LEG_HIND_MIDDLE_LEFT, legL, -4, 9, 0, this);
        this.legFMR = this.addBox(HitboxType.LEG_FRONT_MIDDLE_RIGHT, legR, 4, 9, -1, this);
        this.legFML = this.addBox(HitboxType.LEG_FRONT_MIDDLE_LEFT, legL, -4, 9, -1, this);
        this.legFR = this.addBox(HitboxType.LEG_FRONT_RIGHT, legR, 4, 9, -2, this);
        this.legFL = this.addBox(HitboxType.LEG_FRONT_LEFT, legL, -4, 9, -2, this);
    }

    @Override
    protected @Nullable Hitbox childGetEquipFor(T entity, IMelee.IAttackType type, HumanoidArm arm) {
        return null;
    }

    @Override
    protected void childInit(T entity, float partialTicks) {
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
    public HM legFL() {
        return this.legFL;
    }

    @Override
    public HM legFML() {
        return this.legFML;
    }

    @Override
    public HM legFMR() {
        return this.legFMR;
    }

    @Override
    public HM legFR() {
        return this.legFR;
    }

    @Override
    public HM legHL() {
        return this.legHL;
    }

    @Override
    public HM legHML() {
        return this.legHML;
    }

    @Override
    public HM legHMR() {
        return this.legHMR;
    }

    @Override
    public HM legHR() {
        return this.legHR;
    }

    @Override
    public LegacyHMSpider<T> model() {
        return this;
    }

    @Override
    protected double relativeHeadOrRootX() {
        return 0;
    }

    @Override
    protected double relativeHeadOrRootY() {
        return 0;
    }

    @Override
    protected double relativeHeadOrRootZ() {
        return -8 / 16.0;
    }

    @Override
    public void setAgeInTicks(float ageInTicks) {
        this.ageInTicks = ageInTicks;
    }
}
