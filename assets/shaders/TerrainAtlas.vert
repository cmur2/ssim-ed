
uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrix;
uniform mat3 g_NormalMatrix;
uniform mat4 g_ViewMatrix;
uniform mat4 g_WorldMatrix;

uniform vec4 g_LightColor;
uniform vec4 g_LightPosition;

attribute vec3 inPosition;
attribute vec3 inNormal;
attribute vec2 inTexCoord;

varying vec3 varNormal;
varying vec2 varTexCoord;
varying float varSlope;
varying float varZ;
varying vec4 varLightDir;

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

void main() {
    varNormal = normalize(g_NormalMatrix * inNormal);
    varTexCoord = inTexCoord;
    varSlope = acos(dot(inNormal, vec3(0.0,1.0,0.0)));
    
    vec4 pos = vec4(inPosition, 1.0);
    varZ = (g_WorldMatrix * pos).y;
    gl_Position = g_WorldViewProjectionMatrix * pos;

    //-------------------------
    // general to all lighting
    //-------------------------
    vec4 wvLightPos = (g_ViewMatrix * vec4(g_LightPosition.xyz,clamp(g_LightColor.w,0.0,1.0)));
    wvLightPos.w = g_LightPosition.w;

    vec3 vPosition = (g_WorldViewMatrix * pos).xyz;
    varLightDir = lightComputeDir(vPosition, g_LightColor, wvLightPos);
}