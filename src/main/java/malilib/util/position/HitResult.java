package malilib.util.position;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;

public class HitResult
{
    public final Type type;
    @Nullable public final BlockPos blockPos;
    @Nullable public final Direction side;
    @Nullable public final Vec3d pos;
    @Nullable public final Entity entity;

    public HitResult(Type type, @Nullable BlockPos blockPos, @Nullable Direction side, @Nullable Vec3d pos, @Nullable Entity entity)
    {
        this.type = type;
        this.blockPos = blockPos;
        this.side = side;
        this.pos = pos;
        this.entity = entity;
    }

    @Nullable
    public BlockPos getBlockPos()
    {
        return this.blockPos;
    }

    @Override
    public String toString()
    {
        return "HitResult{type=" + this.type + ", blockPos=" + this.blockPos + ", side=" + this.side +
               ", pos=" + this.pos + ", entity=" + this.entity + "}";
    }

    public enum Type
    {
        MISS,
        BLOCK,
        ENTITY;
    }

    public static HitResult miss()
    {
        return new HitResult(Type.MISS, null, null, null, null);
    }

    public static HitResult block(BlockPos pos, Direction side, Vec3d exactPos)
    {
        return new HitResult(Type.BLOCK, pos, side, exactPos, null);
    }

    public static HitResult entity(Entity entity, Vec3d exactPos)
    {
        return new HitResult(Type.ENTITY, null, null, exactPos, entity);
    }

    @Nullable
    public net.minecraft.world.HitResult toVanilla()
    {
        switch (this.type)
        {
            case BLOCK:     return new net.minecraft.world.HitResult(this.blockPos.x, this.blockPos.y, this.blockPos.z, this.side.getIndex(), this.pos.toVanilla());
            case ENTITY:    return new net.minecraft.world.HitResult(this.entity);
            default:        return null;
        }
    }

    public static HitResult of(@Nullable net.minecraft.world.HitResult trace)
    {
        if (trace == null)
        {
            return miss();
        }

        switch (trace.type)
        {
            case 0:    return block(new BlockPos(trace.x, trace.y, trace.z), Direction.byIndex(trace.face), Vec3d.of(trace.offset));
            case 1:    return entity(trace.entity, Vec3d.of(trace.offset));
            default:
                return miss();
        }
    }
}
