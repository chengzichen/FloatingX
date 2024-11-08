package com.petterp.floatingx.app.test

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.petterp.floatingx.FloatingX
import com.petterp.floatingx.app.MainActivity
import com.petterp.floatingx.app.R
import com.petterp.floatingx.app.addItemView
import com.petterp.floatingx.app.addLinearLayout
import com.petterp.floatingx.app.addNestedScrollView
import com.petterp.floatingx.app.createLinearLayoutToParent
import com.petterp.floatingx.app.dp
import com.petterp.floatingx.app.start
import com.petterp.floatingx.assist.FxGravity

/**
 *
 * @author petterp
 */
class SystemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createLinearLayoutToParent {
            addNestedScrollView {
                addLinearLayout {
                    addItemView("移动到fx2"){
                        MainActivity::class.java.start(this@SystemActivity)
                    }
                    addItemView("移动到(0,0)") {
                        FloatingX.controlOrNull(MultipleFxActivity.TAG_1)?.move(0f, 0f)
                    }
                    addItemView("移动到(-100,0)") {
                        FloatingX.controlOrNull(MultipleFxActivity.TAG_1)?.move(-100f, 0f)
                    }
                    addItemView("移动到(300,0)") {
                        FloatingX.controlOrNull(MultipleFxActivity.TAG_1)?.move(300f, 0f)
                    }
                    addItemView("移动到(500,500)") {
                        FloatingX.controlOrNull(MultipleFxActivity.TAG_1)?.move(500f, 500f)
                    }
                    addItemView("左上角") {
                        FloatingX.controlOrNull(MultipleFxActivity.TAG_1)?.setGravity(FxGravity.LEFT_OR_TOP)
                    }
                    addItemView("updateView()") {
                        FloatingX.controlOrNull(MultipleFxActivity.TAG_1)?.updateView {
                            TextView(it).apply {
                                layoutParams = ViewGroup.LayoutParams(100.dp, 200.dp)
                                text = "App"
                                gravity = Gravity.CENTER
                                textSize = 15f
                                setBackgroundColor(Color.GRAY)
                            }
                        }
                    }
                    addItemView("边距调整为100f") {
                        FloatingX.controlOrNull(MultipleFxActivity.TAG_1)?.updateConfig {
                            setBorderMargin(200f, 200f, 200f, 200f)
                        }
                    }
                    addItemView("边距调整为0f") {
//                        FloatingX.controlOrNull(MultipleFxActivity.TAG_1)?.updateConfig {
//                            setBorderMargin(20f, 20f, 20f, 20f)
////                            setGravity(FxGravity.RIGHT_OR_TOP)
//                        }
                        FloatingX.controlOrNull(MultipleFxActivity.TAG_1)?. setBorderMargin(20f, 20f, 20f, 20f)
                    }
                    addItemView("默认位置") {
                        FloatingX.controlOrNull(MultipleFxActivity.TAG_1)?.setGravity(FxGravity.DEFAULT)
                    }
                    addItemView("右上角") {
                        FloatingX.controlOrNull(MultipleFxActivity.TAG_1)?.setGravity(FxGravity.RIGHT_OR_TOP)
                    }
                    addItemView("右上角") {
                        FloatingX.controlOrNull(MultipleFxActivity.TAG_1)?.setGravity(FxGravity.RIGHT_OR_TOP)
                    }
                    addItemView("updateView2()") {
                        FloatingX.controlOrNull(MultipleFxActivity.TAG_1)
                            ?.updateView(R.layout.item_floating_new)
                    }
                }
            }
        }
    }
}
