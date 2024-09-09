package malilib.render.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.BitSet;
import com.google.common.primitives.Floats;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.block.model.BakedQuad;

import malilib.MaLiLib;
import malilib.util.MathUtils;
import malilib.util.game.wrap.RenderWrap;

public class VanillaWrappingVertexBuilder implements VertexBuilder
{
    private static final ByteBuffer BYTE_BUFFER = allocateDirectByteBuffer(1048576 * 2);

    protected ByteBuffer byteBuffer;
    protected IntBuffer rawIntBuffer;
    protected ShortBuffer rawShortBuffer;
    protected FloatBuffer rawFloatBuffer;
    protected VertexFormat vertexFormat;
    protected boolean started;
    protected int glDrawMode;
    protected int vertexCount;
    protected int vertexSize;
    protected int[] quadDataWorkBuffer;

    public VanillaWrappingVertexBuilder(ByteBuffer buffer, int glDrawMode, malilib.render.buffer.VertexFormat vertexFormat)
    {
        buffer.rewind(); // The derived FooBuffer for some reason gets a capacity from the position to limit
        buffer.limit(buffer.capacity());
        this.byteBuffer = buffer;
        this.rawIntBuffer = buffer.asIntBuffer();
        this.rawShortBuffer = buffer.asShortBuffer();
        this.rawFloatBuffer = buffer.asFloatBuffer().asReadOnlyBuffer();
        this.glDrawMode = glDrawMode;
        this.vertexFormat = vertexFormat;
        this.vertexSize = vertexFormat.getSize();
        this.quadDataWorkBuffer = new int[this.vertexSize]; // vertexSize / 4 for bytes to ints, but 4 vertices per quad => just vertexSize
    }

    @Override
    public VertexBuilder pos(double x, double y, double z)
    {
        this.putPos(x, y, z);
        this.onVertexAdded();
        return this;
    }

    @Override
    public VertexBuilder posColor(double x, double y, double z, int r, int g, int b, int a)
    {
        this.putPos(x, y, z);
        this.putColor(r, g, b, a);
        this.onVertexAdded();
        return this;
    }

    @Override
    public VertexBuilder posUv(double x, double y, double z, float u, float v)
    {
        this.putPos(x, y, z);
        this.putUv(u, v);
        this.onVertexAdded();
        return this;
    }

    @Override
    public VertexBuilder posUvColor(double x, double y, double z, float u, float v, int r, int g, int b, int a)
    {
        this.putPos(x, y, z);
        this.putUv(u, v);
        this.putColor(r, g, b, a);
        this.onVertexAdded();
        return this;
    }

    @Override
    public VertexBuilder putBakedQuad(double x, double y, double z, BakedQuad quad, int colorARGB)
    {
        this.putBakedQuad(quad, colorARGB);
        this.addToQuadPosition(x, y, z);
        return this;
    }

    @Override
    public VertexBuilder putBakedQuad(BakedQuad quad, int colorARGB, int colorMultiplier)
    {
        int ca = (colorARGB >> 24) & 0xFF;
        int cr = (colorARGB >> 16) & 0xFF;
        int cg = (colorARGB >>  8) & 0xFF;
        int cb = (colorARGB      ) & 0xFF;
        int ma = (colorMultiplier >> 24) & 0xFF;
        int mr = (colorMultiplier >> 16) & 0xFF;
        int mg = (colorMultiplier >>  8) & 0xFF;
        int mb = (colorMultiplier      ) & 0xFF;
        int a = ca * ma / 255;
        int r = cr * mr / 255;
        int g = cg * mg / 255;
        int b = cb * mb / 255;
        int color = (a << 24) | (r << 16) | (g << 8) | b;

        return this.putBakedQuad(quad, color);
    }

    @Override
    public VertexBuilder putBakedQuad(BakedQuad quad, int colorARGB)
    {
        this.addQuadVertexData(quad.getVertexData());
        net.minecraft.util.math.Vec3i normal = quad.getFace().getDirectionVec();
        this.putNormalForQuad(normal.getX(), normal.getY(), normal.getZ());
        this.putColorForLastQuad(colorARGB);
        return this;
    }

