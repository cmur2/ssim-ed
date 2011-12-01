package sed.sky;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.TextureCubeMap;
import com.jme3.util.BufferUtils;

public class SkyBoxTexture extends TextureCubeMap {
	
	private static final int TexSize = 256;
	
	private SkyGradient sg;
	
	public SkyBoxTexture(SkyGradient sg) {
		this.sg = sg;
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
		ByteBuffer bb = BufferUtils.createByteBuffer(TexSize*TexSize*3);
		for(int i = 0; i < bb.capacity()/3; i++) {
			bb.put((byte) 100);
			bb.put((byte) 30);
			bb.put((byte) 10);
		}
		return bb;
	}
	
	public void update() {
		updatePosX();
		updateNegX();
		updatePosZ();
		updateNegZ();
		updatePosY();
		updateNegY();
	}
	
	private void updatePosX() {
		final int num = 0;
		ByteBuffer bb = getImage().getData(num);
		bb.rewind();
		// TODO: reallocation or not?
		//ByteBuffer bb = BufferUtils.createByteBuffer(TexSize*TexSize*3);

		// Testing:
//		putBGR(bb, ColorRGBA.Red);
//		putBGR(bb, ColorRGBA.Green);
//		putBGR(bb, ColorRGBA.Blue);
//		putBGR(bb, ColorRGBA.Yellow);
//		getImage().setData(num, bb);
//		if(true) return;
		
		float[] colors = new float[4];
        float yInc = (1f-(-1f))/TexSize;
        float zInc = (1f-(-1f))/TexSize;
        float y = +1f;
        for(int i = 0; i < TexSize/2; i++) { // y
        	int vonOben = i*TexSize*3;
            int vonUnten = (TexSize-1-i)*TexSize*3;
            float z = +1f;
            for(int j = 0; j < TexSize*3; j+=3) { // z
            	sg.getSkycolor(colors, 1f, y, z);
//            	ColorRGBA.randomColor().toArray(colors);
            	putBGR(bb, vonOben+j, colors);
            	putBGR(bb, vonUnten+j, colors);
                z -= zInc;
            }
            y -= yInc;
        }
        getImage().setData(num, bb);
	}
	
    private void updateNegX() {
		final int num = 1;
		ByteBuffer bb = getImage().getData(num);
		bb.rewind();
		float[] colors = new float[3];
        float yInc = (1f-(-1f))/TexSize;
        float zInc = (1f-(-1f))/TexSize;
        float[] pixMap = new float[TexSize*TexSize*3];
        float y = +1f;
        for(int i = 0; i < TexSize/2; i++) { // y
            int vonOben = i*TexSize*3;
            int vonUnten = (TexSize-1-i)*TexSize*3;
            float z = -1f;
            for(int j = 0; j < TexSize*3; j+=3) { // z
                sg.getSkycolor(colors, -1f, y, z);
                putBGR(bb, vonOben+j, colors);
            	putBGR(bb, vonUnten+j, colors);
                z += zInc;
            }
            y -= yInc;
        }
        getImage().setData(num, bb);
    }
    
    private void updatePosZ() {
    	final int num = 4;
		ByteBuffer bb = getImage().getData(num);
		bb.rewind();
        float[] colors = new float[3];
        float yInc = (1f-(-1f))/TexSize;
        float xInc = (1f-(-1f))/TexSize;
        float[] pixMap = new float[TexSize*TexSize*3];
        float y = +1f;
        for(int i = 0; i < TexSize/2; i++) { // y
            int vonOben = i*TexSize*3;
            int vonUnten = (TexSize-1-i)*TexSize*3;
            float x = -1f;
            for(int j = 0; j < TexSize*3; j+=3) { // x
                sg.getSkycolor(colors, x, y, +1f);
                putBGR(bb, vonOben+j, colors);
            	putBGR(bb, vonUnten+j, colors);
                x += xInc;
            }
            y -= yInc;
        }
        getImage().setData(num, bb);
    }
    
