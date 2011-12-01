package sed;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.Logger;

import sed.sky.SkyBoxTexture;
import sed.sky.SkyDome;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.util.BufferUtils;

public class Main extends SimpleApplication {
	
	public static void main(String[] args) {
		Main main = new Main();
		main.setShowSettings(false);
		main.start();
	}
	
	// TODO: Key z -> y
	
	private float time_sky = 0;
	private Material s_mat;
	private SkyBoxTexture sky_box_texture;
	
	@Override
	public void simpleInitApp() {
		flyCam.setMoveSpeed(10 * 5);
//		flyCam.setDragToRotate(true);
		
		Box b = new Box(Vector3f.ZERO, 1, 1, 1);
		Geometry geom = new Geometry("Box", b);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Blue);
		geom.setMaterial(mat);
//		rootNode.attachChild(geom);
		
//		SkyDome s = new SkyDome();
		Box s = new Box(Vector3f.ZERO, 1, 1, 1);
		Geometry s_geom = new Geometry("SkyDome", s);
		s_mat = new Material(assetManager, "Shaders/Sky.j3md");
//		s_mat.getAdditionalRenderState().setWireframe(true);
		s_mat.setColor("Color", ColorRGBA.Gray);
		sky_box_texture = new SkyBoxTexture();
		s_mat.setTexture("SkyBox", sky_box_texture);
		s_geom.setMaterial(s_mat);
		rootNode.attachChild(s_geom);
		
		rootNode.setCullHint(CullHint.Never);
		
	}
	
	@Override
	public void simpleUpdate(float tpf) {		
		if(time_sky > 2) {
			sky_box_texture.update();
//			s_mat.setTexture("SkyBox", SkyBoxTexture.genRandomSkyBox(ColorRGBA.randomColor()));
			time_sky = 0;
		}
		
		time_sky += tpf;
	}
}
