package sed.sky;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.TextureCubeMap;
import com.jme3.util.BufferUtils;

public class SkyBoxTexture extends TextureCubeMap {
    
    private static final int TexSize = 256;
    
    private SkyGradient skyGradient;
    private ScheduledThreadPoolExecutor executor;
    
    private long updateTime;
    
    public SkyBoxTexture(SkyGradient skyGradient, ScheduledThreadPoolExecutor executor) {
        this.skyGradient = skyGradient;
        this.executor = executor;
        ArrayList<ByteBuffer> faces = new ArrayList<ByteBuffer>();
        // +x
        faces.add(newFace());
        // -x
        faces.add(newFace());
        // +y
        faces.add(newFace());
        // -y
        faces.add(newFace());
        // +z
        faces.add(newFace());
        // -z
        faces.add(newFace());
        
        // TODO: what means depth in Image
        Image img = new Image(Image.Format.BGR8, TexSize, TexSize, 1, faces);
        setImage(img);
    }
    
    private ByteBuffer newFace() {
        ByteBuffer bb = BufferUtils.createByteBuffer(TexSize * TexSize * 3);
        int numElements = bb.capacity() / 3;
        for(int i = 0; i < numElements; i++) {
            bb.put((byte) 100);
            bb.put((byte) 30);
            bb.put((byte) 10);
        }
        return bb;
    }
    
    public void update() {
        long t0 = System.nanoTime();
        try {
            executor.invokeAll(Arrays.asList(
                new FaceUpdater(0), new FaceUpdater(1), new FaceUpdater(2),
                new FaceUpdater(3), new FaceUpdater(4), new FaceUpdater(5)
            ));
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
        long t1 = System.nanoTime();
        updateTime = t1-t0;
//        System.out.println(updateTime);
    }
    
    private void updateX(boolean posX, final int imageNum) {
        ByteBuffer buf = getImage().getData(imageNum);
        buf.rewind();
        float[] colors = new float[3];
        final float yInc = (-1) * (1f - (-1f)) / TexSize;
        final float zInc = (posX ? -1 : 1) * (1f - (-1f)) / TexSize;
        final float zInit = (posX ? 1 : -1);
        final float x = (posX ? +1 : -1);
        float y = +1f;
        for(int i = 0; i < TexSize / 2; i++) { // y
            int vonOben = i * TexSize * 3;
            int vonUnten = (TexSize - 1 - i) * TexSize * 3;
            float z = zInit;
            for(int j = 0; j < TexSize * 3; j += 3) { // z
                skyGradient.getSkycolor(colors, x, y, z);
                //ColorRGBA.randomColor().toArray(colors);
                putBGR(buf, vonOben + j, colors);
                putBGR(buf, vonUnten + j, colors);
                z += zInc;
            }
            y += yInc;
        }
        getImage().setData(imageNum, buf);
    }
    
    private void updateZ(boolean posZ, final int imageNum) {
        ByteBuffer buf = getImage().getData(imageNum);
        buf.rewind();
        float[] colors = new float[3];
        final float yInc = (-1) * (1f - (-1f)) / TexSize;
        final float xInc = (posZ ? +1 : -1) * (1f - (-1f)) / TexSize;
        final float xInit = (posZ ? -1 : +1);
        final float z = posZ ? +1 : -1;
        float y = +1f;
        for(int i = 0; i < TexSize / 2; i++) { // y
            int vonOben = i * TexSize * 3;
            int vonUnten = (TexSize - 1 - i) * TexSize * 3;
            float x = xInit;
            for(int j = 0; j < TexSize * 3; j += 3) { // x
                skyGradient.getSkycolor(colors, x, y, z);
                putBGR(buf, vonOben + j, colors);
                putBGR(buf, vonUnten + j, colors);
                x += xInc;
            }
            y += yInc;
        }
        getImage().setData(imageNum, buf);
    }
    
    private void updateY(boolean posY, final int imageNum) {
        ByteBuffer buf = getImage().getData(imageNum);
        buf.rewind();
        float[] colors = new float[3];
        final float zInc = (posY ? +1 : -1) * (1f - (-1f)) / TexSize;
        final float xInc = (1f - (-1f)) / TexSize;
        final float y = posY ? +1 : -1;
        float z = posY ? -1 : +1;
        //for(int i = 0; i < TexSize/2; i++) { // z
        for(int i = 0; i < TexSize; i++) { // z
            int vonOben = i * TexSize * 3;
            //int vonUnten = (TexSize-1-i)*TexSize*3;
            float x = -1f;
            for(int j = 0; j < TexSize * 3; j += 3) { // x
                skyGradient.getSkycolor(colors, x, y, z);
                putBGR(buf, vonOben + j, colors);
                //putBGR(buf, vonUnten+j, colors);
                x += xInc;
            }
            z += zInc;
        }
        getImage().setData(imageNum, buf);
    }
    
    private static final void putBGR(ByteBuffer bb, int offset, float[] colorRGBA) {
        // do not copy A from colorRGBA[3]
        bb.put(offset, (byte) (colorRGBA[2] * 255)); // B
        bb.put(offset + 1, (byte) (colorRGBA[1] * 255)); // G
        bb.put(offset + 2, (byte) (colorRGBA[0] * 255)); // R
    }
    
    private static final void putBGR(ByteBuffer bb, ColorRGBA c) {
        // do not copy A from c.a
        bb.put((byte) (c.b * 255));
        bb.put((byte) (c.g * 255));
        bb.put((byte) (c.r * 255));
    }
    
    public static final TextureCubeMap genRandomSkyBox(ColorRGBA top) {
        ArrayList<ByteBuffer> faces = new ArrayList<ByteBuffer>();
        byte[] base = new byte[] { (byte) 100, (byte) 30, (byte) 10 };
        // +x
        faces.add(BufferUtils.createByteBuffer(base));
        // -x
        faces.add(BufferUtils.createByteBuffer(base));
        // +y
        faces.add(BufferUtils.createByteBuffer(new byte[] { (byte) (top.b * 255),
                (byte) (top.g * 255), (byte) (top.r * 255) }));
        // -y
        faces.add(BufferUtils.createByteBuffer(base));
        // +z
        faces.add(BufferUtils.createByteBuffer(base));
        // -z
        faces.add(BufferUtils.createByteBuffer(base));
        // final image: 1x1 (depth?? 1)
        Image img = new Image(Image.Format.BGR8, 1, 1, 1, faces);
        return new TextureCubeMap(img);
    }
    
    private class FaceUpdater implements Callable<Void> {
        
        private int faceNr;
        
        public FaceUpdater(int faceNr) {
            this.faceNr = faceNr;
        }
        
        @Override
        public Void call() throws Exception {
            switch(faceNr) {
            case 0: updateX(true, 0); break;
            case 1: updateX(false, 1); break;
            case 2: updateY(true, 2); break;
            case 3: updateY(false, 3); break;
            case 4: updateZ(true, 4); break;
            case 5: updateZ(false, 5); break;
            }
            return null;
        }
    }
}
