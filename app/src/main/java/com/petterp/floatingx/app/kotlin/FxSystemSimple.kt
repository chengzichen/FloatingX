package com.petterp.floatingx.app.kotlin

import android.R.attr.bottom
import android.R.attr.left
import android.R.attr.right
import android.R.attr.top
import android.app.AlertDialog
import android.app.Application
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.petterp.floatingx.FloatingX
import com.petterp.floatingx.app.BuildConfig
import com.petterp.floatingx.app.MainActivity
import com.petterp.floatingx.app.R
import com.petterp.floatingx.app.simple.FxAnimationImpl
import com.petterp.floatingx.app.simple.FxConfigStorageToSpImpl
import com.petterp.floatingx.app.test.BlackActivity
import com.petterp.floatingx.app.test.ImmersedActivity
import com.petterp.floatingx.app.test.MultipleFxActivity
import com.petterp.floatingx.app.test.ScopeActivity
import com.petterp.floatingx.assist.FxAdsorbDirection
import com.petterp.floatingx.assist.FxDisplayMode
import com.petterp.floatingx.assist.FxGravity
import com.petterp.floatingx.assist.FxScopeType
import com.petterp.floatingx.listener.IFxProxyTagActivityLifecycle
import com.petterp.floatingx.listener.IFxTouchListener
import com.petterp.floatingx.listener.IFxViewLifecycle
import com.petterp.floatingx.listener.control.IFxAppControl
import com.petterp.floatingx.view.FxBasicContainerView
import com.petterp.floatingx.view.FxViewHolder
import kotlin.math.abs


/**
 *
 * @author petterp
 */
object FxSystemSimple {
    var appControl : IFxAppControl?=null
    fun install(context: Application) {
        appControl = FloatingX.install {
            setContext(context)
            setLayout(R.layout.item_floating)
            setScopeType(FxScopeType.SYSTEM_AUTO)
            // 设置浮窗展示类型，默认可移动可点击，无需配置
            setSaveDirectionImpl(FxConfigStorageToSpImpl(context))
            setDisplayMode(FxDisplayMode.Normal)
            // 设置权限拦截器
            setPermissionInterceptor { activity, controller ->
                AlertDialog.Builder(activity).setTitle("提示").setMessage("需要允许悬浮窗权限")
                    .setPositiveButton("去开启") { _, _ ->
                        Toast.makeText(
                            activity.applicationContext,
                            "去申请权限中~",
                            Toast.LENGTH_SHORT,
                        ).show()
                        controller.requestPermission(
                            activity,
                            isAutoShow = true,
                            canUseAppScope = true,
                        ) {
                            Toast.makeText(
                                activity.applicationContext,
                                "申请权限结果: $it",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }.setNegativeButton("取消") { _, _ ->
                        Toast.makeText(
                            activity.applicationContext,
                            "降级为App浮窗~",
                            Toast.LENGTH_SHORT,
                        ).show()
                        controller.downgradeToAppScope()
                    }.show()
            }
            // 设置悬浮窗默认方向
            setGravity(FxGravity.TOP_OR_CENTER)
                .setBorderMargin(20f,0f,0f,0f)
            // 设置偏移位置
//            setOffsetXY(100f, 100f)
            // 设置启用边缘吸附,默认启用
            setEnableEdgeAdsorption(true)
            // 设置边缘偏移量
//            setEdgeOffset(10f)
            // 设置启用悬浮窗可屏幕外回弹
            setEnableScrollOutsideScreen(false)
            // 开启历史位置缓存
//                setSaveDirectionImpl(FxConfigStorageToSpImpl(context))
            // 设置启用动画
            setEnableAnimation(true)
            // 设置启用动画实现
            setAnimationImpl(FxAnimationImpl())
            // 设置移动边框
            /** 指定浮窗可显示的activity方式 */
            // 1.设置是否允许所有activity都进行显示,默认true
            setEnableAllInstall(true)
            // 2.禁止插入Activity的页面, setEnableAllBlackClass(true)时,此方法生效
            addInstallBlackClass(BlackActivity::class.java)
            addInstallBlackClass(ScopeActivity::class.java.name)
            // 3.允许插入Activity的页面, setEnableAllBlackClass(false)时,此方法生效
            addInstallWhiteClass(
                MainActivity::class.java,
                ImmersedActivity::class.java,
            )
            setEdgeAdsorbDirection(FxAdsorbDirection.CORNERS)
            // 设置点击事件
            setOnClickListener {
                Toast.makeText(context, "浮窗被点击", Toast.LENGTH_SHORT).show()
            }
            setOnLongClickListener {
                Toast.makeText(context, "浮窗被长按", Toast.LENGTH_SHORT).show()
                true
            }
            // 设置tag-Activity生命周期回调时的触发
            setTagActivityLifecycle(object : IFxProxyTagActivityLifecycle {
            })
            // 增加生命周期监听
            addViewLifecycle(object : IFxViewLifecycle {
                override fun initView(holder: FxViewHolder) {
                    holder.getViewOrNull<TextView>(R.id.tvItemFx)?.apply {
                        text = "System浮窗"
                        setOnClickListener {
                            Toast.makeText(context, "子view点击", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
            // 设置滑动监听
            setTouchListener(object : IFxTouchListener {
                override fun onDown() {

                }
                override fun OnDragEnd(
                    x: Float,
                    y: Float,
                    nearestTop: Boolean,
                    nearestLeft: Boolean,
                    isInit: Boolean
                ) {
                    // 释放
                    appControl?.updateViewContent { holder ->
                        val textView = holder.getViewOrNull<TextView>(R.id.tvItemFx)
                        if(nearestTop){
                            if(nearestLeft){
                                textView?.text = "左上"
                            }else{
                                textView?.text = "右上"
                            }

                        }else{
                            if(nearestLeft){
                                textView?.text = "左下"
                            }else{
                                textView?.text = "右下"
                            }
                        }
                    }
                }

                override fun onUp() {

                }

                override fun onDragIng(event: MotionEvent?, x: Float, y: Float) {
                    // 拖动
                    appControl?.updateViewContent { holder ->
                        val textView = holder.getViewOrNull<TextView>(R.id.tvItemFx)
                        textView?.text = "我被拖动"
                    }
                }
            })
            // 设置是否启用日志
            setEnableLog(BuildConfig.DEBUG)
            // 设置浮窗tag
            setTag(MultipleFxActivity.TAG_1)
        }

        appControl?.show()
    }
}