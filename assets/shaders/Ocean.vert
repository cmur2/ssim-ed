
#define VERT
#import "shaders/Fog.glsllib"

uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrix;
uniform mat3 g_NormalMatrix;
uniform mat4 g_ViewMatrix;
uniform mat4 g_WorldMatrix;

uniform vec4 g_LightColor;
uniform vec4 g_LightPosition;

uniform vec3 g_CameraPosition;

attribute vec3 inPosition;
attribute vec3 inNormal;
attribute vec2 inTexCoord;
attribute vec3 inTangent;

varying vec3 varNormal; // view coords
varying vec3 varVertex; // view coords
varying vec4 varLightDir; // view coords
varying vec2 varTexCoord;

// this matrix has meaning of "surface-2-object"
varying mat3 varTBN;

// JME3 lights in world space
vec4 lightComputeDir(in vec3 worldPos, in vec4 color, in vec4 position) {
    float posLight = step(0.5, color.w);
    vec3 tempVec = position.xyz * sign(posLight - 0.5) - (worldPos * posLight);
    float dist = length(tempVec);
    vec4 lightDir;
    lightDir.w = clamp(1.0 - position.w * dist * posLight, 0.0, 1.0);
    lightDir.xyz = tempVec / vec3(dist);
    return lightDir;
}

float waveFalloff(float dist) {
    const float maxDist = 1000.0;
    return 1.0 - clamp(dist / maxDist, 0.0, 1.0);
}

void main() {
    varTexCoord = inTexCoord;

    vec3 n = normalize(g_NormalMatrix * vec3(0.0, 1.0, 0.0));
    vec3 t = normalize(g_NormalMatrix * vec3(1.0, 0.0, 0.0));
    vec3 b = cross(n, t);

    // this matrix translates from tangent to eye space
    // because n and t (and so b) are given in eye space from above
    varTBN = mat3(t,b,n); // column major

    // unused code path, varNormal only read if bump mapping disabled
    varNormal = g_NormalMatrix * inNormal;

    vec4 mPosition = vec4(inPosition, 1.0);
    vec3 wPosition = (g_WorldMatrix * mPosition).xyz;
    wPosition.y = 0.0;

    float dist = waveFalloff(length(g_CameraPosition - wPosition));
    //float dist = waveFalloff(length(vec3(-50.0, 50.0, +50.0) - wPosition));
    mPosition.y *= dist;

    gl_Position = g_WorldViewProjectionMatrix * mPosition;
    varVertex = (g_WorldViewMatrix * mPosition).xyz;

    //-------------------------
    // general to all lighting
    //-------------------------
    vec4 wvLightPos = (g_ViewMatrix * vec4(g_LightPosition.xyz,clamp(g_LightColor.w,0.0,1.0)));
    wvLightPos.w = g_LightPosition.w;

    vec3 vPosition = (g_WorldViewMatrix * mPosition).xyz;
    varLightDir = lightComputeDir(vPosition, g_LightColor, wvLightPos); // from vertex to light (view coords)

    setFogCoord(vPosition);
}