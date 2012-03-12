
uniform vec4 g_LightColor;

uniform sampler2D m_HeightGradient;
uniform float m_InvHeightGradientTexWidth;
uniform float m_InvMeterPerTexel;

varying vec3 varNormal;
varying float varZ;
varying vec4 varLightDir;

void main() {
    float height = 0.25 + varZ * m_InvMeterPerTexel * m_InvHeightGradientTexWidth;
    vec2 texCoord = vec2(height, 0.5);
    vec4 material_diffuse = vec4(texture2D(m_HeightGradient, texCoord).rgb, 1.0);

    vec3 vNormal = normalize(varNormal);

    //-----------------------
    // lighting calculations
    //-----------------------
    vec4 diffuse = g_LightColor * max(0.0, dot(vNormal, normalize(varLightDir.xyz))) * varLightDir.w;

    //--------------------------
    // final color calculations
    //--------------------------
    gl_FragColor = diffuse * material_diffuse;
}