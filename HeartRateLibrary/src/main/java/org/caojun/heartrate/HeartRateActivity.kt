package org.caojun.heartrate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.PowerManager
import android.view.SurfaceHolder
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_heartrate.*
import org.achartengine.ChartFactory
import org.achartengine.GraphicalView
import org.achartengine.model.XYMultipleSeriesDataset
import org.achartengine.model.XYSeries
import org.achartengine.renderer.XYMultipleSeriesRenderer
import org.achartengine.renderer.XYSeriesRenderer
import org.caojun.utils.AverageUtils
import org.caojun.utils.RandomUtils
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class HeartRateActivity: Activity() {

    private val timer = Timer()
    //Timer任务，与Timer配套使用
    private val task = object : TimerTask() {
        override fun run() {
            val message = Message()
            message.what = 1
            handler.sendMessage(message)
        }
    }
    private var gx: Int = 0
    private var j: Int = 0

    private var flag = 1.0
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            //刷新图表
            updateChart()
            super.handleMessage(msg)
        }
    }
    private var TITLE: String? = null
    private var series: XYSeries? = null
    private val mDataset = XYMultipleSeriesDataset()
    private var chart: GraphicalView? = null
    private val renderer = buildRenderer()
    private var addX = -1.0
    private var addY = 0.0
    private var xv = DoubleArray(300)
    private var yv = DoubleArray(300)
    private var hua = intArrayOf(9, 10, 11, 12, 13, 14, 13, 12, 11, 10, 9, 8, 7, 6, 7, 8, 9, 10, 11, 10, 10)

    private val processing = AtomicBoolean(false)
    //预览设置信息
    private var previewHolder: SurfaceHolder? = null
    //Android手机相机句柄
    private var camera: Camera? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var averageIndex = 0
    private val averageArraySize = 4
    private val averageArray = IntArray(averageArraySize)

    private var value = 0

    /**
     * 类型枚举
     */
    enum class TYPE {
        GREEN, RED
    }

    //设置默认类型
    private var currentType = TYPE.GREEN

    //获取当前类型
