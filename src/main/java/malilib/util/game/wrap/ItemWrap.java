package malilib.util.game.wrap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemWrap
{
    public static final ItemStack EMPTY_STACK = null;

    /*
    @Nullable
    public static NbtCompound getTag(ItemStack stack)
    {
        return stack.getTagCompound();
    }

    public static void setTag(ItemStack stack, @Nullable NbtCompound tag)
    {
        stack.setTagCompound(tag);
    }
    */

    public static ItemStack fromTag(NbtCompound tag)
    {
        return new ItemStack(tag);
    }

    public static boolean isEmpty(ItemStack stack)
    {
        return stack == null || stack.size <= 0;
    }

    public static boolean notEmpty(ItemStack stack)
    {
        return stack != null && stack.size > 0;
    }

    public static String getStackString(ItemStack stack)
    {
        if (ItemWrap.notEmpty(stack))
        {
            String id = RegistryUtils.getItemIdStr(stack.getItem());
            return String.format("id: %s", id);
            /* TODO in-20100223
            NbtCompound tag = null; //getTag(stack);

            return String.format("[%s @ %d - display: %s - NBT: %s] (%s)",
                                 id != null ? id : "null", stack.getMetadata(), StringUtils.translate(stack.getTranslationKey()),
                                 tag != null ? tag.toString() : "<no NBT>", stack);
            */
        }

        return "<empty>";
    }
}
