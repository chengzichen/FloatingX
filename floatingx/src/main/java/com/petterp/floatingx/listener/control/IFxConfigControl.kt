package com.petterp.floatingx.listener.control

import com.petterp.floatingx.assist.FxAnimation
import com.petterp.floatingx.assist.FxDisplayMode
import com.petterp.floatingx.listener.IFxConfigStorage
import com.petterp.floatingx.listener.IFxViewLifecycle
import com.petterp.floatingx.assist.FxAdsorbDirection
import com.petterp.floatingx.assist.FxGravity
import com.petterp.floatingx.listener.IFxTouchListener

/**
 * 配置更改接口,使用此接口运行时更改配置层
 * @author petterp
 */
interface IFxConfigControl {

    /** 是否启用动画
     * @param isEnable 是否启用
     * @param animationImpl 具体实现实例
     * */
    fun setEnableAnimation(isEnable: Boolean, animationImpl: FxAnimation? = null)

    /** 是否启用动画 */
    fun setEnableAnimation(isEnable: Boolean)

    /** 设置边框相对应父view的偏移量 */
    fun setBorderMargin(t: Float, l: Float, b: Float, r: Float)

    /** 设置边缘吸附方向 */
    fun setEdgeAdsorbDirection(direction: FxAdsorbDirection)

    /** 设置是否启用点击事件 */
    fun setEnableClick(isEnable: Boolean)

    /** 设置边缘偏移量 */
    fun setEdgeOffset(edgeOffset: Float)

    /** 启用边缘回弹
     * */
    fun setEnableEdgeRebound(isEnable: Boolean)

    /** 设置是否支启用悬浮窗半隐藏模式
     * */
    fun setEnableHalfHide(isEnable: Boolean)

    /**
     * 调用此方法,将忽视传递的(x,y)。 浮窗的坐标将根据 传递进来的 [gravity] + 此方法传入的偏移量
     * 计算，而非直接坐标。 这样的好处是,你不用去关注具体浮窗坐标应该是什么，而是可以依靠参照物的方式摆放。
     * 比如默认你的浮窗在右下角，但是想增加一点在右侧偏移，此时就可以依靠此方法，将浮窗位置设置在右下角，然后增加相应方向的偏移量即可。
     */
    fun setOffsetXY(x: Float, y: Float)

    /**
     * 设置悬浮窗视图默认位置,默认右下角,
     *
     * 注意：此方法会影响setX()||setY()
     */
    fun setGravity(gravity: FxGravity)

    /**
     * 设置悬浮窗半隐藏模式的隐藏比例
     * @param isEnable 是否启用
     * @param percent 半隐比例
     * */
    fun setEnableHalfHide(isEnable: Boolean, percent: Float = 0.5f)


    /**
     * 设置仅仅贴边滑动
     * @param isEnable 是否启用 默认不开启
     *    TOP,
     *     LEFT,
     *     RIGHT,
     *     BOTTOM,
     *     生效
     */
    fun setEnableOnlyEdgeSlide(isEnable: Boolean)
    /**
     * 设置浮窗展示模式
     *
     * @param mode 展示模式
     * - [FxDisplayMode.Normal] 默认模式，可以移动与点击
     * - [FxDisplayMode.ClickOnly] 禁止移动，只能响应点击事件
     * - [FxDisplayMode.DisplayOnly] 只能展示，不能移动与响应点击事件
     * */
    fun setDisplayMode(mode: FxDisplayMode)

    /**
     * 启用边缘吸附
     * */
    fun setEnableEdgeAdsorption(isEnable: Boolean)

    /** 设置滑动监听 */
    fun setTouchListener(listener: IFxTouchListener)

    /** 设置view-lifecycle监听 */
    @Deprecated(
        replaceWith = ReplaceWith("addViewLifecycleListener"),
        message = "use addViewLifecycle"
    )
    fun setViewLifecycleListener(listener: IFxViewLifecycle)

    fun addViewLifecycleListener(listener: IFxViewLifecycle)

    /** 设置允许保存方向 */
    fun setEnableSaveDirection(impl: IFxConfigStorage, isEnable: Boolean = true)

    /** 设置方向保存开关
     * 设置之前,请确保已经设置了方向保存实例
     * */
    fun setEnableSaveDirection(isEnable: Boolean = true)

    /** 清除保存的位置信息 */
    fun clearLocationStorage()

    /**获取当前View的位置*/
    fun getViewLocation(): Pair<Float, Float>?
}