//    fun getCurrent(): TYPE {
//        return currentType
//    }

    //心跳下标值
    private var beatsIndex = 0
    //心跳数组的大小
    private val beatsArraySize = 3
    //心跳数组
    private val beatsArray = IntArray(beatsArraySize)
    //心跳脉冲
    private var beats = 0.0
    //开始时间
    private var startTime: Long = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heartrate)

        TITLE = getString(R.string.heartrate)
        series = XYSeries(TITLE)

        initConfig()
    }

    /**
     * 初始化配置
     */
    private fun initConfig() {

        //将点集添加到这个数据集中
        mDataset.addSeries(series)

        //设置好图表的样式
        setChartSettings(renderer, "X", "Y", 0.0, 300.0, 4.0, 16.0, Color.WHITE, Color.WHITE)

        //生成图表
        chart = ChartFactory.getLineChartView(this, mDataset, renderer)

        //将图表添加到布局中去
        llChart.addView(chart, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        timer.schedule(task, 1, 20)           //曲线
        //获取SurfaceView控件
//        preview = findViewById(R.id.id_preview) as SurfaceView
        previewHolder = surfaceView.holder
        previewHolder!!.addCallback(surfaceCallback)
        previewHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen")
    }

    //	曲线
    public override fun onDestroy() {
        //当结束程序时关掉Timer
        timer.cancel()
        super.onDestroy()
    }

    /**
     * 创建图表
     */
    private fun buildRenderer(): XYMultipleSeriesRenderer {
        val renderer = XYMultipleSeriesRenderer()

        //设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
        val r = XYSeriesRenderer()
        r.color = Color.RED
        r.lineWidth = 1f
        renderer.addSeriesRenderer(r)
        return renderer
    }

    /**
     * 设置图标的样式
     * @param renderer
     * @param xTitle：x标题
     * @param yTitle：y标题
     * @param xMin：x最小长度
     * @param xMax：x最大长度
     * @param yMin:y最小长度
     * @param yMax：y最大长度
     * @param axesColor：颜色
     * @param labelsColor：标签
     */
    private fun setChartSettings(renderer: XYMultipleSeriesRenderer, xTitle: String, yTitle: String,
                                   xMin: Double, xMax: Double, yMin: Double, yMax: Double, axesColor: Int, labelsColor: Int) {
        //有关对图表的渲染可参看api文档
        renderer.chartTitle = TITLE
        renderer.xTitle = xTitle
        renderer.yTitle = yTitle
        renderer.xAxisMin = xMin
        renderer.xAxisMax = xMax
        renderer.yAxisMin = yMin
        renderer.yAxisMax = yMax
        renderer.axesColor = axesColor
        renderer.labelsColor = labelsColor
        renderer.setShowGrid(true)
        renderer.gridColor = Color.GREEN
        renderer.xLabels = 20
        renderer.yLabels = 10
        renderer.xTitle = "Time"
        renderer.yTitle = "mmHg"
        renderer.setYLabelsAlign(Paint.Align.RIGHT)
        renderer.pointSize = 3f
        renderer.isShowLegend = false
    }

    /**
     * 更新图标信息
     */
    private fun updateChart() {
        //设置好下一个需要增加的节点
        if (flag == 1.0) {
            addY = 10.0
        } else {
            flag = 1.0
            if (gx < 200) {
                if (hua[20] > 1) {
//                    toast("请用您的指尖盖住摄像头镜头！")
                    tvInfo.text = getString(R.string.alert)
                    hua[20] = 0
                }
                hua[20]++
                return
            } else {
                hua[20] = 10
            }
            j = 0
        }
        if (j < 20) {
//            addY = hua[j].toDouble()
            addY = hua[j].toDouble() + RandomUtils.getRandom(-1, 1)
            j++
        }

        //移除数据集中旧的点集
        mDataset.removeSeries(series)

        //判断当前点集中到底有多少点，因为屏幕总共只能容纳100个，所以当点数超过100时，长度永远是100
        var length = series!!.itemCount
        var bz = 0

        if (length > 300) {
            length = 300
            bz = 1
        }
        addX = length.toDouble()
        //将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
        for (i in 0 until length) {
            xv[i] = series!!.getX(i) - bz
            yv[i] = series!!.getY(i)
        }

        //点集先清空，为了做成新的点集而准备
        series!!.clear()
        //将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
        //这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
        series!!.add(addX, addY)
        for (k in 0 until length) {
            series!!.add(xv[k], yv[k])
        }
        //在数据集中添加新的点集
        mDataset.addSeries(series)

        //视图更新，没有这一步，曲线不会呈现动态
        //如果在非UI主线程中，需要调用postInvalidate()，具体参考api
        chart!!.invalidate()
    } //曲线

    public override fun onResume() {
        super.onResume()
        wakeLock?.acquire()
        camera = Camera.open()
        startTime = System.currentTimeMillis()
    }

    public override fun onPause() {
        super.onPause()
        wakeLock?.release()
        camera?.setPreviewCallback(null)
        camera?.stopPreview()
        camera?.release()
        camera = null
    }


    /**
     * 相机预览方法
     * 这个方法中实现动态更新界面UI的功能，
     * 通过获取手机摄像头的参数来实时动态计算平均像素值、脉冲数，从而实时动态计算心率值。
     */
    private val previewCallback = Camera.PreviewCallback { data, cam ->

        val size = cam.parameters.previewSize ?: throw NullPointerException()
        if (!processing.compareAndSet(false, true)) {
            return@PreviewCallback
        }
        val width = size.width
        val height = size.height

        //图像处理
        val imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width)
        gx = imgAvg
//        mTV_Avg_Pixel_Values!!.text = "平均像素值是" + imgAvg.toString()

        if (imgAvg == 0 || imgAvg == 255) {
            processing.set(false)
            return@PreviewCallback
        }
        //计算平均值
        var averageArrayAvg = 0
        var averageArrayCnt = 0
        for (i in averageArray.indices) {
            if (averageArray[i] > 0) {
                averageArrayAvg += averageArray[i]
                averageArrayCnt++
            }
        }

        //计算平均值
        val rollingAverage = if (averageArrayCnt > 0) averageArrayAvg / averageArrayCnt else 0
        var newType = currentType

        if (imgAvg < rollingAverage) {
            newType = TYPE.RED
            if (newType != currentType) {
                beats++
                flag = 0.0
            }
        } else if (imgAvg > rollingAverage) {
            newType = TYPE.GREEN
        }

        if (averageIndex == averageArraySize) {
            averageIndex = 0
        }
        averageArray[averageIndex] = imgAvg
        averageIndex++

        if (newType != currentType) {
            currentType = newType
        }

        //获取系统结束时间（ms）
        val endTime = System.currentTimeMillis()
        val totalTimeInSecs = (endTime - startTime) / 1000.0
        if (totalTimeInSecs >= 2) {
            val bps = beats / totalTimeInSecs
            val dpm = (bps * 60.0).toInt()
            if (dpm < 30 || dpm > 180 || imgAvg < 200) {
                //获取系统开始时间（ms）
                startTime = System.currentTimeMillis()
                //beats心跳总数
                beats = 0.0
                processing.set(false)
                return@PreviewCallback
            }

            if (beatsIndex == beatsArraySize) {
                beatsIndex = 0
            }
            beatsArray[beatsIndex] = dpm
            beatsIndex++

            var beatsArrayAvg = 0
            var beatsArrayCnt = 0
            for (i in beatsArray.indices) {
                if (beatsArray[i] > 0) {
                    beatsArrayAvg += beatsArray[i]
                    beatsArrayCnt++
                }
            }
            val beatsAvg = beatsArrayAvg / beatsArrayCnt
            tvInfo.text = getString(R.string.heart_rate, beatsAvg.toString(), value.toString())
                    value = AverageUtils.add(beatsAvg).toInt()
            //获取系统时间（ms）
            startTime = System.currentTimeMillis()
            beats = 0.0
        }
        processing.set(false)
    }

    /**
     * 预览回调接口
     */
    private val surfaceCallback = object : SurfaceHolder.Callback {
        //创建时调用
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                val parameters = camera!!.parameters
                if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    parameters.set("orientation", "portrait") //
                    // parameters.set("rotation", 90); // 镜头角度转90度（默认摄像头是横拍）
                    camera!!.setDisplayOrientation(90) // 在2.2以上可以使用
                } else {
                    // 如果是横屏
                    parameters.set("orientation", "landscape") //
                    camera!!.setDisplayOrientation(0) // 在2.2以上可以使用
                }
                camera!!.setPreviewDisplay(previewHolder)
                camera!!.setPreviewCallback(previewCallback)
            } catch (t: Throwable) {
            }

        }

        //当预览改变的时候回调此方法
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            val parameters = camera!!.parameters
            parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            val size = getSmallestPreviewSize(width, height, parameters)
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height)
            }
            camera!!.parameters = parameters
            camera!!.startPreview()
        }

        //销毁的时候调用
        override fun surfaceDestroyed(holder: SurfaceHolder) {

        }
    }

    /**
     * 获取相机最小的预览尺寸
     */
    private fun getSmallestPreviewSize(width: Int, height: Int, parameters: Camera.Parameters): Camera.Size? {
        var result: Camera.Size? = null
        for (size in parameters.supportedPreviewSizes) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size
                } else {
                    val resultArea = result.width * result.height
                    val newArea = size.width * size.height
                    if (newArea < resultArea) {
                        result = size
                    }
                }
            }
        }
        return result
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("data", value)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}