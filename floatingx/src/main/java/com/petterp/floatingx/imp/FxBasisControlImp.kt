package com.petterp.floatingx.imp

import android.view.View
import androidx.annotation.LayoutRes
import com.petterp.floatingx.assist.FxGravity
import com.petterp.floatingx.assist.helper.FxBasisHelper
import com.petterp.floatingx.listener.control.IFxConfigControl
import com.petterp.floatingx.listener.control.IFxControl
import com.petterp.floatingx.listener.provider.IFxAnimationProvider
import com.petterp.floatingx.listener.provider.IFxContextProvider
import com.petterp.floatingx.listener.provider.IFxHolderProvider
import com.petterp.floatingx.listener.provider.IFxPlatformProvider
import com.petterp.floatingx.util.INVALID_LAYOUT_ID
import com.petterp.floatingx.view.FxBasicContainerView
import com.petterp.floatingx.view.IFxInternalHelper

/**
 * Fx基础控制器,用于协调各provider的分发
 * */
abstract class FxBasisControlImp<F : FxBasisHelper, P : IFxPlatformProvider<F>>(
    protected val helper: F
) : IFxControl {

    protected lateinit var platformProvider: P
    private lateinit var _configControl: IFxConfigControl
    private lateinit var _animationProvider: IFxAnimationProvider
    private val internalView: IFxInternalHelper?
        get() = platformProvider.internalView

    override val configControl: IFxConfigControl get() = _configControl
    override fun getView() = internalView?.childView
    override fun getViewHolder() = internalView?.viewHolder
    override fun getManagerView() = internalView?.containerView

    abstract fun createPlatformProvider(f: F): P
    open fun createConfigProvider(f: F, p: P): IFxConfigControl = FxBasicConfigProvider(f, p)
    open fun createAnimationProvider(f: F, p: P): IFxAnimationProvider = FxBasicAnimationProvider(f)

    fun initProvider() {
        platformProvider = createPlatformProvider(helper)
        _animationProvider = createAnimationProvider(helper, platformProvider)
        _configControl = createConfigProvider(helper, platformProvider)
    }

    override fun show() {
        if (isShow()) return
        if (!updateEnableStatusAndCheckCanShow()) return
        val fxView = getManagerView() ?: return
        platformProvider.show()
        helper.fxLog.d("fxView -> showFx")
        if (_animationProvider.canRunAnimation()) {
            _animationProvider.start(fxView)
        }
    }

    private fun updateEnableStatusAndCheckCanShow(): Boolean {
        val oldStatus = helper.enableFx
        // pre update status
        helper.enableFx = true
        val canShow = platformProvider.checkOrInit()
        // If it was not open before, it can be safely updated to the latest status
        if (!oldStatus) helper.enableFx = canShow
        return canShow
    }

    override fun hide() {
        if (!isShow()) return
        helper.enableFx = false
        val fxView = getManagerView() ?: return
        helper.fxLog.d("fxView -> hideFx")
        if (_animationProvider.canCancelAnimation()) {
            _animationProvider.hide(fxView) {
                platformProvider.hide()
            }
        } else {
            platformProvider.hide()
        }
    }

    override fun cancel() {
        val fxView = getManagerView()
        (fxView as? FxBasicContainerView)?.preCancelAction()
        if (fxView != null && isShow() && _animationProvider.canRunAnimation()) {
            _animationProvider.hide(fxView) {
                reset()
            }
        } else {
            reset()
        }
    }

    override fun isShow(): Boolean {
        val interView = getManagerView() ?: return false
        val isShow = platformProvider.isShow()
        if (isShow != null) return isShow
        return interView.isAttachedToWindow && interView.visibility == View.VISIBLE
    }

    override fun updateView(@LayoutRes resource: Int) {
        check(resource != INVALID_LAYOUT_ID) { "resource cannot be INVALID_LAYOUT_ID!" }
        // 检查是否为相同布局ID，如果是则不进行更新
        if (helper.layoutId == resource) {
            helper.fxLog.d("fxView -> updateView skipped, same layoutId: $resource")
            return
        }
        helper.layoutView = null
        helper.layoutId = resource
        internalView?.updateView(resource)
    }

    override fun setBorderMargin(t: Float, l: Float, b: Float, r: Float) {
        val fxBorderMargin = helper.fxBorderMargin
        fxBorderMargin.t = t
        fxBorderMargin.l = l
        fxBorderMargin.b = b
        fxBorderMargin.r = r
        (internalView as? FxBasicContainerView)?.locationHelper?.updateMoveBoundary()
    }

    override fun updateView(view: View) {
        // 检查是否为相同View对象，如果是则不进行更新
        if (helper.layoutView == view) {
            helper.fxLog.d("fxView -> updateView skipped, same layoutView object")
            return
        }
        helper.layoutId = INVALID_LAYOUT_ID
        helper.layoutView = view
        internalView?.updateView(view)
    }

    override fun updateView(provider: IFxContextProvider) {
        updateView(provider.build(platformProvider.context))
    }

    override fun updateViewContent(provider: IFxHolderProvider) {
        provider.apply(getViewHolder() ?: return)
    }

    override fun setClickListener(time: Long, listener: View.OnClickListener?) {
        helper.clickTime = time
        helper.iFxClickListener = listener
        helper.enableClickListener = listener != null
    }

    override fun setClickListener(listener: View.OnClickListener?) {
        setClickListener(0, listener)
    }

    override fun setLongClickListener(listener: View.OnLongClickListener?) {
        helper.iFxLongClickListener = listener
        helper.enableClickListener = listener != null
    }
    override fun setGravity(gravity: FxGravity) {
        helper.gravity = gravity
    }



    override fun moveToEdge(useAnimation: Boolean) {
        (internalView as? FxBasicContainerView)?.locationHelper?.updateMoveBoundary()
        internalView?.moveToEdge(useAnimation)
    }

    override fun moveDefault(useAnimation: Boolean) {
        internalView?.moveDefaultLocation(useAnimation)
    }

    override fun move(x: Float, y: Float) {
        move(x, y, true)
    }

    override fun moveByVector(x: Float, y: Float) {
        moveByVector(x, y, true)
    }

    override fun move(x: Float, y: Float, useAnimation: Boolean) {
        internalView?.moveLocation(x, y, useAnimation)
    }

    override fun moveByVector(x: Float, y: Float, useAnimation: Boolean) {
        internalView?.moveLocationByVector(x, y, useAnimation)
    }


    override fun updateConfig(obj: IFxConfigControl.() -> Unit) {
        obj.invoke(_configControl)
    }

    protected open fun reset() {
        platformProvider.reset()
        _animationProvider.reset()
        helper.clear()
        helper.fxLog.d("fxView-lifecycle-> code->cancelFx")
    }
}
