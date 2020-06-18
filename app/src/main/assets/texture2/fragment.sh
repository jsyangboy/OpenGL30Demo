#version 300 es
precision mediump float;
uniform sampler2D uTexture;
in vec2 vTextCoord;
out vec4 vColor;
void main(){
  vColor = texture(uTexture,vTextCoord);
}