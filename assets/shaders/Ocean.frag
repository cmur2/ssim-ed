
uniform vec4 g_LightColor;

uniform vec4 m_Diffuse;

varying vec3 varNormal;
varying vec4 varLightDir;

void main() {

    vec4 material_diffuse = m_Diffuse;

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