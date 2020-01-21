package org.caojun.opengl

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.util.AttributeSet
import com.chillingvan.canvasgl.ICanvasGL
import com.chillingvan.canvasgl.glcanvas.GLPaint
import com.chillingvan.canvasgl.glview.GLView
import org.jetbrains.anko.doAsync
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.cos

class ParticleWaveView(context: Context, attrs: AttributeSet) : GLView(context, attrs) {

    companion object {

//        private const val TAU = Math.PI * 2
        private const val ParticleCount = 10000
        private const val MIN = 1
        private const val ADD = 5
        private const val SpreadX = 5
        private const val WaveControl = 10
        private const val WaitCount = 500
        private val WaveColor = arrayOf(0x47, 0x7C, 0xC2)
    }

    private val particle = DoubleArray(ParticleCount)
    private var particleWaveWalker = 0.0
//    private var randomWalker = 0.0
    private val points = ArrayList<P>()
    private var countPaint = 0

    init {
        for (i in particle.indices) {
            particle[i] = Math.random()
            points.add(P(0F, 0F, 0F, 0))
        }
    }

    private fun renderParticle() {
//        randomWalker += (Math.random() - 0.5) * 0.1
        particleWaveWalker += 0.03

        val midY = height / 2
        val midX = width / 2

        var spreadZ: Double
        var modZ: Double
        var addX: Double
        var addY: Double

        var xIndex: Int
        var zIndex: Int
        var i = 0

        while (i < particle.size) {

            xIndex = i
            zIndex = i + 1

            particle[zIndex] += 0.003

            if (particle[zIndex] > 1) {
                particle[zIndex] = 0.0
                particle[xIndex] = Math.random()
            }

            if (particle[zIndex] < 0.3) {
                continue
            }

            modZ = particle[zIndex].pow(2)
            spreadZ = 1 + (SpreadX - 1) * modZ

            addX = (0.5 - particle[xIndex]) * width * spreadZ
            addY = midY * modZ * (1 + 3 / WaveControl)

            val p = points[i]
            p.x = (midX + addX).toFloat()
            p.y = (midY + addY).toFloat()
            p.r = (MIN + modZ * ADD).toFloat()

            p.y += (sin(particle[xIndex] * 50 + particleWaveWalker) * addY / WaveControl).toFloat()
            p.y += (cos(particle[zIndex] * 10 + particleWaveWalker) * addY / WaveControl).toFloat()

            p.y -= (cos(particle[zIndex] + particle[xIndex] * 10 + particleWaveWalker) * addY / WaveControl).toFloat()

            p.y -= (cos(particle[xIndex] * 50 + particleWaveWalker) * addY / WaveControl).toFloat()
            p.y -= (sin(particle[zIndex] * 10 + particleWaveWalker) * addY / WaveControl).toFloat()

            val point = points[i]
            val per = point.y / height
            val colorR = (WaveColor[0] * per).toInt()
            val colorG = (WaveColor[1] * per).toInt()
            val colorB = (WaveColor[2] * per).toInt()
            p.color = Color.rgb(colorR, colorG, colorB)

            if (p.x < 0 || p.x > width) {
                continue
            }

            i += 2
        }
    }

    private class P(x: Float, y: Float, var r: Float, var color: Int) : PointF(x, y)
    private var countMeasure = 0
    private var isInitialized = false
//    private val paint = Paint()
    private val glPaint = GLPaint()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        countMeasure ++
        if (countMeasure > 1 && !isInitialized && measuredWidth > 0 && measuredHeight > 0) {

            doAsync {
                countPaint = 0
                while (!(context as Activity).isFinishing) {
                    synchronized(this@ParticleWaveView) {
                        renderParticle()
                        countPaint ++
                        if (countPaint < 0) {
                            countPaint = WaitCount + 1
                        }
//                        invalidate()
                        if (countPaint > WaitCount) {
                            restart()
                        }
                    }
                    Thread.sleep(1)
                }
            }
            isInitialized = true
        }
    }

    override fun onGLDraw(canvas: ICanvasGL?) {
//        glPaint.color = Color.BLACK
//        canvas?.drawRect(0F, 0F, width.toFloat(), height.toFloat(), glPaint)

        if (!isInitialized || countPaint <= WaitCount) {
            return
        }

        synchronized(this@ParticleWaveView) {
            for (i in points.indices) {
                val point = points[i]
                glPaint.color = point.color
                canvas?.drawCircle(point.x, point.y, point.r, glPaint)
            }
        }
    }
}