    @Override
    public VertexBuilder putBlockQuad(double x, double y, double z,
                                      BakedQuad quad, float cma, float cmr, float cmg, float cmb,
                                      int lightMapVertex0, int lightMapVertex1, int lightMapVertex2, int lightMapVertex3)
    {
        System.arraycopy(quad.getVertexData(), 0, this.quadDataWorkBuffer, 0, this.quadDataWorkBuffer.length);

        int offset = this.vertexSize >> 2; // vertex size in int buffer entries
        int index = this.vertexFormat.getColorOffset() >> 2;
        this.quadDataWorkBuffer[index             ] = this.getMultipliedPackedColor(this.quadDataWorkBuffer[index             ], cma, cmr, cmg, cmb);
        this.quadDataWorkBuffer[index + offset    ] = this.getMultipliedPackedColor(this.quadDataWorkBuffer[index + offset    ], cma, cmr, cmg, cmb);
        this.quadDataWorkBuffer[index + offset * 2] = this.getMultipliedPackedColor(this.quadDataWorkBuffer[index + offset * 2], cma, cmr, cmg, cmb);
        this.quadDataWorkBuffer[index + offset * 3] = this.getMultipliedPackedColor(this.quadDataWorkBuffer[index + offset * 3], cma, cmr, cmg, cmb);

        index = this.vertexFormat.getLightMapOffset() >> 2;
        this.quadDataWorkBuffer[index             ] = lightMapVertex0;
        this.quadDataWorkBuffer[index + offset    ] = lightMapVertex1;
        this.quadDataWorkBuffer[index + offset * 2] = lightMapVertex2;
        this.quadDataWorkBuffer[index + offset * 3] = lightMapVertex3;

        /*
        net.minecraft.util.math.Vec3i normal = quad.getFace().getDirectionVec();
        int packedNormal = this.getPackedNormal(normal.getX(), normal.getY(), normal.getZ());
        index = this.vertexFormat.getNormalOffset() >> 2;
        this.quadDataWorkBuffer[index             ] = packedNormal;
        this.quadDataWorkBuffer[index + offset    ] = packedNormal;
        this.quadDataWorkBuffer[index + offset * 2] = packedNormal;
        this.quadDataWorkBuffer[index + offset * 3] = packedNormal;
        */

        index = this.vertexFormat.getPositionOffset() >> 2;

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            int baseOffset = index + vertexIndex * offset;
            this.quadDataWorkBuffer[baseOffset    ] = Float.floatToRawIntBits((float) x + Float.intBitsToFloat(this.quadDataWorkBuffer[baseOffset    ]));
            this.quadDataWorkBuffer[baseOffset + 1] = Float.floatToRawIntBits((float) y + Float.intBitsToFloat(this.quadDataWorkBuffer[baseOffset + 1]));
            this.quadDataWorkBuffer[baseOffset + 2] = Float.floatToRawIntBits((float) z + Float.intBitsToFloat(this.quadDataWorkBuffer[baseOffset + 2]));
        }

        this.addQuadVertexData(this.quadDataWorkBuffer);

