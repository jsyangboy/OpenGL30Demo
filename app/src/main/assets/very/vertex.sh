#version 300 es
precision mediump float;
layout(location = 1) in vec4 aColor;
layout(location = 0) in vec4 aPosition;//location 是指定索引
out vec4 vColor;
void main(){
  vColor = aColor;
  gl_Position = aPosition;
}