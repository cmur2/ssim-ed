package de.mycrobase.ssim.ed.sky;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import ssim.util.MathExt;

import com.jme3.math.Vector2f;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

import de.mycrobase.ssim.ed.util.TempVars;

public class SunTexture extends Texture2D {
    
    private static final Logger logger = Logger.getLogger(SunTexture.class);
    
    private static final int TexSize = 256;
    
    // simple
    private static final float SunRadius = 12.0f;
    
    // lensflare
    private static final int NumRays = 6;
    private static final float RayWidth = 1.0f;
    private static final float LfShininessVariation = 0.015f;
    
    // theta ranges
    private static final float SunLensflareThetaMax = 55f;
    private static final float SunNormalThetaMax = 75f;
    
    private float lensflareShininess;
    private boolean lensflareEnabled;
    /**
     * when set forces rerendering of lensflare image
     */
    private boolean dirty;
    
    private Sun sun;
    
    /**
     * cache and lazy init images, see {@link #dirty}
     */
    private ByteBuffer emptyImage;
    private ByteBuffer simpleImage;
    private ByteBuffer lensflareImage;
    
    public SunTexture(Sun sun) {
        this.sun = sun;
        Image img = new Image(Image.Format.ABGR8, TexSize, TexSize, getEmptyImage());
        setImage(img);
    }
    
    public void setLensflareShininess(float lensflareShininess) {
        if(lensflareShininess == this.lensflareShininess) {
            return;
        }
        dirty = true;
        this.lensflareShininess = lensflareShininess;
    }
    
    public float getLensflareShininess() {
        return lensflareShininess;
    }
    
    public void setLensflareEnabled(boolean lensflareEnabled) {
        if(lensflareEnabled == this.lensflareEnabled) {
            return;
        }
        dirty = true;
        this.lensflareEnabled = lensflareEnabled;
    }
    
    public boolean getLensflareEnabled() {
        return lensflareEnabled;
    }
    
    public void update() {
        TempVars vars = TempVars.get();
        Vector2f sunAngles = sun.getSunAngles(vars.vect10);
        float sunThetaDeg = (float) Math.toDegrees(sunAngles.y);
        // theta=0 -> zenith
        // theta=90 deg -> horizon
        if(sunThetaDeg < SunLensflareThetaMax) {
            getImage().setData(getLensflareImage());
        } else if(sunThetaDeg < SunNormalThetaMax) {
            getImage().setData(getSimpleImage());
        } else {
            getImage().setData(getEmptyImage());
        }
        vars.release();
    }
    
    private ByteBuffer getEmptyImage() {
        if(emptyImage == null) {
            logger.debug("Generating empty sun texture");
            emptyImage = BufferUtils.createByteBuffer(TexSize * TexSize * 4);
            for(int i = 0; i < emptyImage.capacity()/4; i++) {
                emptyImage.put((byte) 0); // A
                emptyImage.put((byte) 255); // B
                emptyImage.put((byte) 255); // G
                emptyImage.put((byte) 255); // R
            }
        }
        return emptyImage;
    }
    
    private ByteBuffer getSimpleImage() {
        if(simpleImage == null) {
            logger.debug("Generating simple sun texture");
            float[][] alphas = new float[TexSize][TexSize];
            generateGlow(alphas);
            simpleImage = generateByteBuffer(alphas);
        }
        return simpleImage;
    }
    
    private ByteBuffer getLensflareImage() {
        if(lensflareImage == null || dirty) {
            logger.debug("Generating lensflare sun texture");
            float[][] alphas = new float[TexSize][TexSize];
            generateGlow(alphas);
            if(lensflareEnabled) { 
                generateFlares(alphas);
            }
            lensflareImage = generateByteBuffer(alphas);
            dirty = false;
        }
        return lensflareImage;
    }
    
