
uniform vec4 g_LightColor;

uniform sampler2D m_TextureTable;
uniform float m_InvMaxAltitude;

varying vec3 varNormal;
varying float varSlope;
varying float varZ;
varying vec4 varLightDir;

void main() {
    vec2 texCoord = vec2(varSlope, varZ * m_InvMaxAltitude * 0.5 + 0.5);
    // lookup index in m_TextureTable and use it directly as color
    vec4 material_diffuse = vec4(texture2D(m_TextureTable, texCoord).rgb, 1.0);

    vec3 vNormal = normalize(varNormal);

    //-----------------------
    // lighting calculations
    //-----------------------
    vec4 diffuse = g_LightColor * max(0.0, dot(vNormal, normalize(varLightDir.xyz))) * varLightDir.w;

    //--------------------------
    // final color calculations
    //--------------------------
    float x = varSlope;
    gl_FragColor = material_diffuse;
}