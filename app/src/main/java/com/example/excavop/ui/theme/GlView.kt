package com.example.excavop.ui.theme
import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.text.style.ScaleXSpan
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GestureListener(private val renderer: MyGlRenderer) : GestureDetector.SimpleOnGestureListener() {
    private var previousX = 0.0f
    private var previousY = 0.0f

    override fun onDown(e: MotionEvent): Boolean {
        //Muss true zurueck geben, damit Events erkannt werden kann.
        return true
    }

    override fun onScroll(
        //https://developer.android.com/reference/android/view/GestureDetector.OnGestureListener
        e1: MotionEvent?, // e1 ? MotionEvent can be null
        e2: MotionEvent, // e2 cannot be null
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        //negativ fuer inverse Steuerung
        renderer.rotateCameraPosition(distanceX, distanceY)
        return true
    }
}

class GlView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGlRenderer
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector

    override fun onTouchEvent(event: MotionEvent) :  Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    init {
        setEGLContextClientVersion(3)
        renderer = MyGlRenderer()
        setRenderer(renderer)

        scaleGestureDetector = ScaleGestureDetector(context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val scaleFactor = detector.scaleFactor
                    renderer.handleZoom(scaleFactor)
                    return true
                }
            }
        )
        gestureDetector = GestureDetector(context, GestureListener(renderer))
    }
}
class MyGlRenderer : GLSurfaceView.Renderer {
    val prog = MeshProg()
    val vao = MeshVao()

    val projMatrix = FloatArray(16)
    val viewMatrix = FloatArray(16)

    private var cameraZoom = 8.0f
    private var phi = 0.0f
    private var theta = 90.0f
    private var time = 0.0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        enableBlend()
        prog.buildProgram()
        GLES32.glClearColor(1.0f, 0.0f, 0.0f, 0.0f)

        Matrix.perspectiveM(projMatrix, 0, 50.0f, 1200/2000f, 0.5f, 20.0f)
        prog.setProjMatrix(projMatrix)

        vao.buildVao()
    }
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES32.glViewport(0, 0, width, height)
    }
    override fun onDrawFrame(gl: GL10?) {
        GLES32.glDepthFunc(GLES32.GL_LESS)
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)

        GLES32.glClearColor(0.0f, 0.0f, 1.0f, 0.0f)

        updateCamera()

        time += 0.02f
        prog.setTime(time)

        prog.use()
        vao.render()

        GLES32.glUseProgram(0)
        GLES32.glBindVertexArray(0)
    }

    public fun handleZoom(scaleFactor: Float) {
        cameraZoom *= scaleFactor
        cameraZoom = cameraZoom.coerceIn(1f, 10f) // coerceIn(min, max), damit wir nicht unendlich weit rein- oder rauszoomen koennen
    }
    public fun rotateCameraPosition(dx: Float, dy: Float) {
        val sensitivity = 0.1f
        phi -= dx * sensitivity
        theta -= dy * sensitivity
        theta = theta.coerceIn(1f, 179f)
    }

    private fun updateCamera() {
        val phiRad = Math.toRadians(phi.toDouble())
        val thetaRad = Math.toRadians(theta.toDouble())

        val cameraEyePositionX = (cameraZoom * Math.sin(thetaRad) * Math.cos(phiRad)).toFloat()
        val cameraEyePositionY = (cameraZoom * Math.sin(thetaRad) * Math.sin(phiRad)).toFloat()
        val cameraEyePositionZ = (cameraZoom * Math.cos(thetaRad)).toFloat()

        Matrix.setLookAtM(viewMatrix, 0,
            cameraEyePositionX, cameraEyePositionY, cameraEyePositionZ,
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, -1.0f
        )
        prog.setViewMatrix(viewMatrix)
    }
    private fun enableBlend() {
        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
//          GLES32.glEnable(GLES32.GL_BLEND)
//          GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)
    }
}