package malilib.gui.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import com.google.common.collect.Sets;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.living.player.PlayerEntity;

import malilib.MaLiLib;
import malilib.config.value.HudAlignment;
import malilib.gui.widget.BaseTextFieldWidget;
import malilib.listener.EventListener;
import malilib.util.game.wrap.GameWrap;

public class GuiUtils
{
    private static final Set<String> LINK_PROTOCOLS = Sets.newHashSet("http", "https");

    public static int getScaledWindowWidth()
    {
        int scale = Math.min(getDisplayWidth() / 320, getDisplayHeight() / 240);
        scale = Math.max(scale, 1);
        return getDisplayWidth() / scale;
    }

    public static int getScaledWindowHeight()
    {
        int scale = Math.min(getDisplayWidth() / 320, getDisplayHeight() / 240);
        scale = Math.max(scale, 1);
        return getDisplayHeight() / scale;
    }

    public static int getVanillaScreenScale()
    {
        int scale = Math.min(getDisplayWidth() / 320, getDisplayHeight() / 240);
        scale = Math.min(scale, GameWrap.getVanillaOptionsScreenScale());
        scale = Math.max(scale, 1);

        if (GameWrap.isUnicode() && (scale & 0x1) != 0 && scale > 1)
        {
            scale -= 1;
        }

        return scale;
    }

    public static int getDisplayWidth()
    {
        return GameWrap.getClient().width;
    }

    public static int getDisplayHeight()
    {
        return GameWrap.getClient().height;
    }

    public static int getMouseScreenX()
    {
        Screen screen = getCurrentScreen();
        return screen != null ? getMouseScreenX(screen.width) : 0;
    }

    public static int getMouseScreenX(int screenWidth)
    {
        return Mouse.getEventX() * screenWidth / getDisplayWidth();
    }

    public static int getMouseScreenY()
    {
        Screen screen = getCurrentScreen();
        return screen != null ? getMouseScreenY(screen.height) : 0;
    }

    public static int getMouseScreenY(int screenHeight)
    {
        return screenHeight - Mouse.getEventY() * screenHeight / getDisplayHeight() - 1;
    }

    public static boolean isScreenOpen()
    {
        return getCurrentScreen() != null;
    }

    public static boolean noScreenOpen()
    {
        return isScreenOpen() == false;
    }

    @Nullable
    public static Screen getCurrentScreen()
    {
        return GameWrap.getClient().screen;
    }

    @Nullable
    public static <T> T getCurrentScreenIfMatches(Class<T> clazz)
    {
        Screen screen = getCurrentScreen();

        if (screen != null && clazz.isAssignableFrom(screen.getClass()))
        {
            return clazz.cast(screen);
        }

        return null;
    }

    public static void reInitCurrentScreen()
    {
        Screen screen = getCurrentScreen();

        if (screen != null)
        {
            screen.init();
        }
    }

    public static boolean isMouseInRegion(int mouseX, int mouseY, int x, int y, int width, int height)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static int getHudOffsetForPotions(HudAlignment alignment, double scale, PlayerEntity player)
    {
        // TODO b1.7.3
        /*
        if (alignment == HudAlignment.TOP_RIGHT)
        {
            // Only Chuck Norris can divide by zero
            if (scale == 0d)
            {
                return 0;
            }

            Collection<PotionEffect> effects = player.getActivePotionEffects();

            if (effects.isEmpty() == false)
            {
                int y1 = 0;
                int y2 = 0;

                for (PotionEffect effect : effects)
                {
                    Potion potion = effect.getPotion();

                    if (effect.doesShowParticles() && potion.hasStatusIcon())
                    {
                        if (potion.isBeneficial())
                        {
                            y1 = 26;
                        }
                        else
                        {
                            y2 = 52;
                            break;
                        }
                    }
                }

                return (int) (Math.max(y1, y2) / scale);
            }
        }
        */

        return 0;
    }

    public static int getHudPosY(int yOrig, int yOffset, int contentHeight, double scale, HudAlignment alignment)
    {
        int scaledHeight = GuiUtils.getScaledWindowHeight();
        int posY = yOrig;

        if (alignment == HudAlignment.BOTTOM_LEFT || alignment == HudAlignment.BOTTOM_RIGHT)
        {
            posY = (int) ((scaledHeight / scale) - contentHeight - yOffset);
        }
        else if (alignment == HudAlignment.CENTER)
        {
            posY = (int) ((scaledHeight / scale / 2.0d) - (contentHeight / 2.0d) + yOffset);
        }

        return posY;
    }

    public static boolean changeTextFieldFocus(List<BaseTextFieldWidget> textFields, boolean reverse)
    {
        final int size = textFields.size();

        if (size > 1)
        {
            int currentIndex = -1;

            for (int i = 0; i < size; ++i)
            {
                BaseTextFieldWidget textField = textFields.get(i);

                if (textField.isFocused())
                {
                    currentIndex = i;
                    textField.setFocused(false);
                    break;
                }
            }

            if (currentIndex != -1)
            {
                int count = size - 1;
                int newIndex = currentIndex + (reverse ? -1 : 1);

                for (int i = 0; i < count; ++i)
                {
                    if (newIndex >= size)
                    {
                        newIndex = 0;
                    }
                    else if (newIndex < 0)
                    {
                        newIndex = size - 1;
                    }

                    BaseTextFieldWidget textField = textFields.get(newIndex);

                    if (textField.isEnabled())
                    {
                        textField.setFocused(true);
                        return true;
                    }

                    newIndex += (reverse ? -1 : 1);
                }
            }
        }

        return false;
    }

    /**
     * Creates a click handler that opens the given URL via the vanilla OPEN_LINK
     * text component click handling.
     */
    public static EventListener createLabelClickHandlerForInfoUrl(String urlString)
    {
        return () -> tryOpenLink(urlString);
    }

    public static void tryOpenLink(String urlString)
    {
        try
        {
            URI uri = new URI(urlString);
            String s = uri.getScheme();

            if (s == null)
            {
                throw new URISyntaxException(urlString, "Missing protocol");
            }

            if (LINK_PROTOCOLS.contains(s.toLowerCase(Locale.ROOT)) == false)
            {
                throw new URISyntaxException(urlString, "Unsupported protocol: " + s.toLowerCase(Locale.ROOT));
            }

            final Screen currentScreen = getCurrentScreen();

            /* TODO b1.7.3
            if (GameWrap.getOptions().chatLinksPrompt)
            {
                //BaseScreen.openGui(new ConfirmActionScreen(320, "", () -> openWebLink(uri), getCurrentScreen(), ""));
                BaseScreen.openScreen(new GuiConfirmOpenLink((result, id) -> {
                    if (result && id == 31102009)
                        openWebLink(uri);
                    else
                        BaseScreen.openScreen(currentScreen);
                    }, urlString, 31102009, false));
            }
            else
            {
                openWebLink(uri);
            }
            */
        }
        catch (URISyntaxException urisyntaxexception)
        {
            MaLiLib.LOGGER.error("Can't open url for {}", urlString, urisyntaxexception);
        }
    }

    public static boolean openWebLink(URI uri)
    {
        try
        {
            Class<?> clazz = Class.forName("java.awt.Desktop");
            Object object = clazz.getMethod("getDesktop").invoke(null);
            clazz.getMethod("browse", URI.class).invoke(object, uri);
            return true;
        }
        catch (Throwable t)
        {
            Throwable throwable = t.getCause();
            MaLiLib.LOGGER.error("Couldn't open link: {}", (throwable == null ? "<UNKNOWN>" : throwable.getMessage()));
            return false;
        }
    }
}
