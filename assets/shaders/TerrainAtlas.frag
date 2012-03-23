
#import "shaders/Fog.glsllib"

uniform vec4 g_LightColor;

uniform sampler2D m_TerrainLUT;
uniform sampler2D m_TerrainAtlas;
uniform sampler2D m_TerrainNoise;
uniform float m_InvMaxAltitude;
uniform vec3 m_AtlasParameters;
uniform vec2 m_NoiseParameters;

varying vec3 varNormal;
varying vec2 varTexCoord;
varying float varSlope;
varying float varZ;
varying vec4 varLightDir;

varying vec3 varFogCoord;

void main() {
    // unpacked noise in [-1,+1]
    float noise = texture2D(m_TerrainNoise, fract(varTexCoord*4.0)).r * 2.0 - 1.0;
    vec2 terrainTypeCoord = vec2(varSlope, varZ);
    // permutation of (slope,altitude) by noise:
    // weight permutation of varZ by varZ so higher altitudes are changed more
    terrainTypeCoord += noise * vec2(1.0, varZ) * m_NoiseParameters;
    // convert (slope,altitude) into tex coord
    terrainTypeCoord.x = cos(terrainTypeCoord.x);
    terrainTypeCoord.y = terrainTypeCoord.y * m_InvMaxAltitude * 0.5 + 0.5;
    // lookup ID
    int id = int(texture2D(m_TerrainLUT, terrainTypeCoord).r * 256.0);
    // 2d offset in atlas texture
    vec2 offset = vec2(mod(float(id), m_AtlasParameters.x), id / int(m_AtlasParameters.x));

    // always use the center of each tile, ignore varTexCoord:
    //vec2 texCoord = (offset + 0.5) * m_AtlasParameters.y;

    // use offset, add a border to prevent false wrapping/bilinear filtering and
    // shrink the amount to which varTexCoord is expanded to tile width-2*border
    vec2 texCoord =
        offset * m_AtlasParameters.y + m_AtlasParameters.z +
        fract(varTexCoord) * (m_AtlasParameters.y - 2.0 * m_AtlasParameters.z);

    vec4 material_diffuse = texture2D(m_TerrainAtlas, texCoord);

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
    applyFoggedColorByFragmentOnly(gl_FragColor, varFogCoord);
}