package com.example.excavop.ui.theme

import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CubeEdgesVao: GlVao {
    private var vao: Int = 0
    private var cubeEdges = floatArrayOf(
        0f, 0f, 0f,   0f, 0f, 0f, 1f,
        1f, 0f, 0f,   0f, 0f, 0f, 1f,

        1f, 0f, 0f,   0f, 0f, 0f, 1f,
        1f, 1f, 0f,   0f, 0f, 0f, 1f,

        1f, 1f, 0f,   0f, 0f, 0f, 1f,
        0f, 1f, 0f,   0f, 0f, 0f, 1f,

        0f, 1f, 0f,   0f, 0f, 0f, 1f,
        0f, 0f, 0f,   0f, 0f, 0f, 1f,

        // Rückseite
        0f, 0f, 1f,   0f, 0f, 0f, 1f,
        1f, 0f, 1f,   0f, 0f, 0f, 1f,

        1f, 0f, 1f,   0f, 0f, 0f, 1f,
        1f, 1f, 1f,   0f, 0f, 0f, 1f,

        1f, 1f, 1f,   0f, 0f, 0f, 1f,
        0f, 1f, 1f,   0f, 0f, 0f, 1f,

        0f, 1f, 1f,   0f, 0f, 0f, 1f,
        0f, 0f, 1f,   0f, 0f, 0f, 1f,

        // Verbindungen Vorder- & Rückseite
        0f, 0f, 0f,   1f, 1f, 1f, 1f,
        0f, 0f, 1f,   1f, 1f, 1f, 1f,

        1f, 0f, 0f,   1f, 1f, 1f, 1f,
        1f, 0f, 1f,   1f, 1f, 1f, 1f,

        1f, 1f, 0f,   1f, 1f, 1f, 1f,
        1f, 1f, 1f,   1f, 1f, 1f, 1f,

        0f, 1f, 0f,   1f, 1f, 1f, 1f,
        0f, 1f, 1f,   1f, 1f, 1f, 1f,
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
        val tempBuffer = ByteBuffer.allocateDirect(24 * (3+4) * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        tempBuffer.put(cubeEdges).position(0)

        //we need for each layout an attribpointer
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, 24 * (3+4) * 4, tempBuffer, GLES32.GL_STATIC_DRAW)
        GLES32.glVertexAttribPointer(0, 3, GLES32.GL_FLOAT, false, 7*4, 0)
        GLES32.glEnableVertexAttribArray(0)
        GLES32.glVertexAttribPointer(1, 4, GLES32.GL_FLOAT, false, 7*4, 12)
        GLES32.glEnableVertexAttribArray(1)

        GLES32.glBindVertexArray(0) //Finished with this VAO
    }

    override fun render() {
        GLES32.glBindVertexArray(vao)
        GLES32.glDrawArrays(GLES32.GL_LINES, 0, 24)
        GLES32.glBindVertexArray(0)
    }
}
class EdgeProg: GLProgram(edgeVertexShaderCode, edgeFragmentShaderCode) {
    private var mView: Int = -1
    private var mProj: Int = -1

    companion object {
        private const val TAG = "EdgeProg"
        private val edgeVertexShaderCode = """
             #version 320 es
             layout(location = 0) in vec3 vPosition;
             
             uniform mat4 uView;
             uniform mat4 uProjection;
             
             void main() {
                gl_Position = uProjection * uView * vec4(vPosition, 1.0); 
             }
        """.trimIndent()
        private val edgeFragmentShaderCode = """
             #version 320 es
             precision mediump float;
             
             out vec4 fragColor;
             
             void main() {
                fragColor = vec4(1.0); 
             }
        """.trimIndent()
    }

    override fun buildProgram() {
        super.buildProgram()
        mView = GLES32.glGetUniformLocation(mProgram, "uView")
        mProj = GLES32.glGetUniformLocation(mProgram, "uProjection")
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
}
