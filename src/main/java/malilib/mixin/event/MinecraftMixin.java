package malilib.mixin.event;

import java.awt.Canvas;
import javax.annotation.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftApplet;
import net.minecraft.client.entity.living.player.InputPlayerEntity;
import net.minecraft.world.World;

import malilib.event.dispatch.ClientWorldChangeEventDispatcherImpl;
import malilib.event.dispatch.InitializationDispatcherImpl;
import malilib.event.dispatch.TickEventDispatcherImpl;
import malilib.registry.Registry;
import malilib.util.game.wrap.GameWrap;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin
{
    @Shadow public World world;
    @Shadow public InputPlayerEntity player;

    private World malilib_worldBefore;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void malilib$onConstruct(Canvas minecraftApplet, MinecraftApplet i, int j, int bl, boolean par5, CallbackInfo ci)
    {
        GameWrap.minecraft = (Minecraft) (Object) this;
    }

    @Inject(method = "run",
            at = @At(value = "FIELD", shift = At.Shift.AFTER,
                     target = "Lnet/minecraft/client/Minecraft;gui:Lnet/minecraft/client/gui/GameGui;"))
    private void malilib$onInitComplete(CallbackInfo ci)
    {
        // Register all mod handlers
        ((InitializationDispatcherImpl) Registry.INITIALIZATION_DISPATCHER).onGameInitDone();
    }

    // TODO in-20100223 is this ok?
    @Inject(method = "tick()V", at = @At(value = "TAIL"))
    private void malilib$onTickEnd(CallbackInfo ci)
    {
        if (this.world != null && this.player != null)
        {
            ((TickEventDispatcherImpl) Registry.TICK_EVENT_DISPATCHER).onClientTick();
        }
    }

    @Inject(method = "m_8603410", at = @At("HEAD"))
    private void malilib$beforeSetWorld(@Nullable World worldIn, CallbackInfo ci)
    {
        this.malilib_worldBefore = this.world;
        ((ClientWorldChangeEventDispatcherImpl) Registry.CLIENT_WORLD_CHANGE_EVENT_DISPATCHER).onWorldLoadPre(this.world, worldIn);
    }

    @Inject(method = "m_8603410", at = @At("RETURN"))
    private void malilib$afterSetWorld(@Nullable World worldIn, CallbackInfo ci)
    {
        ((ClientWorldChangeEventDispatcherImpl) Registry.CLIENT_WORLD_CHANGE_EVENT_DISPATCHER).onWorldLoadPost(this.malilib_worldBefore, worldIn);
        this.malilib_worldBefore = null;
    }
}
