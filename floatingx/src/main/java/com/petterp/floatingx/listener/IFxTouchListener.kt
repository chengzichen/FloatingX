package com.petterp.floatingx.listener

/**
 * Fx手势监听器
 * @author petterp
 */
import android.view.MotionEvent
import com.petterp.floatingx.assist.helper.FxBuilderDsl
import com.petterp.floatingx.view.IFxInternalHelper

/**
 * fx监听事件，用于监听浮窗上的一些手势事件
 *
 * 默认提供了一些常见的事件场景，比如按下、松开、浮窗位置移动,如需监听所有，请在eventIng中监听所有事件
 * */
@FxBuilderDsl
interface IFxTouchListener {

    /** 按下 */
    fun onDown() {}

    /** 松开 */
    fun onUp() {}

    /**
     * 监测当前移动浮窗的手指 move回调
     *
     * @param event 当前手势事件 可能是为空，使用api调用时滑动是也会回调
     * @param x 当前浮窗相对于父View的x坐标
     * @param y 当前浮窗相对于父View的y坐标.
     * @since x,y代表了当前浮窗相对于父View的位置信息,即当前浮窗(左上角坐标)。
     * App与Activity级别时一般相当于其相对于屏幕的位置。
     * View级别相当于其相当于父ViewGroup
     *
     * 注意：[displayMode == FxDisplayMode.ClickOnly] 时，x,y不会变化
     *
     * 注意：这个方法仅会回调当前正在按压的手指事件
     *
     * */
    fun onDragIng(event: MotionEvent?, x: Float, y: Float) {}

    /**
     * 实现此方法，可实现类似TouchListener的拦截效果
     * @param event 当前事件
     * @param control 浮窗控制器,便于自行实现一些调度
     * @return true:拦截当前事件处理，
     * @return false:未处理,走系统默认事件
     * */
    fun onTouch(event: MotionEvent, control: IFxInternalHelper?): Boolean = false

    /**
     * 浮窗事件拦截
     *
     * 默认情况下，浮窗会拦截所有事件，为了保证优先滑动，通常无需重写该方法，除非有特殊需求,比如你的浮窗view内部希望只有指定的view触摸时才可以滑动，此时就可以进行拦截修改
     * @param event 当前事件
     * @param control 浮窗控制器,便于自行实现一些调度
     * @return true(default) 拦截当前事件,事件不会传给下一级
     * @return false  浮窗禁止滑动，事件会传给下一级
     * */
    fun onInterceptTouchEvent(event: MotionEvent, control: IFxInternalHelper?): Boolean = true

    /**
     * Called when the drag operation ends. This function is used to handle the logic after the user stops dragging.
     *
     * @param endX The X coordinate where the drag ends. Used to determine the final position of the drag.
     * @param endY The Y coordinate where the drag ends. Used to determine the final position of the drag.
     * @param nearestTop A Boolean value indicating whether the drag ends closest to the top. Helps decide if the object should be moved to the top.
     * @param nearestLeft A Boolean value indicating whether the drag ends closest to the left. Helps decide if the object should be moved to the left.
     * @param isInit An optional Boolean parameter indicating whether this is an initialization drag. Defaults to false.
     */
    fun OnDragEnd(endX: Float, endY: Float, nearestTop: Boolean, nearestLeft: Boolean, isInit: Boolean = false) {}

}
