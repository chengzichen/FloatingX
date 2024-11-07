package com.petterp.floatingx.imp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference
import java.util.Stack

/**
 * Fx基础Provider提供者
 * @author petterp
 */
class FxAppLifecycleProvider : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        addActivity(activity)
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        removeActivity(activity)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    companion object {
        private var activityStack: Stack<Activity>? = null

        @JvmSynthetic
        fun getTopActivity(): Activity? =if (activityStack!=null&& activityStack!!.isNotEmpty()){ activityStack?.lastElement()} else null
        fun addActivity(activity: Activity) {
            if (activityStack == null) {
                activityStack = Stack()
            }
            activityStack!!.add(activity)
        }

        fun removeActivity(activity: Activity?) {
            if (activity != null) {
                activityStack!!.remove(activity)
            }
        }
    }
}
