package tgw.evolution.util.hitbox.hitboxes;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.npc.Villager;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.items.IMelee;
import tgw.evolution.util.hitbox.Hitbox;
import tgw.evolution.util.hitbox.HitboxGroup;
import tgw.evolution.util.hitbox.HitboxType;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.LegacyHMVillager;
import tgw.evolution.util.hitbox.hrs.LegacyHRVillager;

public class LegacyHitboxVillager extends HitboxEntity<Villager> implements LegacyHMVillager<Villager>, LegacyHRVillager {
    protected final HitboxGroup head;
    protected final Hitbox legL;
    protected final Hitbox legR;
    protected final Hitbox root;

    public LegacyHitboxVillager() {
        //Head
        this.head = new HitboxGroup(this, 0, 24, 0);
        this.root = this.addBox(HitboxType.HEAD, box(-4, 0, -4, 8, 10, 8), this.head);
        this.addBox(HitboxType.NOSE, box(-1, -1, -6, 2, 4, 2), this.head);
        //Body
        this.addBox(HitboxType.CHEST, box(-4, -12, -3, 8, 12, 6), 0, 24, 0, this);
        //Arms
        HitboxGroup arms = new HitboxGroup(this, 0, 15.2f, -6.4f);
        this.addBox(HitboxType.ARM_LEFT, box(-8, 2, -2, 4, 8, 4), arms);
        this.addBox(HitboxType.ARM_RIGHT, box(4, 2, -2, 4, 8, 4), arms);
        this.addBox(HitboxType.HAND_LEFT, box(-4, 2, -2, 4, 4, 4), arms);
        this.addBox(HitboxType.HAND_RIGHT, box(0, 2, -2, 4, 4, 4), arms);
        arms.setRotationX(0.75f);
        //Legs
        this.legR = this.addBox(HitboxType.LEG_RIGHT, box(-2, -12, -2, 4, 12, 4), -2, 12, 0, this);
        this.legL = this.addBox(HitboxType.LEG_LEFT, box(-2, -12, -2, 4, 12, 4), 2, 12, 0, this);
    }

    @Override
    protected @Nullable Hitbox childGetEquipFor(Villager entity, IMelee.IAttackType type, HumanoidArm arm) {
        return null;
    }

    @Override
    protected void childInit(Villager entity, float partialTicks) {
        this.renderOrInit(entity, this, partialTicks);
    }

    @Override
    public HM head() {
        return this.head;
    }

    @Override
    protected Hitbox headOrRoot() {
        return this.root;
    }

    @Override
    public HM legL() {
        return this.legL;
    }

    @Override
    public HM legR() {
        return this.legR;
    }

    @Override
    public LegacyHMVillager<Villager> model() {
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
    public void setAgeInTicks(float ageInTicks) {
        this.ageInTicks = ageInTicks;
    }
}
