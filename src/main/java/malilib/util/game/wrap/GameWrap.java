package malilib.util.game.wrap;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.entity.living.player.InputPlayerEntity;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Container;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import malilib.util.position.HitResult;

public class GameWrap
{
    public static Minecraft getClient()
    {
        return Minecraft.getMinecraft();
    }

    @Nullable
    public static World getClientWorld()
    {
        return getClient().world;
    }

    @Nullable
    public static World getClientPlayersServerWorld()
    {
        return getClientWorld();
    }

    @Nullable
    public static InputPlayerEntity getClientPlayer()
    {
        return getClient().player;
    }

    @Nullable
    public static PlayerInventory getPlayerInventory()
    {
        PlayerEntity player = getClient().player;
        return player != null ? player.inventory : null;
    }

    @Nullable
    public static Container getPlayerInventoryContainer()
    {
        EntityPlayer player = getClient().player;
        return player != null ? player.inventoryContainer : null;
    }

    @Nullable
    public static Container getCurrentInventoryContainer()
    {
        EntityPlayer player = getClient().player;
        return player != null ? player.openContainer : null;
    }

    public static PlayerControllerMP getInteractionManager()
    {
        return getClient().playerController;
    }

    public static void clickSlot(int syncId, int slotId, int mouseButton, ClickType clickType)
    {
        PlayerControllerMP controller = getInteractionManager();

        if (controller != null)
        {
            controller.windowClick(syncId, slotId, mouseButton, clickType, getClientPlayer());
        }
    }

    public static double getPlayerReachDistance()
    {
        return getInteractionManager().getBlockReachDistance();
    }

    /*
    @Nullable
    public static MinecraftServer getIntegratedServer()
    {
        return getClient().getIntegratedServer();
    }
    */

    @Nullable
    public static NetHandlerPlayClient getNetworkConnection()
    {
        return getClient().getConnection();
    }

    public static GameOptions getOptions()
    {
        return getClient().options;
    }

    public static void printToChat(String msg)
    {
        getClient().ingameGUI.addChatMessage(ChatType.CHAT, new TextComponentString(msg));
    }

    public static void showHotbarMessage(String msg)
    {
        getClient().ingameGUI.addChatMessage(ChatType.GAME_INFO, new TextComponentString(msg));
    }

    public static boolean sendChatMessage(String command)
    {
        InputPlayerEntity player = getClientPlayer();

        if (player != null)
        {
            player.sendChat(command);
            return true;
        }

        return false;
    }

    public static boolean sendCommand(String command)
    {
        if (command.startsWith("/") == false)
        {
            command = "/" + command;
        }

        return sendChatMessage(command);
    }

    /**
     * @return The camera entity, if it's not null, otherwise returns the client player entity.
     */
    @Nullable
    public static Entity getCameraEntity()
    {
        Minecraft mc = getClient();
        Entity entity = mc.camera;
        return entity != null ? entity : mc.player;
    }

    public static String getPlayerName()
    {
        Entity player = getClientPlayer();
        return player != null ? player.getName() : "?";
    }

    public static HitResult getHitResult()
    {
        return HitResult.of(getClient().objectMouseOver);
    }

    public static long getCurrentWorldTick()
    {
        World world = getClientWorld();
        return world != null ? world.getTime() : -1L;
    }

    public static boolean isCreativeMode()
    {
        return false;
    }

    public static int getRenderDistanceChunks()
    {
        return getOptions().renderDistanceChunks;
    }

    public static int getVanillaOptionsScreenScale()
    {
        return GameWrap.getOptions().guiScale;
    }

    public static boolean isSinglePlayer()
    {
        return getClient().isSingleplayer();
    }

    public static boolean isUnicode()
    {
        return getClient().isUnicode();
    }

    public static boolean isHideGui()
    {
        return getOptions().hideGUI;
    }

    public static void scheduleToClientThread(Runnable task)
    {
        Minecraft mc = getClient();

        if (mc.isCallingFromMinecraftThread())
        {
            task.run();
        }
        else
        {
            mc.addScheduledTask(task);
        }
    }

    public static void profilerPush(String name)
    {
        //getClient().profiler.startSection(name);
    }

    public static void profilerPush(Supplier<String> nameSupplier)
    {
        //getClient().profiler.m_4994039(nameSupplier);
    }

    public static void profilerSwap(String name)
    {
        //getClient().profiler.endStartSection(name);
    }

    public static void profilerSwap(Supplier<String> nameSupplier)
    {
        //getClient().profiler.m_3681950(nameSupplier);
    }

    public static void profilerPop()
    {
        //getClient().profiler.endSection();
    }

    public static void openFile(Path file)
    {
        OpenGlHelper.openFile(file.toFile());
    }

    @Nullable
    public static Path getCurrentSinglePlayerWorldDirectory()
    {
        if (isSinglePlayer())
        {
            MinecraftServer server = getIntegratedServer();
            File file = server.getActiveAnvilConverter().getFile(server.getFolderName(), "icon.png");
            return file.getParentFile().toPath();
        }

        return null;
    }
}
