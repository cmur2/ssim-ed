
uniform float m_ImageSize;
uniform float m_MaxSteps;
uniform float m_CloudSharpness;
uniform float m_WayFactor;
uniform vec3  m_SunPosition;
uniform vec4  m_SunLightColor;

uniform sampler2D m_HeightField;

varying vec4 varTexVoxelCoord;

float texLookup(vec2 texCoord) {
    return texture2D(m_HeightField, texCoord).r * 255.0;
}

void main() {
    float alpha = texLookup(varTexVoxelCoord.xy);
    if(alpha == 0.0) {
        // early exit if no cloud at current texel
        // render final texel full translucent
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
        return;
    }
    vec4 v = vec4(varTexVoxelCoord.zw, -alpha, 0.0);
    float zdiff = 255.0 - v.z;
    vec3 vdir = normalize(m_SunPosition - v.xyz);
    float numUnitVdirs = zdiff / vdir.z;
    vdir *= numUnitVdirs;
    //vec4 vadd = vec4(m_SunPosition-v.xyz, 1.0);
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

    // =========================================
    // = the second component: sun light color =
    // =========================================
    //vec3 light = vec3(1.0,1.0,1.0);
    vec3 light = m_SunLightColor.rgb;

    // =======================
    // = determine the alpha =
    // =======================
    alpha = 1.0 - pow(m_CloudSharpness, alpha);
    alpha *= texture2D(m_HeightField, varTexVoxelCoord.xy).r; // use fading information

    // ================
    // = final action =
    // ================
    gl_FragColor = vec4(color * light, alpha);
    //gl_FragColor = vec4(color * light, 1.0);
}
