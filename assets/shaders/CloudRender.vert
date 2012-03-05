
uniform mat4  g_WorldViewProjectionMatrix;
uniform float m_ImageSize;

attribute vec3 inPosition;
attribute vec2 inTexCoord;

varying vec4 varTexVoxelCoord;

void main() { 
    varTexVoxelCoord.xy = inTexCoord.st;
    varTexVoxelCoord.zw = varTexVoxelCoord.xy * m_ImageSize;
    
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0); 
}