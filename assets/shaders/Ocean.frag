
#define FRAG
#import "shaders/Fog.glsllib"

uniform mat4 g_ViewMatrix;
uniform mat4 g_ViewMatrixInverse;

uniform vec4 g_LightColor;

uniform vec3 g_CameraPosition;

uniform vec4 m_WaterColor;
uniform float m_R0;
uniform float m_Shininess;
uniform float m_ShininessFactor;
uniform samplerCube m_SkyBox;
uniform sampler2D m_NormalMap;

varying vec3 varNormal; // view coords
varying vec3 varVertex; // view coords
varying vec4 varLightDir; // view coords
varying vec2 varTexCoord;

// this matrix has meaning of "surface-2-object"
varying mat3 varTBN;

//const float etaratio = 1.0003/1.3333; // ^= firstIndex/secondIndex
//const float r0 = pow((1.0-etaratio) / (1.0+etaratio), 2.0);

float Rapprox(float cosTheta) {
    //float r0 = pow((1.0-etaratio) / (1.0+etaratio), 2.0);
    return mix(pow(1.0 - cosTheta, 5.0), 1.0, m_R0);
}

void main() {
    // get the normal in tangent space from texture:
    vec3 vNormal = normalize(texture2D(m_NormalMap, varTexCoord).rgb * 2.0 - 1.0);
    vNormal = normalize(varTBN * vNormal);
    //vec3 vNormal = normalize(varNormal);
    
    vec3 pEye = vec3(0.0, 0.0, 0.0);
    vec3 vView = normalize(varVertex - pEye); // from camera to vertex (view coords)
    vec3 vReflect = reflect(vView, vNormal); // from vertex to sky (view coords)

    float fresnelReflectance = Rapprox(dot(-vView, vNormal));
    fresnelReflectance = clamp(fresnelReflectance, 0.0, 1.0);

    //-----------------------
    // lighting calculations
    //-----------------------
    //vec4 diffuse = g_LightColor * max(0.0, dot(vNormal, normalize(varLightDir.xyz))) * varLightDir.w;
    //vec4 diffuse = vec4(1.0,1.0,1.0,1.0) * max(0.0, dot(vNormal, normalize(varLightDir.xyz))) * varLightDir.w;

    // cheap reflection mapping by only using the sky box texture
    vec4 cSky = textureCube(m_SkyBox, (g_ViewMatrixInverse * vec4(vReflect, 0.0)).xyz);

    vec4 cRefract = m_WaterColor;
    vec4 cReflect = cSky;

    // fresnel mix of refraction/reflection color
    vec4 cFinal = mix(cRefract, cReflect, fresnelReflectance);

    vec4 cSpecular = vec4(0.0);
    float NdotL = max(0.0, dot(vNormal, normalize(varLightDir.xyz)));
    if(NdotL > 0.0) {
        vec3 vHalf = normalize(-vView + varLightDir.xyz);
        float pf = pow(max(dot(vNormal,vHalf), 0.0), m_Shininess);
        cSpecular = vec4(pf);
    }

    cFinal = clamp(cFinal + cSpecular * m_ShininessFactor, 0.0, 1.0);

    gl_FragColor = cFinal;
    //gl_FragColor = vec4(varTexCoord/13.0, 0.0, 1.0);
    applyFoggedColorByFragmentOnly(gl_FragColor);
}