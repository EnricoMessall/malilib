package malilib.util.game.wrap;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.InputPlayerEntity;
import net.minecraft.client.interaction.ClientPlayerInteractionManager;
import net.minecraft.client.options.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.world.World;

import malilib.util.position.HitResult;

public class GameWrap
{
    public static Minecraft minecraft;

    public static Minecraft getClient()
    {
        return minecraft;
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

    /* TODO in-20100223
    @Nullable
    public static InventoryMenu getPlayerInventoryContainer()
    {
        PlayerEntity player = getClient().player;
        return player != null ? player.playerMenu : null;
    }

    @Nullable
    public static InventoryMenu getCurrentInventoryContainer()
    {
        PlayerEntity player = getClient().player;
        return player != null ? player.menu : null;
    }
    */

    public static ClientPlayerInteractionManager getInteractionManager()
    {
        return getClient().interactionManager;
    }

    public static void clickSlot(int syncId, int slotId, int mouseButton, boolean quickMove)
    {
        /* TODO in-20100223
        ClientPlayerInteractionManager controller = getInteractionManager();

        if (controller != null)
        {
            controller.clickSlot(syncId, slotId, mouseButton, quickMove, getClientPlayer());
        }
        */
    }

    public static double getPlayerReachDistance()
    {
        return getInteractionManager().getReach();
    }

    /*
    @Nullable
    public static MinecraftServer getIntegratedServer()
    {
        return getClient().getIntegratedServer();
    }

    @Nullable
    public static ClientNetworkHandler getNetworkConnection()
    {
        InputPlayerEntity player = getClientPlayer();
        return player instanceof LocalPlayerEntity ? ((LocalPlayerEntity) player).networkHandler : null;
    }
    */

    public static GameOptions getOptions()
    {
        return getClient().options;
    }

    public static void printToChat(String msg)
    {
        /* TODO in-20100223
        getClient().gui.addChatMessage(msg);
        */
    }

    public static void showHotbarMessage(String msg)
    {
        // TODO b1.7.3
        //getClient().ingameGUI.addChatMessage(ChatType.GAME_INFO, new TextComponentString(msg));
    }

    public static boolean sendChatMessage(String command)
    {
        /* TODO in-20100223
        InputPlayerEntity player = getClientPlayer();

        if (player != null)
        {
            player.sendChat(command);
            return true;
        }
        */

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
        /* TODO in-20100223
        Minecraft mc = getClient();
        Entity entity = mc.camera;
        return entity != null ? entity : mc.player;
        */
        return getClientPlayer();
    }

    public static String getPlayerName()
    {
        /* TODO in-20100223
        PlayerEntity player = getClientPlayer();
        return player != null ? player.name : "?";
        */
        return "player";
    }

    public static HitResult getHitResult()
    {
        return HitResult.of(getClient().crosshairTarget);
    }

    public static long getCurrentWorldTick()
    {
        World world = getClientWorld();
        return world != null ? world.time : -1L;
    }

    public static boolean isCreativeMode()
    {
        return false;
    }

    public static int getRenderDistanceChunks()
    {
        return getOptions().viewDistance;
    }

    public static int getVanillaOptionsScreenScale()
    {
        /* TODO in-20100223
        return getOptions().guiScale;
        */
        return 2;
    }

    public static boolean isSinglePlayer()
    {
        /* TODO in-20100223
        return getClientWorld() != null && getClient().isMultiplayer() == false;
        */
        return true;
    }

    public static boolean isUnicode()
    {
        return false; // TODO b1.7.3 getClient().isUnicode();
    }

    public static boolean isHideGui()
    {
        return false; // TODO b1.7.3 getOptions().hideGUI;
    }

    public static void scheduleToClientThread(Runnable task)
    {
        /* TODO b1.7.3
        Minecraft mc = getClient();

        if (mc.isCallingFromMinecraftThread())
        {
            task.run();
        }
        else
        {
            mc.addScheduledTask(task);
        }
        */
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
        // TODO b1.7.3
        //OpenGlHelper.openFile(file.toFile());
    }

    @Nullable
    public static Path getCurrentSinglePlayerWorldDirectory()
    {
        /* TODO b1.7.3
        if (isSinglePlayer())
        {
            MinecraftServer server = getIntegratedServer();
            File file = server.getActiveAnvilConverter().getFile(server.getFolderName(), "icon.png");
            return file.getParentFile().toPath();
        }
        */

        return null;
    }

    public static BufferedImage createScreenshot(int width, int height)
    {
        IntBuffer buffer = BufferUtils.createIntBuffer(width * height);
        int[] intArr = new int[width * height];

        GL11.glPixelStorei(3333, 1);
        GL11.glPixelStorei(3317, 1);
        //GL11.glReadPixels(0, 0, width, height, 6407, 5121, buffer);
        //GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
        //GlStateManager.glReadPixels(0, 0, width, height, 32993, 33639, pixelBuffer);
        GL11.glReadPixels(0, 0, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
        buffer.clear();
        buffer.get(intArr);
        swapLineOrder(intArr, width, height);

        /*
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                int index = x + (height - y - 1) * width;
                int baseIndex = index * 3;
                int r = byteArr[baseIndex + 0] & 0xFF;
                int g = byteArr[baseIndex + 1] & 0xFF;
                int b = byteArr[baseIndex + 2] & 0xFF;
                int color = 0xFF000000 | (r << 16) | (g << 8) | b;
                intArr[x + y * width] = color;
            }
        }
        */

        BufferedImage image = new BufferedImage(width, height, 1);
        image.setRGB(0, 0, width, height, intArr, 0, width);

        return image;
    }

    private static void swapLineOrder(int[] arr, int width, int height) {
        int[] line = new int[width];
        int maxY = height / 2;

        for(int y = 0; y < maxY; ++y) {
            System.arraycopy(arr, y * width, line, 0, width);
            System.arraycopy(arr, (height - 1 - y) * width, arr, y * width, width);
            System.arraycopy(line, 0, arr, (height - 1 - y) * width, width);
        }
    }
}