    private ByteBuffer generateByteBuffer(float[][] alphas) {
        ByteBuffer bb = BufferUtils.createByteBuffer(TexSize * TexSize * 4);
        for(int column = 0; column < TexSize; column++) {
            for(int row = 0; row < TexSize; row++) {
                int index = (row*TexSize + column) * 4;
                float alpha = MathExt.clamp(alphas[column][row], 0, 255);
                bb.put(index+0, (byte) (alpha)); // A
                bb.put(index+1, (byte) (255)); // B
                bb.put(index+2, (byte) (255)); // G
                bb.put(index+3, (byte) (255)); // R
            }
        }
        return bb;
    }
    
    private void generateGlow(float[][] alphas) {
        //final float FalloffK = 0.95f;
        final float CutAlpha = 1f; // 0-255
        final float maxDist = TexSize/2-SunRadius;
        // Damit ein voelliges Ausblenden sichergestellt ist,
        // muss FallOffK kleiner sein als die (dist-SunRadius)'ste
        // Wurzel aus (CutAlpha / 255f)
        float maxFalloffK = (float) Math.pow(CutAlpha/255, 1d/maxDist);
        final float FalloffK = maxFalloffK;
        //assert FalloffK < maxFalloffK : "Sun glow attenuation factor too big!";
        for(int column = 0; column < TexSize; column++) {
            for(int row = 0; row < TexSize; row++) {
                int index = (row*TexSize + column);// * 4;
                float color = 0;
                float xdiff = column-TexSize/2;
                float ydiff = row-TexSize/2;
                float dist = (float) Math.sqrt(xdiff*xdiff + ydiff*ydiff);
                if(dist < SunRadius) {
                    color = 255f;
                } else {
                    color = (float) Math.pow(FalloffK, dist-SunRadius)*255f;
                }
                alphas[column][row] = color;
            }
        }
    }
    
    private void generateFlares(float[][] alphas) {
        float beginGrad = (float) 0*(180/NumRays);
        for(int i = 0; i < NumRays; i++) {
            float angle = beginGrad + 180/NumRays*i + 180/NumRays/2;
            float exp = lensflareShininess;
            exp += 2*(Math.random()-0.5)*LfShininessVariation;
            for(int column = 0; column < TexSize; column++) {
                for(int row = 0; row < TexSize; row++) {
                    int index = (row*TexSize + column) * 4;
                    int xdiff = column-TexSize/2;
                    int ydiff = row-TexSize/2;
                    if(xdiff == 0 && ydiff == 0) {
                        // ignore center pixel
                        continue;
                    }
                    float dist = (float) Math.sqrt(xdiff*xdiff+ydiff*ydiff);
                    if(dist < SunRadius) {
                        // skip since sun is located here
                        continue;
                    }
                    // angle between 12 o'clock and current ray direction
                    float phi = (float) MathExt.normDeg(Math.toDegrees(Math.asin(xdiff/dist)));
                    
                    if(ydiff > 0) {
                        // 4. Sektor behandeln (90 < phi < 180)
                        if(xdiff > 0) { phi = 180-phi; }
                        // 3. Sektor behandeln (180 < phi < 270)
                        else if(xdiff < 0) { phi = 270+270-phi; }
                    }
                    float diffangle = Math.abs(phi-angle);
                    // Abstand des Pixels zum Schnittpunkt einer Senkrechten
                    // mit dem Strahl; die Senkrechte verlaeuft durch das Pixel.
                    float abstandBreite = (float) Math.sin(Math.toRadians(diffangle))*dist;
                    if(Math.abs(abstandBreite) <= RayWidth) {
                        //float blockdist = (float)Math.abs(Math.cos(Math.toRadians(diffangle))*dist);
                        /*float alpha = 0;
                        if(dist < SunRadius) { alpha = 255f; }
                        else { alpha = (float)Math.pow(exp, dist-SunRadius)*255f; }*/
                        //color *= (RayWidth-Math.abs(breite))/RayWidth;
                        // Ausblenden der Laenge nach:
                        float alpha = (float) Math.pow(exp, dist-SunRadius)*255f;
                        // Ausblenden in der Breite:
                        alpha *= 1f-(Math.abs(abstandBreite)/RayWidth);
                        alphas[column][row] += alpha;
                    }
                }
            }
        }
    }
}