        return this;
    }

    @Override
    public boolean isStarted()
    {
        return this.started;
    }

    @Override
    public VertexBuilder start()
    {
        return this.start(this.glDrawMode, this.vertexFormat);
    }

    @Override
    public VertexBuilder start(int glDrawMode, VertexFormat format)
    {
        if (this.started == false)
        {
            this.started = true;
            this.glDrawMode = glDrawMode;
            this.vertexFormat = format;
            this.vertexSize = format.getSize();
            this.byteBuffer.limit(this.byteBuffer.capacity());

            if (this.quadDataWorkBuffer.length < this.vertexSize)
            {
                this.quadDataWorkBuffer = new int[this.vertexSize]; // vertexSize / 4 for bytes to ints, but 4 vertices per quad => just vertexSize
            }

            this.reset();
        }

        return this;
    }

    @Override
    public void finishDrawing()
    {
        if (this.started)
        {
            this.byteBuffer.position(0);
            this.byteBuffer.limit(this.vertexCount * this.vertexSize);
        }

        this.started = false;
    }

    @Override
    public void draw()
    {
        if (this.started)
        {
            if (this.vertexFormat.hasTexture())
            {
                RenderWrap.enableTexture2D();
            }
            else
            {
                RenderWrap.disableTexture2D();
            }

            this.drawNoModeChanges();

            RenderWrap.enableTexture2D();
        }
    }

    @Override
    public void drawNoModeChanges()
    {
        if (this.started)
        {
            this.finishDrawing();

            if (this.vertexCount > 0)
            {
                this.vertexFormat.setupDraw(this.byteBuffer);
                RenderWrap.glDrawArrays(this.glDrawMode, 0, this.vertexCount);
                this.vertexFormat.disableAfterDraw();
            }

            this.reset();
        }
    }

    @Override
    public void reset()
    {
        this.vertexCount = 0;
    }

    @Override
    public int getGlDrawMode()
    {
        return this.glDrawMode;
    }

    @Override
    public int getVertexCount()
    {
        return this.vertexCount;
    }

    @Override
    public VertexFormat getVertexFormat()
    {
        return this.vertexFormat;
    }

    @Override
    public ByteBuffer getByteBuffer()
    {
        return this.byteBuffer;
    }

    @Override
    public void uploadVertexData()
    {
    }

    @Override
    public VertexBuilderState getState()
    {
        int intSize = this.vertexCount * (this.vertexSize >> 2);
        int[] arr = new int[intSize];

        this.rawIntBuffer.rewind();
        this.rawIntBuffer.limit(intSize);
        this.rawIntBuffer.get(arr);
        this.rawIntBuffer.position(intSize);
        this.rawIntBuffer.limit(this.rawIntBuffer.capacity());

        return new VertexBuilderState(this.vertexFormat, arr);
    }

    @Override
    public void setState(VertexBuilderState state)
    {
        int[] vertexData = state.getVertexData();

        this.growBuffer(vertexData.length << 2);
        this.byteBuffer.clear();

        this.vertexCount = state.getVertexCount();
        this.vertexFormat = state.getVertexFormat();
        this.vertexSize = this.vertexFormat.getSize();
        this.rawIntBuffer.put(vertexData);

        if (this.quadDataWorkBuffer.length < this.vertexSize)
        {
            this.quadDataWorkBuffer = new int[this.vertexSize]; // vertexSize / 4 for bytes to ints, but 4 vertices per quad => just vertexSize
        }
    }

    /*
    @Override
    public BufferBuilder.State getVertexData()
    {
        return this.buffer.getVertexState();
    }

    @Override
    public void setVertexData(BufferBuilder.State date)
    {
        this.buffer.setVertexState(date);
    }

    @Override
    public void addVertexData(int[] data)
    {
        this.buffer.addVertexData(data);
    }

    @Override
    public void putPosition(double x, double y, double z)
    {
        this.buffer.putPosition(x, y, z);
    }

    @Override
    public void putNormal(float x, float y, float z)
    {
        this.buffer.putNormal(x, y, z);
    }

    @Override
    public void putColorMultiplier(float r, float g, float b, int vertexIndex)
    {
        this.buffer.putColorMultiplier(r, g, b, vertexIndex);
    }

    @Override
    public void putQuadColor(int argb)
    {
        this.buffer.putColor4(argb);
    }

    @Override
    public void putQuadColor(float r, float g, float b)
    {
        this.buffer.putColorRGB_F4(r, g, b);
    }

    @Override
    public void putBrightness(int vertex0, int vertex1, int vertex2, int vertex3)
    {
        this.buffer.putBrightness4(vertex0, vertex1, vertex2, vertex3);
    }

    @Override
    public void sortVertexData(float cameraX, float cameraY, float cameraZ)
    {
        this.buffer.sortVertexData(cameraX, cameraY, cameraZ);
    }
    */

    protected void putPos(double x, double y, double z)
    {
        int startIndex = this.vertexCount * this.vertexSize + this.vertexFormat.getPositionOffset();
        this.byteBuffer.putFloat(startIndex    , (float) x);
        this.byteBuffer.putFloat(startIndex + 4, (float) y);
        this.byteBuffer.putFloat(startIndex + 8, (float) z);
    }

    protected void putColor(int r, int g, int b, int a)
    {
        int startIndex = this.vertexCount * this.vertexSize + this.vertexFormat.getColorOffset();

        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
        {
            this.byteBuffer.put(startIndex    , (byte) r);
            this.byteBuffer.put(startIndex + 1, (byte) g);
            this.byteBuffer.put(startIndex + 2, (byte) b);
            this.byteBuffer.put(startIndex + 3, (byte) a);
        }
        else
        {
            this.byteBuffer.put(startIndex    , (byte) a);
            this.byteBuffer.put(startIndex + 1, (byte) b);
            this.byteBuffer.put(startIndex + 2, (byte) g);
            this.byteBuffer.put(startIndex + 3, (byte) r);
        }
    }

    protected void putUv(float u, float v)
    {
        int startIndex = this.vertexCount * this.vertexSize + this.vertexFormat.getUvOffset();
        this.byteBuffer.putFloat(startIndex    , u);
        this.byteBuffer.putFloat(startIndex + 4, v);
    }

    protected void addQuadVertexData(int[] vertexData)
    {
        // TODO verify the array length?
        this.growBuffer(vertexData.length * 4);
        this.rawIntBuffer.position(this.vertexCount * (this.vertexSize >> 2));
        this.rawIntBuffer.put(vertexData);
        this.vertexCount += vertexData.length / (this.vertexSize >> 2);
    }

    protected void putIntValueForLastQuad(int value, int offsetInVertexAsBytes)
    {
        // Get the data index of the first vertex of the last 4 vertices, as an int buffer offset
        int startIndex = ((this.vertexCount - 4) * this.vertexSize + offsetInVertexAsBytes) >> 2;
        int offset = this.vertexSize >> 2; // vertex size in int buffer entries

        this.rawIntBuffer.put(startIndex             , value);
        this.rawIntBuffer.put(startIndex + offset    , value);
        this.rawIntBuffer.put(startIndex + offset * 2, value);
        this.rawIntBuffer.put(startIndex + offset * 3, value);
    }

    protected int getPackedNormal(int x, int y, int z)
    {
        x = (x * 127) & 0xFF;
        y = (y * 127) & 0xFF;
        z = (z * 127) & 0xFF;

        return (z << 16) | (y << 8) | x;
    }

    protected void putNormalForQuad(int x, int y, int z)
    {
        int packedValue = this.getPackedNormal(x, y, z);
        this.putIntValueForLastQuad(packedValue, this.vertexFormat.getNormalOffset());
    }

    protected int getMultipliedPackedColor(int nativeColor, float ma, float mr, float mg, float mb)
    {
        int a;
        int r;
        int g;
        int b;

        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
        {
            a = ((int) ((nativeColor >> 24) * ma)) & 0xFF;
            b = ((int) ((nativeColor >> 16) * mb)) & 0xFF;
            g = ((int) ((nativeColor >>  8) * mg)) & 0xFF;
            r = ((int) ((nativeColor      ) * mr)) & 0xFF;
        }
        else
        {
            r = ((int) ((nativeColor >> 24) * mr)) & 0xFF;
            g = ((int) ((nativeColor >> 16) * mg)) & 0xFF;
            b = ((int) ((nativeColor >>  8) * mb)) & 0xFF;
            a = ((int) ((nativeColor      ) * ma)) & 0xFF;
        }

        return this.getPackedColor(a, r, g, b);
    }

    protected int getPackedColor(int argb)
    {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        int b = (argb      ) & 0xFF;

        return this.getPackedColor(a, r, g, b);
    }

    protected int getPackedColor(int a, int r, int g, int b)
    {
        int packedValue;

        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
        {
            packedValue = (a << 24) | (b << 16) | (g << 8) | r;
        }
        else
        {
            packedValue = (r << 24) | (g << 16) | (b << 8) | a;
        }

        return packedValue;
    }

    protected void putColorForLastQuad(int argb)
    {
        int packedValue = this.getPackedColor(argb);
        this.putIntValueForLastQuad(packedValue, this.vertexFormat.getColorOffset());
    }

    protected void putColorForLastQuad(int a, int r, int g, int b)
    {
        int packedValue = this.getPackedColor(a, r, g, b);
        this.putIntValueForLastQuad(packedValue, this.vertexFormat.getColorOffset());
    }

    protected void putLightMapForLastQuad(int v0, int v1, int v2, int v3)
    {
        // Get the data index of the first vertex of the last 4 vertices, as an int buffer offset
        int startIndex = ((this.vertexCount - 4) * this.vertexSize + this.vertexFormat.getLightMapOffset()) >> 2;
        int offset = this.vertexSize >> 2; // vertex size in int buffer entries

        this.rawIntBuffer.put(startIndex             , v0);
        this.rawIntBuffer.put(startIndex + offset    , v1);
        this.rawIntBuffer.put(startIndex + offset * 2, v2);
        this.rawIntBuffer.put(startIndex + offset * 3, v3);
    }

    protected void addToQuadPosition(double x, double y, double z)
    {
        // Get the data index of the first vertex of the last 4 vertices, as an int buffer offset
        int startIndex = ((this.vertexCount - 4) * this.vertexSize + this.vertexFormat.getPositionOffset()) >> 2;
        int offset = this.vertexSize >> 2; // vertex size in int buffer entries

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            int baseOffset = startIndex + vertexIndex * offset;
            this.rawIntBuffer.put(baseOffset    , Float.floatToRawIntBits((float) x + Float.intBitsToFloat(this.rawIntBuffer.get(baseOffset    ))));
            this.rawIntBuffer.put(baseOffset + 1, Float.floatToRawIntBits((float) y + Float.intBitsToFloat(this.rawIntBuffer.get(baseOffset + 1))));
            this.rawIntBuffer.put(baseOffset + 2, Float.floatToRawIntBits((float) z + Float.intBitsToFloat(this.rawIntBuffer.get(baseOffset + 2))));
        }
    }

    protected void putValueForQuad(int value, int offsetInVertexAsBytes, int[] buffer)
    {
        int startIndex = offsetInVertexAsBytes >> 2;
        int offset = this.vertexSize >> 2; // vertex size in int buffer entries

        buffer[startIndex             ] = value;
        buffer[startIndex + offset    ] = value;
        buffer[startIndex + offset * 2] = value;
        buffer[startIndex + offset * 3] = value;
    }

    protected void putValuesForQuad(int v0, int v1, int v2, int v3, int offsetInVertexAsBytes, int[] buffer)
    {
        int startIndex = offsetInVertexAsBytes >> 2;
        int offset = this.vertexSize >> 2; // vertex size in int buffer entries

        buffer[startIndex             ] = v0;
        buffer[startIndex + offset    ] = v1;
        buffer[startIndex + offset * 2] = v2;
        buffer[startIndex + offset * 3] = v3;
    }

    protected void onVertexAdded()
    {
        ++this.vertexCount;
        this.growBuffer(this.vertexSize);
    }

    protected void growBuffer(int increaseAmount)
    {
        if (this.vertexCount * this.vertexSize + increaseAmount > this.byteBuffer.capacity())
        {
            int currentCapacity = this.byteBuffer.capacity();
            int newCapacity = currentCapacity + MathUtils.roundUp(increaseAmount, 2097152);
            MaLiLib.LOGGER.debug("VertexBuilder#growBuffer(): Old size {} B, new size {} B", currentCapacity, newCapacity);

            ByteBuffer newByteBuffer = allocateDirectByteBuffer(newCapacity);
            int position = this.rawIntBuffer.position();

            this.byteBuffer.position(0);
            newByteBuffer.put(this.byteBuffer);
            newByteBuffer.rewind();
            this.byteBuffer = newByteBuffer;
            this.rawFloatBuffer = this.byteBuffer.asFloatBuffer().asReadOnlyBuffer();
            this.rawIntBuffer = this.byteBuffer.asIntBuffer();
            this.rawShortBuffer = this.byteBuffer.asShortBuffer();
            this.rawIntBuffer.position(position);
            this.rawShortBuffer.position(position << 1);
        }
    }

    private static float getQuadDistanceSq(FloatBuffer floatBufferIn, float x, float y, float z, int integerSize, int offset)
    {
        float f = floatBufferIn.get(offset + integerSize * 0 + 0);
        float g = floatBufferIn.get(offset + integerSize * 0 + 1);
        float h = floatBufferIn.get(offset + integerSize * 0 + 2);
        float i = floatBufferIn.get(offset + integerSize * 1 + 0);
        float j = floatBufferIn.get(offset + integerSize * 1 + 1);
        float k = floatBufferIn.get(offset + integerSize * 1 + 2);
        float l = floatBufferIn.get(offset + integerSize * 2 + 0);
        float m = floatBufferIn.get(offset + integerSize * 2 + 1);
        float n = floatBufferIn.get(offset + integerSize * 2 + 2);
        float o = floatBufferIn.get(offset + integerSize * 3 + 0);
        float p = floatBufferIn.get(offset + integerSize * 3 + 1);
        float q = floatBufferIn.get(offset + integerSize * 3 + 2);
        float r = (f + i + l + o) * 0.25F - x;
        float s = (g + j + m + p) * 0.25F - y;
        float t = (h + k + n + q) * 0.25F - z;
        return r * r + s * s + t * t;
    }

    @Override
    public void sortVertices(float cameraX, float cameraY, float cameraZ)
    {
        final int quadCount = this.vertexCount / 4;
        final int vertexSize = this.vertexSize;
        final int intSize = this.vertexFormat.getSize() >> 2;
        final float[] quadDistances = new float[quadCount];

        for (int index = 0; index < quadCount; ++index)
        {
            quadDistances[index] = getQuadDistanceSq(this.rawFloatBuffer, cameraX, cameraY, cameraZ, intSize, index * vertexSize);
        }

        IntArrayList list = new IntArrayList(quadCount);
        int[] integers = new int[quadCount];

        for (int i = 0; i < integers.length; ++i)
        {
            integers[i] = i;
            list.add(i);
        }

        list.sort((index1, index2) -> Floats.compare(quadDistances[index2], quadDistances[index1]));

        BitSet bitSet = new BitSet();
        int[] is = new int[vertexSize];

        for (int index = bitSet.nextClearBit(0); index < quadCount; index = bitSet.nextClearBit(index + 1))
        {
            int newIndex = integers[index];

            if (newIndex != index)
            {
                this.rawIntBuffer.limit(newIndex * vertexSize + vertexSize);
                this.rawIntBuffer.position(newIndex * vertexSize);
                this.rawIntBuffer.get(is);
                int o = newIndex;

                for (int p = integers[newIndex]; o != index; p = integers[p])
                {
                    this.rawIntBuffer.limit(p * vertexSize + vertexSize);
                    this.rawIntBuffer.position(p * vertexSize);
                    IntBuffer intBuffer = this.rawIntBuffer.slice();
                    this.rawIntBuffer.limit(o * vertexSize + vertexSize);
                    this.rawIntBuffer.position(o * vertexSize);
                    this.rawIntBuffer.put(intBuffer);
                    bitSet.set(o);
                    o = p;
                }

                this.rawIntBuffer.limit(index * vertexSize + vertexSize);
                this.rawIntBuffer.position(index * vertexSize);
                this.rawIntBuffer.put(is);
            }

            bitSet.set(index);
        }
    }


    private static float getQuadDistanceSq(int[] vertexData, float x, float y, float z, int vertexIntSize, int offset)
    {
        float x1 = Float.intBitsToFloat(vertexData[offset                        ]);
        float y1 = Float.intBitsToFloat(vertexData[offset                     + 1]);
        float z1 = Float.intBitsToFloat(vertexData[offset                     + 2]);
        float x2 = Float.intBitsToFloat(vertexData[offset + vertexIntSize        ]);
        float y2 = Float.intBitsToFloat(vertexData[offset + vertexIntSize     + 1]);
        float z2 = Float.intBitsToFloat(vertexData[offset + vertexIntSize     + 2]);
        float x3 = Float.intBitsToFloat(vertexData[offset + vertexIntSize * 2    ]);
        float y3 = Float.intBitsToFloat(vertexData[offset + vertexIntSize * 2 + 1]);
        float z3 = Float.intBitsToFloat(vertexData[offset + vertexIntSize * 2 + 2]);
        float x4 = Float.intBitsToFloat(vertexData[offset + vertexIntSize * 3    ]);
        float y4 = Float.intBitsToFloat(vertexData[offset + vertexIntSize * 3 + 1]);
        float z4 = Float.intBitsToFloat(vertexData[offset + vertexIntSize * 3 + 2]);

        float distX = (x1 + x2 + x3 + x4) * 0.25F - x;
        float distY = (y1 + y2 + y3 + y4) * 0.25F - y;
        float distZ = (z1 + z2 + z3 + z4) * 0.25F - z;

        return distX * distX + distY * distY + distZ * distZ;
    }

    public static int[] sortVertices(float cameraX, float cameraY, float cameraZ, int[] vertexData, int vertexSize)
    {
        final int vertexIntSize = vertexSize >> 2;
        final int quadCount = vertexData.length / vertexIntSize / 4;
        final float[] quadDistances = new float[quadCount];

        for (int index = 0; index < quadCount; ++index)
        {
            // Note: The offset is basically quadIndex * vertexIntSize * 4, which is quadIndex * vertexSize, although it doesn't "read clearly"
            quadDistances[index] = getQuadDistanceSq(vertexData, cameraX, cameraY, cameraZ, vertexIntSize, index * vertexSize);
        }

        IntArrayList list = new IntArrayList(quadCount);

        for (int i = 0; i < quadCount; ++i)
        {
            list.add(i);
        }

        list.sort((index1, index2) -> Floats.compare(quadDistances[index2], quadDistances[index1]));

        int[] sortedVertexData = new int[vertexData.length];

        for (int index = 0; index < quadCount; ++index)
        {
            int sortedIndex = list.getInt(index);
            // Note: The offset is basically quadIndex * vertexIntSize * 4,
            // which is quadIndex * vertexSize, although it doesn't "read clearly".
            // And the length of one quad in ints is the same as the vertexSize (in bytes).
            System.arraycopy(vertexData, index * vertexSize, sortedVertexData, sortedIndex * vertexSize, vertexSize);
        }

        return sortedVertexData;
    }

    public static VertexBuilder coloredLines()
    {
        return create(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
    }

    public static VertexBuilder coloredLineStrip()
    {
        return create(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
    }

    public static VertexBuilder coloredLineLoop()
    {
        return create(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
    }

    public static VertexBuilder coloredQuads()
    {
        return create(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
    }

    public static VertexBuilder texturedQuad()
    {
        return create(GL11.GL_QUADS, VertexFormats.POSITION_TEX);
    }

    public static VertexBuilder tintedTexturedQuad()
    {
        return create(GL11.GL_QUADS, VertexFormats.POSITION_TEX_COLOR);
    }

    public static VertexBuilder coloredTriangles()
    {
        return create(GL11.GL_TRIANGLES, VertexFormats.POSITION_COLOR);
    }

    public static VertexBuilder coloredTriangleStrip()
    {
        return create(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
    }

    /**
     * Creates and returns a VertexBuilder using a static/shared ByteBuffer.
     * Note: The builder is also started using the given modes.
     */
    public static VertexBuilder create(int glMode, malilib.render.buffer.VertexFormat vertexFormat)
    {
        return create(BYTE_BUFFER, glMode, vertexFormat);
    }

    /**
     * Creates a VertexBuilder using a newly allocated ByteBuffer with the given capacity.
     * Note: The builder is also started using the given modes.
     */
    public static VertexBuilder create(int capacity, int glMode, malilib.render.buffer.VertexFormat vertexFormat)
    {
        ByteBuffer buffer = allocateDirectByteBuffer(capacity * 4);
        return create(buffer, glMode, vertexFormat);
    }

    /**
     * Creates and returns a VertexBuilder using the provided ByteBuffer.
     * Note: The builder is also started using the given modes.
     */
    public static VertexBuilder create(ByteBuffer buffer, int glMode, VertexFormat vertexFormat)
    {
        VanillaWrappingVertexBuilder builder = new VanillaWrappingVertexBuilder(buffer, glMode, vertexFormat);
        builder.start();

        return builder;
    }

    public static synchronized ByteBuffer allocateDirectByteBuffer(int capacity)
    {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }
}
