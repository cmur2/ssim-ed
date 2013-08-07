
uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_ViewMatrix;
uniform mat4 g_ProjectionMatrix;
uniform mat4 g_WorldMatrix;

attribute vec3 inPosition;

varying vec3 varVertex; // in body (world) coords.

void main() {
    varVertex = vec3(inPosition);
    
    // old solution does not work correctly for mirroring
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
    
    // automatically center in eye position, needed for correct
    // reflection mapping where eye is mirrored at y=0
    //gl_Position = g_ProjectionMatrix * vec4((g_ViewMatrix * vec4(inPosition, 0.0)).xyz, 1.0);
}