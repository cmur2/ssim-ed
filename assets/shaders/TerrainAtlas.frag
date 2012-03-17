
uniform vec4 g_LightColor;

uniform sampler2D m_TextureTable;
uniform sampler2D m_TextureAtlas;
uniform float m_InvMaxAltitude;

varying vec3 varNormal;
varying vec2 varTexCoord;
varying float varSlope;
varying float varZ;
varying vec4 varLightDir;

void main() {
    vec2 terrainTypeCoord = vec2(varSlope, varZ * m_InvMaxAltitude * 0.5 + 0.5);
    int id = int(texture2D(m_TextureTable, terrainTypeCoord).r * 256.0);

    //const int nbTiles = int(1.0 / 0.25);
    vec2 offset = vec2(mod(float(id), 4.0), id / 4);

    //vec2 texCoord = offset * 0.25 + 0.125;
    vec2 texCoord = offset * 0.25 + 0.00 + fract(varTexCoord) * 0.25;

    //mipmapLevel(texCoord, vec2(64.0));

    //vec4 material_diffuse = vec4(0.1,0.1,0.1,1.0);
    vec4 material_diffuse = texture2D(m_TextureAtlas, texCoord);
    //float x = float(id)/16.0;
    //vec4 material_diffuse = vec4(x,0.0,0.0,1.0);
    //if(offset.x == 2.0) { material_diffuse.r = 1.0; }
    //material_diffuse.r = offset.x;

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