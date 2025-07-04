package com.petterp.floatingx.view.helper

import android.content.res.Configuration
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.petterp.floatingx.assist.FxAdsorbDirection
import com.petterp.floatingx.assist.FxBoundaryConfig
import com.petterp.floatingx.assist.FxGravity
import com.petterp.floatingx.util.coerceInFx
import com.petterp.floatingx.util.shr
import com.petterp.floatingx.view.FxBasicContainerView

/**
 * 浮窗坐标的配置助手，用于处理坐标相关的处理
 * @author petterp
 */
class FxViewLocationHelper : FxViewBasicHelper(), View.OnLayoutChangeListener {
    private var viewW = 0f
    private var viewH = 0f
    private var parentW = 0f
    private var parentH = 0f
    private var screenWidthDp = 0
    private var screenHeightDp = 0

    private var isInitLocation = true
    private var restoreTopStandard = false
    private var restoreLeftStandard = false
    private var needUpdateConfig: Boolean = false
    private var needUpdateLocation: Boolean = false
    private val moveBoundary = FxBoundaryConfig()
    private val moveIngBoundary = FxBoundaryConfig()

    private var orientation = Configuration.ORIENTATION_PORTRAIT


    private val x: Float
        get() = basicView?.currentX() ?: 0f
    private val y: Float
        get() = basicView?.currentY() ?: 0f

    private val Pair<Float, Float>.safeLocationXY: Pair<Float, Float>
        get() {
            val offX = config.offsetX
            val offY = config.offsetY
            return first + offX to second + offY
        }


    override fun initConfig(parentView: FxBasicContainerView) {
        super.initConfig(parentView)
        parentView.addOnLayoutChangeListener(this)
        parentView.resources.configuration.apply {
            this@FxViewLocationHelper.orientation = orientation
            this@FxViewLocationHelper.screenWidthDp = screenWidthDp
            this@FxViewLocationHelper.screenHeightDp = screenHeightDp
        }
    }



