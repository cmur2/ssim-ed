package sed;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.Logger;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.TextureCubeMap;
import com.jme3.util.BufferUtils;

public class Main extends SimpleApplication {
	
	public static void main(String[] args) {
		Main main = new Main();
		main.setShowSettings(false);
		main.start();
	}
	
	private int cnt = 0;
	private Material s_mat;
	
	@Override
	public void simpleInitApp() {
		flyCam.setMoveSpeed(10 * 5);
//		flyCam.setDragToRotate(true);
		
		Box b = new Box(Vector3f.ZERO, 1, 1, 1);
		Geometry geom = new Geometry("Box", b);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Blue);
		geom.setMaterial(mat);
		rootNode.attachChild(geom);
		
		SkyDome s = new SkyDome();
		Geometry s_geom = new Geometry("SkyDome", s);
		s_mat = new Material(assetManager, "Shaders/Sky.j3md");
		s_mat.getAdditionalRenderState().setWireframe(true);
		s_mat.setColor("Color", ColorRGBA.Gray);
		s_mat.setTexture("SkyBox", skyBox(ColorRGBA.Cyan));
		s_geom.setMaterial(s_mat);
		rootNode.attachChild(s_geom);
		
		rootNode.setCullHint(CullHint.Never);
		
	}
	
	@Override
	public void simpleUpdate(float tpf) {
		
		cnt++;
		
		if(cnt == 100) {
			s_mat.setTexture("SkyBox", skyBox(ColorRGBA.randomColor()));
			cnt = 0;
		}
		
	}
	
	private TextureCubeMap skyBox(ColorRGBA top) {
		ArrayList<ByteBuffer> faces = new ArrayList<ByteBuffer>();
		
		// +x
		faces.add(BufferUtils.createByteBuffer(new byte[] {0x00, 0x00, (byte) 0xff}));
		// -x
		faces.add(BufferUtils.createByteBuffer(new byte[] {0x00, 0x00, (byte) 0xff}));
		// +y
		faces.add(BufferUtils.createByteBuffer(new byte[] {
				(byte) (top.b*255),
				(byte) (top.g*255),
				(byte) (top.r*255)}));
		// -y
		faces.add(BufferUtils.createByteBuffer(new byte[] {0x00, 0x00, (byte) 0xff}));
		// +z
		faces.add(BufferUtils.createByteBuffer(new byte[] {0x00, 0x00, (byte) 0xff}));
		// -z
		faces.add(BufferUtils.createByteBuffer(new byte[] {0x00, 0x00, (byte) 0xff}));
		
		Image img = new Image(Image.Format.BGR8, 1, 1, 1, faces); // depth??
		TextureCubeMap t = new TextureCubeMap(img);
		return t;
	}
}
