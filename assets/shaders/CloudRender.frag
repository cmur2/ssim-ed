
uniform float m_ImageSize;
uniform float m_CloudSharpness; // unneeded here, since alpha is outsourced
uniform float m_MaxSteps;
uniform vec3  m_SunPos;
uniform float m_WayFactor;
uniform vec3  m_SunLightColor;

//uniform float m_Compression;
//uniform float m_ClippingDistance;
//uniform float m_ClippingFactor;
//uniform float m_AlphaFactor;
//uniform float m_MinColor;

uniform sampler2D m_HeightField; // tex unit 0

varying vec4 varTexVoxelCoord;

float texLookup(vec2 texCoord) {
    return texture2D(m_HeightField, texCoord).r * 255.0;
}

void main() {
    //float malpha = texture2D(m_HeightField, varTexVoxelCoord.xy).b; 
    //gl_FragColor = vec4(malpha,malpha,malpha, 1.0);
    //return;
    //gl_FragColor = vec4(1.0,1.0,1.0, 1.0);
    //return;
    
    float alpha = texLookup(varTexVoxelCoord.xy);
    if(alpha == 0.0) {
        // early exit if no cloud at current texel
        // render final texel full translucent
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
        return;
    }
    vec4 v = vec4(varTexVoxelCoord.zw, -alpha, 0.0);
    float zdiff = 255.0 - v.z;
    vec3 vdir = normalize(m_SunPos - v.xyz);
    float numUnitVdirs = zdiff / vdir.z;
    vdir *= numUnitVdirs;
    //vec4 vadd =vec4(m_SunPos-v.xyz, 1.0);
    vec4 vadd = vec4(vdir, 1.0);
    vadd.xyz /= m_MaxSteps;
    float len = length(vadd.xyz);
    
    // convert to texture space
    v.xy /= m_ImageSize;
    vadd.xy /= m_ImageSize;

    float wayInClouds = 0.0;
    for(; v.w < m_MaxSteps; v += vadd) {
        float talpha = texLookup(v.xy);
        if(talpha != 0.0) {
            if(-talpha <= v.z && v.z <= talpha) {
                wayInClouds += len;
            }
        }
    }

    // ============================================
    // = many ways to get the "color"/attenuation =
    // ============================================
    //float lenSun = length(vec3(varTexVoxelCoord.zw, -alpha) - m_Sun);
    //wayInClouds = wayInClouds / lenSun;
    //vec3 color = pow(0.75, m_WayFactor * wayInClouds) * vec3(1.0,1.0,1.0);// * vec3(gl_LightSource[0].diffuse);

    float color = exp(-m_WayFactor * wayInClouds);
    //float color = exp(-0.001 * wayInClouds);

    //color = pow(2.0, -m_WayFactor * length(varTexVoxelCoord.zw - m_Sun.xy));

    //vec3 color = exp(- 0.04 * float(hits)) * vec3(1.0,1.0,1.0);// * vec3(gl_LightSource[0].diffuse);

    //color = clamp(color, 0.0, 1.0);

    // ==========================================
    // = the second component: sun lights color =
    // ==========================================
    //vec3 lig = gl_LightSource[0].diffuse.rgb;
    //vec3 lig = vec3(1.0,1.0,1.0);
    vec3 lig = m_SunLightColor;

    // ====================================
    // = determine the alpha (outsourced) =
    // ====================================
    alpha = 1.0 - pow(m_CloudSharpness, alpha);
    alpha *= texture2D(m_HeightField, varTexVoxelCoord.xy).r; // use fading information

    // ================
    // = final action =
    // ================
    gl_FragColor = vec4(color * lig, alpha);
    //gl_FragColor = vec4(color * lig, 1.0);
}
