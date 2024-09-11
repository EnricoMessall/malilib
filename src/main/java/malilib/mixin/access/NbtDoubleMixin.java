package malilib.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.nbt.NbtDouble;

@Mixin(NbtDouble.class)
public interface NbtDoubleMixin
{
    @Accessor("value")
    double getValue();

    @Accessor("value")
    void setValue(double value);
}