    private void updateNegZ() {
    	final int num = 5;
		ByteBuffer bb = getImage().getData(num);
		bb.rewind();
        float[] colors = new float[3];
        float yInc = (1f-(-1f))/TexSize;
        float xInc = (1f-(-1f))/TexSize;
        float[] pixMap = new float[TexSize*TexSize*3];
        float y = +1f;
        for(int i = 0; i < TexSize/2; i++) { // y
            int vonOben = i*TexSize*3;
            int vonUnten = (TexSize-1-i)*TexSize*3;
            float x = +1f;
            for(int j = 0; j < TexSize*3; j+=3) { // x
                sg.getSkycolor(colors, x, y, -1f);
                putBGR(bb, vonOben+j, colors);
            	putBGR(bb, vonUnten+j, colors);
                x -= xInc;
            }
            y -= yInc;
        }
        getImage().setData(num, bb);
    }
    
    private void updatePosY() {
    	final int num = 3;
		ByteBuffer bb = getImage().getData(num);
		bb.rewind();
        float[] colors = new float[3];
        float zInc = (1f-(-1f))/TexSize;
        float xInc = (1f-(-1f))/TexSize;
        float[] pixMap = new float[TexSize*TexSize*3];
        float z = +1f;
        //for(int i = 0; i < TexSize/2; i++) { // z
        for(int i = 0; i < TexSize; i++) { // z
            int vonOben = i*TexSize*3;
            //int vonUnten = (TexSize-1-i)*TexSize*3;
            float x = -1f;
            for(int j = 0; j < TexSize*3; j+=3) { // x
                sg.getSkycolor(colors, x, +1f, z);
                putBGR(bb, vonOben+j, colors);
                //pixMap[vonUnten+j+0] = colors[0]*255;
                //pixMap[vonUnten+j+1] = colors[1]*255;
                //pixMap[vonUnten+j+2] = colors[2]*255;
                x += xInc;
            }
            z -= zInc;
        }
        getImage().setData(num, bb);
    }

    private void updateNegY() {
    	final int num = 2;
		ByteBuffer bb = getImage().getData(num);
		bb.rewind();
        float[] colors = new float[3];
        float zInc = (1f-(-1f))/TexSize;
        float xInc = (1f-(-1f))/TexSize;
        float[] pixMap = new float[TexSize*TexSize*3];
        float z = -1f;
        //for(int i = 0; i < TexSize/2; i++) { // z
        for(int i = 0; i < TexSize; i++) { // z
            int vonOben = i*TexSize*3;
            //int vonUnten = (TexSize-1-i)*TexSize*3;
            float x = -1f;
            for(int j = 0; j < TexSize*3; j+=3) { // x
                sg.getSkycolor(colors, x, -1f, z);
                putBGR(bb, vonOben+j, colors);
                //pixMap[vonUnten+j+0] = colors[0]*255;
                //pixMap[vonUnten+j+1] = colors[1]*255;
                //pixMap[vonUnten+j+2] = colors[2]*255;
                x += xInc;
            }
            z += zInc;
        }
        getImage().setData(num, bb);
    }
    
	private static final void putBGR(ByteBuffer bb, int offset, float[] colorRGBA) {
		// do not copy A from colorRGBA[3]
		bb.put(offset,   (byte) (colorRGBA[2]*255)); // B
		bb.put(offset+1, (byte) (colorRGBA[1]*255)); // G
		bb.put(offset+2, (byte) (colorRGBA[0]*255)); // R
	}
	
	private static final void putBGR(ByteBuffer bb, ColorRGBA c) {
		// do not copy A from c.a
		bb.put((byte) (c.b*255));
		bb.put((byte) (c.g*255));
		bb.put((byte) (c.r*255));
	}
	
	public static final TextureCubeMap genRandomSkyBox(ColorRGBA top) {
		ArrayList<ByteBuffer> faces = new ArrayList<ByteBuffer>();
		byte[] base = new byte[] {(byte) 100, (byte) 30, (byte) 10};
		// +x
		faces.add(BufferUtils.createByteBuffer(base));
		// -x
		faces.add(BufferUtils.createByteBuffer(base));
		// +y
		faces.add(BufferUtils.createByteBuffer(new byte[] {
				(byte) (top.b*255),
				(byte) (top.g*255),
				(byte) (top.r*255)}));
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
}