    override fun onInit() {
        // 先刷新一下view大小，避免有时候sizeChanged没测量
        updateViewSize()
        val hasHistory = config.enableSaveDirection && config.iFxConfigStorage?.hasConfig() == true
        val locationFrom: String
        val (defaultX, defaultY) = if (hasHistory) {
            locationFrom = "history_location"
            getHistoryXY()
        } else if (config.hasDefaultXY) {
            locationFrom = "user_init_location"
            config.defaultX to config.defaultY
        } else {
            locationFrom = "default_location"
            getDefaultXY(parentW, parentH, viewW, viewH)
        }
        // 判断坐标应该准确在哪里
        val (safeX, safeY) = safeX(defaultX) to safeY(defaultY)
        basicView?.updateXY(safeX, safeY)
        if (config.enableEdgeAdsorption){
            config?.iFxTouchListener?.OnDragEnd(defaultX, defaultY,
                basicView?.isNearestTop(defaultY) == true, basicView?.isNearestLeft(defaultX) == true
            ,true)
        }
        isInitLocation = false
        config.fxLog.d("fxView -> initLocation: x:$safeX,y:$safeY,way:[$locationFrom]")

        basicView?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->

                    val isImmersiveMode = insets.isVisible(WindowInsetsCompat.Type.navigationBars()).not()
                if (basicView?.isNavigationBarHidden==null||basicView?.isNavigationBarHidden!= isImmersiveMode){
                    Log.d("WindowInsets", "Status Bar isImmersiveMode: $isImmersiveMode")
                    basicView?.isNavigationBarHidden=isImmersiveMode
                    updateViewSize()
                    checkOrRestoreLocation()
                }
                insets
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        config.fxLog.d("fxView -> onMeasure: widthMeasureSpec:$widthMeasureSpec,heightMeasureSpec:$heightMeasureSpec")
    }
    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        updateViewSize()
        // 初始化跳过
        if (isInitLocation) return
        if (needUpdateLocation) {
            checkOrRestoreLocation()
        } else {
            basicView?.internalMoveToXY(safeX(x), safeY(y))
        }
    }

    override fun onConfigurationChanged(config: Configuration) {
        if (config.orientation != orientation || config.screenWidthDp != screenWidthDp || config.screenHeightDp != screenHeightDp) {
            orientation = config.orientation
            screenWidthDp = config.screenWidthDp
            screenHeightDp = config.screenHeightDp
            restoreLeftStandard = isNearestLeft(x)
            restoreTopStandard = isNearestTop(y)
            this.needUpdateLocation = true
            this.needUpdateConfig = true
            this.config.fxLog.d("fxView -> onConfigurationChanged:[screenChanged:${this.needUpdateLocation}]")
        }
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        if (!needUpdateLocation) return
        updateViewSize()
        checkOrRestoreLocation()
    }

    fun needUpdateLocation() {
        this.needUpdateLocation = true
    }

    fun getDefaultEdgeXY(x: Float = this.x, y: Float = this.y): Pair<Float, Float>? {
        return if (config.enableEdgeAdsorption || config.enableHalfHide) {
            getAdsorbDirectionLocation(isNearestLeft(x), isNearestTop(y))
        } else if (config.enableEdgeRebound) {
            x to y
        } else {
            null
        }
    }

    fun safeX(x: Float, isMoveIng: Boolean = false): Float {
        // 是否考虑边界
        val enableBound = isMoveIng && config.enableEdgeRebound
        val minW = if (enableBound) moveIngBoundary.minW else moveBoundary.minW
        val maxW = if (enableBound) moveIngBoundary.maxW else moveBoundary.maxW
        return x.coerceInFx(minW, maxW)
    }

    fun safeY(y: Float, isMoveIng: Boolean = false): Float {
        val enableBound = isMoveIng && config.enableEdgeRebound
        val minH = if (enableBound) moveIngBoundary.minH else moveBoundary.minH
        val maxH = if (enableBound) moveIngBoundary.maxH else moveBoundary.maxH
        return y.coerceInFx(minH, maxH)
    }

    fun checkOrSaveLocation(x: Float, y: Float) {
        if (config.iFxConfigStorage == null || !config.enableSaveDirection) return
        config.iFxConfigStorage!!.update(x, y)
        config.fxLog.d("saveLocation -> x:$x,y:$y")
    }

    private fun getAdsorbDirectionLocation(
        isNearestLeft: Boolean,
        isNearestTop: Boolean
    ): Pair<Float, Float> {
        return when (config.adsorbDirection) {
            FxAdsorbDirection.LEFT -> {
                val moveX = moveBoundary.minW
                val moveY = safeY(y)
                moveX to moveY
            }

            FxAdsorbDirection.RIGHT -> {
                val moveX = moveBoundary.maxW
                val moveY = safeY(y)
                moveX to moveY
            }

            FxAdsorbDirection.LEFT_OR_RIGHT -> {
                val moveX = if (isNearestLeft) moveBoundary.minW else moveBoundary.maxW
                val moveY = safeY(y)
                moveX to moveY
            }

            FxAdsorbDirection.TOP -> {
                val moveX = safeX(x)
                val moveY = moveBoundary.minH
                moveX to moveY
            }

            FxAdsorbDirection.BOTTOM -> {
                val moveX = safeX(x)
                val moveY = moveBoundary.maxH
                moveX to moveY
            }

            FxAdsorbDirection.TOP_OR_BOTTOM -> {
                val moveX = safeX(x)
                val moveY = if (isNearestTop) moveBoundary.minH else moveBoundary.maxH
                moveX to moveY
            }
            FxAdsorbDirection.CORNERS -> {
                val moveX = if (isNearestLeft) moveBoundary.minW else moveBoundary.maxW
                val moveY = if (isNearestTop) moveBoundary.minH else moveBoundary.maxH
                moveX to moveY
            }
        }
    }

    private fun updateViewSize() {
        val view = basicView ?: return
        val (pW, pH) = view.parentSize() ?: return
        val viewH = view.measuredHeight.toFloat()
        val viewW = view.measuredWidth.toFloat()
        this.parentW = pW.toFloat()
        this.parentH = pH.toFloat()
        this.viewW = viewW
        this.viewH = viewH
        updateMoveBoundary()
        config.fxLog.d("fxView -> updateSize: parentW:$parentW,parentH:$parentH,viewW:$viewW,viewH:$viewH")
    }

     fun getDefaultLocation(): Pair<Float, Float> {
        val locationFrom: String
        val (defaultX, defaultY) =  if (config.hasDefaultXY) {
            locationFrom = "user_init_location"
            config.defaultX to config.defaultY
        } else {
            locationFrom = "default_location"
            getDefaultXY(parentW, parentH, viewW, viewH)
        }
        config.fxLog.d("fxView -> getDefaultLocation: x:$defaultX,y:$defaultY,way:[$locationFrom]")
        return  defaultX to defaultY
    }

    public fun isNearestLeft(x: Float): Boolean {
        val middle = parentW / 2
        val viewMiddlePoint = x + viewW / 2
        return viewMiddlePoint < middle
    }

    public fun isNearestTop(y: Float): Boolean {
        val middle = parentH / 2
        val viewMiddlePoint = y + viewH / 2
        return viewMiddlePoint < middle
    }

    private fun getHistoryXY(): Pair<Float, Float> {
        return config.run {
            val configX = iFxConfigStorage?.getX() ?: 0f
            val configY = iFxConfigStorage?.getY() ?: 0f
            configX to configY
        }
    }

    private fun getDefaultXY(
        width: Float,
        height: Float,
        viewW: Float,
        viewH: Float
    ): Pair<Float, Float> {
        val floatPair = config.run {
            // 为历史方法做兼容
            val l = fxBorderMargin.l + safeEdgeOffSet
            val r = fxBorderMargin.r + safeEdgeOffSet
            val b = fxBorderMargin.b + safeEdgeOffSet
            val t = fxBorderMargin.t + safeEdgeOffSet
            when (gravity) {
                FxGravity.DEFAULT,
                FxGravity.LEFT_OR_TOP -> l to t

                FxGravity.LEFT_OR_CENTER -> l to (height - viewH).shr(2)

                FxGravity.LEFT_OR_BOTTOM -> 0f to height - viewH - b

                FxGravity.RIGHT_OR_TOP -> width - viewW - r to t

                FxGravity.RIGHT_OR_CENTER -> width - viewW - r to (height - viewH).shr(2)

                FxGravity.RIGHT_OR_BOTTOM -> width - viewW - r to height - viewH - b

                FxGravity.TOP_OR_CENTER -> (width - viewW).shr(2) to t

                FxGravity.BOTTOM_OR_CENTER -> (width - viewW).shr(2) to height - viewH - b

                else -> (width - viewW).shr(2) to (height - viewH).shr(2)
            }.safeLocationXY
        }
        config.fxLog.d("fxView -> getDefaultXY: x:${floatPair.first},y:${floatPair.second}")
        return floatPair
    }

    internal fun updateMoveBoundary() {
        config.apply {
            // 如果启用了半隐藏，这里需要单独处理
            if (enableHalfHide) {
                val halfWidth = viewW * halfHidePercent
                moveIngBoundary.apply {
                    minW = -halfWidth
                    maxW = parentW - halfWidth
                    minH = statsBarHeight.toFloat()
                    maxH = parentH - viewH - navigationBarHeight
                }
                moveBoundary.copy(moveIngBoundary).apply {
                    minH += fxBorderMargin.t + edgeOffset
                    maxH -= fxBorderMargin.b + edgeOffset
                }
            } else {
                moveIngBoundary.apply {
                    minW = 0f
                    maxW = parentW - viewW
                    minH = statsBarHeight.toFloat()
                    maxH = parentH - viewH - navigationBarHeight
                }
                moveBoundary.copy(moveIngBoundary).apply {
                    minW += fxBorderMargin.l + edgeOffset
                    maxW -= fxBorderMargin.r + edgeOffset
                    minH += fxBorderMargin.t + edgeOffset
                    maxH -= fxBorderMargin.b + edgeOffset
                }
            }
            config.fxLog.d("fxView -> updateMoveBoundary, moveBoundary:$moveBoundary")
            config.fxLog.d("fxView -> updateMoveIngBoundary, moveIngBoundary:$moveIngBoundary")
        }
    }

    private   fun   checkOrRestoreLocation() {
        synchronized(this) {
            if (isInitLocation) return
            config.fxLog.d("fxView -> restoreLocation,start")
            val defaultXY = getDefaultLocation()
            val (restoreX, restoreY) = if (config.enableEdgeAdsorption) {
                // 如果是由configChange触发，则优先使用之前保存的
                val (isNearestLeft, isNearestTop) = if (needUpdateConfig) {
                    restoreLeftStandard to restoreTopStandard
                } else {
                    isNearestLeft(defaultXY.first) to isNearestTop(defaultXY.second)
                }
                getAdsorbDirectionLocation(isNearestLeft, isNearestTop)
            } else {
                safeX(defaultXY.first) to safeY(defaultXY.second)
            }
            restoreLeftStandard = false
            restoreTopStandard = false
            needUpdateLocation = false
            needUpdateConfig = false
            basicView?.internalMoveToXY(restoreX, restoreY,needUpdateLocation= true)
            config.fxLog.d("fxView -> restoreLocation,success")
        }
    }
}
