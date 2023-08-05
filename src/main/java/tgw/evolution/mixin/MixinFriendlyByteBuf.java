package tgw.evolution.mixin;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchBlockHitResult;

@Mixin(FriendlyByteBuf.class)
public abstract class MixinFriendlyByteBuf extends ByteBuf {

    @Overwrite
    public BlockHitResult readBlockHitResult() {
        long pos = this.readLong();
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        Direction direction = this.readEnum(Direction.class);
        float hitX = this.readFloat();
        float hitY = this.readFloat();
        float hitZ = this.readFloat();
        boolean inside = this.readBoolean();
        return PatchBlockHitResult.create(x + hitX, y + hitY, z + hitZ, direction, x, y, z, inside);
    }

    @Shadow
    public abstract <T extends Enum<T>> T readEnum(Class<T> class_);

    @Override
    @Shadow
    public abstract long readLong();

    @Overwrite
    public void writeBlockHitResult(BlockHitResult hitResult) {
        int posX = hitResult.posX();
        int posY = hitResult.posY();
        int posZ = hitResult.posZ();
        this.writeLong(BlockPos.asLong(posX, posY, posZ));
        this.writeEnum(hitResult.getDirection());
        this.writeFloat((float) (hitResult.x() - posX));
        this.writeFloat((float) (hitResult.y() - posY));
        this.writeFloat((float) (hitResult.z() - posZ));
        this.writeBoolean(hitResult.isInside());
    }

    @Shadow
    public abstract FriendlyByteBuf writeEnum(Enum<?> enum_);

    @Override
    @Shadow
    public abstract ByteBuf writeLong(long l);
}
