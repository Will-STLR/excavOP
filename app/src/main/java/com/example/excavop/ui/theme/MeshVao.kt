package com.example.excavop.ui.theme
import android.opengl.GLES32
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder


class MeshVao: GlVao {
    private var vao: Int = 0
    private val vertices = floatArrayOf(
        //  x,     y,     r,    g,    b
        0.0f,  0.5f,  1.0f, 0.0f, 0.0f, 1.0f,  // oben (rot)
        -0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 1.0f,  // links unten (grün)
        0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 1.0f // rechts unten (blau)
    )

    override fun buildVao() {
        val vaoTemp = IntArray(1)
        GLES32.glGenVertexArrays(1, vaoTemp, 0)
        vao = vaoTemp[0]
        GLES32.glBindVertexArray(vao)

        val vboTemp = IntArray(1)
        GLES32.glGenBuffers(1, vboTemp, 0)
        val vbo = vboTemp[0]

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo)
        //first para: vertices, second: dim(3 for each vertex and 4 for color), third: sizeof(float)
        val tempBuffer = ByteBuffer.allocateDirect(3 * (2+4) * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        tempBuffer.put(vertices).position(0)

        //we need for each layout an attribpointer
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, 3 * (2+4) * 4, tempBuffer, GLES32.GL_STATIC_DRAW)
        GLES32.glVertexAttribPointer(0, 2, GLES32.GL_FLOAT, false, 6*4, 0)
        GLES32.glEnableVertexAttribArray(0)
        GLES32.glVertexAttribPointer(1, 4, GLES32.GL_FLOAT, false, 6*4, 8)
        GLES32.glEnableVertexAttribArray(1)

        GLES32.glBindVertexArray(0) //Finished with this VAO
    }

    override fun render() {
        GLES32.glBindVertexArray(vao)
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, 3)
        GLES32.glBindVertexArray(0)
    }
}
class MeshProg: GLProgram(vertexShaderCode, fragmentShaderCode) {
    companion object {
        private const val TAG = "MeshProg"
        private val vertexShaderCode = """
          #version 320 es
          layout(location = 0) in vec2 vPosition;
          layout(location = 1) in vec4 vColor;
           
          uniform mat4 uView;
          uniform mat4 uProjection;
          uniform float uTime; 
           
          out vec4 color;
            
          void main() { 
            vec2 pos = vPosition;
            pos.y += sin(pos.x * 10.0 + uTime) * 0.1;
            gl_Position = uProjection * uView * vec4(pos, 0.0f, 1.0f);
            
            float wave = sin(pos.x * 10.0 + uTime);
            float intensity = (wave + 1.0) * 0.5;
            color = vec4(intensity, 0.5, 1.0 - intensity, 1.0);
          }  
        """.trimIndent()
        private val fragmentShaderCode = """
           #version 320 es
           precision mediump float;
            
           out vec4 fragColor;
           in vec4 color;
          
           void main() {
            fragColor = color;
          }  
        """.trimIndent()
    }

    private var mView: Int = -1
    private var mProj: Int = -1
    private var mTime: Int = -1

    override fun buildProgram() {
        super.buildProgram()

        mView = GLES32.glGetUniformLocation(mProgram, "uView")
        mProj = GLES32.glGetUniformLocation(mProgram, "uProjection")
        mTime = GLES32.glGetUniformLocation(mProgram, "uTime")

        assert(mView != -1)
        assert(mProj != -1)
        assert(mTime != -1)
    }

    //Functions for Uniforms
    fun setViewMatrix(viewMat: FloatArray) {
        assert(viewMat.size == 16)
        val tempBufferView = ByteBuffer.allocateDirect(viewMat.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        tempBufferView.put(viewMat).position(0)
        GLES32.glProgramUniformMatrix4fv(mProgram, mView, 1, false, tempBufferView)
    }
    fun setProjMatrix(projMat: FloatArray) {
        assert(projMat.size == 16)
        val tempBufferProjection = ByteBuffer.allocateDirect(projMat.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        tempBufferProjection.put(projMat).position(0)
        GLES32.glProgramUniformMatrix4fv(mProgram, mProj, 1, false, tempBufferProjection)
    }
    fun setTime(time: Float) {
        GLES32.glProgramUniform1f(mProgram, mTime, time)
    }
}