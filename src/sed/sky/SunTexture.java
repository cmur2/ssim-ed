package sed.sky;

import java.nio.ByteBuffer;

import ssim.util.MathExt;

import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

public class SunTexture extends Texture2D {
    
    private static final int TexSize = 256;
    
    // simple
    private static final float SunRadius = 12.0f;
    
    // lensflare
    private static final int NumRays = 6;
    private static final float RayWidth = 1.0f;
    
    private float lensflareShininess;
    
    public SunTexture(float lensflareShininess) {
        this.lensflareShininess = lensflareShininess;
//        Image img = new Image(Image.Format.ABGR8, TexSize, TexSize, generateSimple());
        Image img = new Image(Image.Format.ABGR8, TexSize, TexSize, generateLensflare());
        setImage(img);
    }
    
    private ByteBuffer generateSimple() {
        float[][] alphas = new float[TexSize][TexSize];
        
        generateGlow(alphas);
        
        return generateByteBuffer(alphas);
    }
    
    private ByteBuffer generateLensflare() {
        float[][] alphas = new float[TexSize][TexSize];
        
        generateGlow(alphas);
        generateFlares(alphas);
        
        return generateByteBuffer(alphas);
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
        float beginGrad = (float)Math.random()*(180/NumRays);
        for(int i = 0; i < NumRays; i++) {
            float angle = beginGrad + 180/NumRays*i + 180/NumRays/2; //beginGrad+(180/maxStrahlen*i)+(float)Math.random()*(180/maxStrahlen);
            float exp = lensflareShininess; //0.92f+(float)Math.random()*0.05f;
            exp += (Math.random()-0.5)*0.03f;
            for(int column = 0; column < TexSize; column++) {
                for(int row = 0; row < TexSize; row++) {
                    int index = (row*TexSize + column) * 4;
                    int xdiff = column-TexSize/2;
                    int ydiff = row-TexSize/2;
                    if(xdiff == 0 && ydiff == 0) {
                        // Mittelpixel nicht beachten!
                        continue;
                    }
                    float dist = (float)Math.sqrt(xdiff*xdiff+ydiff*ydiff);
                    if(dist < SunRadius) {
                        // Sonne ueberdeckt diesen Bereich bereits vollstaendig
                        continue;
                    }
                    // Winkel zw. 12-Uhr und aktueller Strahlrichtung
                    float phi = (float)MathExt.normDeg(Math.toDegrees(Math.asin(xdiff/dist)));
                    
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
                        float alpha = (float)Math.pow(exp, dist-SunRadius)*255f;
                        // Ausblenden in der Breite:
                        alpha *= 1f-(Math.abs(abstandBreite)/RayWidth);
                        alphas[column][row] += alpha;
                    }
                }
            }
        }
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
}
