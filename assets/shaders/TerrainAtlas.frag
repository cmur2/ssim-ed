
uniform vec4 g_LightColor;

uniform sampler2D m_TextureTable;
uniform sampler2D m_TextureAtlas;
uniform float m_InvMaxAltitude;
uniform vec3 m_AtlasParameters;

varying vec3 varNormal;
varying vec2 varTexCoord;
varying float varSlope;
varying float varZ;
varying vec4 varLightDir;

void main() {
    vec2 terrainTypeCoord = vec2(varSlope, varZ * m_InvMaxAltitude * 0.5 + 0.5);
    int id = int(texture2D(m_TextureTable, terrainTypeCoord).r * 256.0);

    vec2 offset = vec2(mod(float(id), m_AtlasParameters.x), id / int(m_AtlasParameters.x));

    // always use the center of each tile, ignore varTexCoord:
    //vec2 texCoord = (offset + 0.5) * m_AtlasParameters.y;

    // use offset, add a border to prevent false wrapping/bilinear filtering and
    // shrink the amount to which varTexCoord is expanded to tile width-2*border
    vec2 texCoord =
        offset * m_AtlasParameters.y + m_AtlasParameters.z +
        fract(varTexCoord) * (m_AtlasParameters.y - 2.0 * m_AtlasParameters.z);

    vec4 material_diffuse = texture2D(m_TextureAtlas, texCoord);

    vec3 vNormal = normalize(varNormal);

    //-----------------------
    // lighting calculations
    //-----------------------
    //vec4 diffuse = g_LightColor * max(0.0, dot(vNormal, normalize(varLightDir.xyz))) * varLightDir.w;
    vec4 diffuse = vec4(1.0,1.0,1.0,1.0) * max(0.0, dot(vNormal, normalize(varLightDir.xyz))) * varLightDir.w;

    //--------------------------
    // final color calculations
    //--------------------------
    gl_FragColor = material_diffuse * diffuse;
}