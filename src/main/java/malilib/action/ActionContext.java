package malilib.action;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.World;

import malilib.overlay.message.MessageOutput;
import malilib.util.game.wrap.GameWrap;

public class ActionContext
{
    public static final ActionContext COMMON = new ActionContext();

    @Nullable public final MessageOutput messageOutput;

    public ActionContext()
    {
        this(null);
    }

    public ActionContext(@Nullable MessageOutput messageOutput)
    {
        this.messageOutput = messageOutput;
    }

    public MessageOutput getMessageOutputOrDefault(MessageOutput defaultOutput)
    {
        return this.messageOutput != null ? this.messageOutput : defaultOutput;
    }

    public Minecraft getClient()
    {
        return GameWrap.getClient();
    }

    @Nullable
    public World getWorld()
    {
        return GameWrap.getClientWorld();
    }

    @Nullable
    public PlayerEntity getPlayer()
    {
        return GameWrap.getClientPlayer();
    }

    @Nullable
    public Entity getCameraEntity()
    {
        return GameWrap.getCameraEntity();
    }
}
