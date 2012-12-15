
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

varying vec3 varNormal;
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

float waveFalloff(float dist) {
	const float maxDist = 1000.0;
	return 1.0 - clamp(dist / maxDist, 0.0, 1.0);
}

void main() {
    varNormal = normalize(g_NormalMatrix * inNormal);

	vec4 mPosition = vec4(inPosition, 1.0);
	vec3 wPosition = (g_WorldMatrix * mPosition).xyz;
	wPosition.y = 0.0;

    float dist = waveFalloff(length(        g_CameraPosition - wPosition));
    //float dist = waveFalloff(length(vec3(-50.0, 50.0, +50.0) - wPosition));
    mPosition.y *= dist;

    gl_Position = g_WorldViewProjectionMatrix * mPosition;

    //-------------------------
    // general to all lighting
    //-------------------------
    vec4 wvLightPos = (g_ViewMatrix * vec4(g_LightPosition.xyz,clamp(g_LightColor.w,0.0,1.0)));
    wvLightPos.w = g_LightPosition.w;

    vec3 vPosition = (g_WorldViewMatrix * mPosition).xyz;
    varLightDir = lightComputeDir(vPosition, g_LightColor, wvLightPos);
}