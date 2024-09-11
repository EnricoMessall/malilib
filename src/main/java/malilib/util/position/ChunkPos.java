package malilib.util.position;

public class ChunkPos //extends net.minecraft.util.math.ChunkPos
{
    public final int x;
    public final int z;

    public ChunkPos(int x, int z)
    {
        this.x = x;
        this.z = z;
    }

    public int getX()
    {
        return this.x;
    }

    public int getZ()
    {
        return this.z;
    }

    @Override
    public String toString()
    {
        return "ChunkPos{x=" + this.x + ", z=" + this.z + "}";
    }

    public static long asLong(int chunkX, int chunkZ)
    {
        return ((long) chunkZ << 32) | (long) chunkX;
    }

    /*
    public static ChunkPos of(net.minecraft.util.math.ChunkPos pos)
    {
        return new ChunkPos(pos.x, pos.z);
    }
    */
}
