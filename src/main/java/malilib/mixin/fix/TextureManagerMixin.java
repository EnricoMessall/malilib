package malilib.mixin.fix;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.render.texture.TextureManager;

@Mixin(TextureManager.class)
public class TextureManagerMixin
{
    @Unique private BufferedImage fallbackImage;

    @WrapOperation(method = "load",
                   at = @At(value = "INVOKE",
                            target = "Ljavax/imageio/ImageIO;read(Ljava/io/InputStream;)Ljava/awt/image/BufferedImage;"))
    private BufferedImage malilib$preventCrashes(InputStream is, Operation<BufferedImage> operation)
    {
        if (is == null)
        {
            return this.malilib$getFallbackImage();
        }

        try
        {
            BufferedImage img = operation.call(is);
            is.close();
            return img;
        }
        catch (Exception e)
        {
            return this.malilib$getFallbackImage();
        }
    }

    private BufferedImage malilib$getFallbackImage()
    {
        if (this.fallbackImage == null)
        {
            this.fallbackImage = new BufferedImage(16, 16, 2);
            Graphics graphics = this.fallbackImage.getGraphics();
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, 8, 8);
            graphics.fillRect(8, 8, 8, 8);
            graphics.setColor(Color.PINK);
            graphics.fillRect(0, 8, 8, 8);
            graphics.fillRect(8, 0, 8, 8);
            graphics.dispose();
        }

        return this.fallbackImage;
    }

    /* TODO in-20100223
    @Inject(method = "reload", at = @At("TAIL"))
    private void malilib_onPostReload(CallbackInfo ci)
    {
        TextRenderer.INSTANCE.onResourceManagerReload();
    }
    */
}
