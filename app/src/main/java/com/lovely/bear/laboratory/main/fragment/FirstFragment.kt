package com.lovely.bear.laboratory.main.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope

import com.lovely.bear.laboratory.continuation.view.alert
import com.lovely.bear.laboratory.databinding.FragmentFirstBinding
import com.lovely.bear.laboratory.function.async.AsyncLayoutActivity
import com.lovely.bear.laboratory.function.async.AsyncLayoutControlActivity
import com.lovely.bear.laboratory.https.initOk
import com.lovely.bear.laboratory.https.okHttpClient
import com.lovely.bear.laboratory.https.testOkSSL
import com.lovely.bear.laboratory.launch.LaunchTestStandardActivity
import com.lovely.bear.laboratory.main.NoRegisterActivity
import com.lovely.bear.laboratory.main.SecondActivity
import com.lovely.bear.laboratory.surface.TestSurfaceViewActivity
import com.lovely.bear.laboratory.widget.TagDrawable
import com.lovely.bear.laboratory.widget.action.ActionItem
import com.lovely.bear.laboratory.widget.action.ActionView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarSpecial.toolbarViewGroup.setOnClickListener {
            if (binding.toolbar2ViewStub.parent != null) {
                binding.toolbar2ViewStub.inflate()
            }
        }

        //binding.toolbar.toolbarViewGroup.viewTreeObserver.addOnGlobalFocusChangeListener()
        binding.tvConfirmAlert.setOnClickListener {
            lifecycleScope.launch {
                val ok = requireContext().alert("升职加薪", "是否要升职加薪？")
                Toast.makeText(requireContext(), "$ok", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvSsl.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                initOk()
                if (okHttpClient != null) {
                    testOkSSL(okHttpClient!!)
                }
            }
        }
        binding.progressView.setData(3.9F)
        binding.iv.background = TagDrawable("优质", requireContext().resources)

        binding.tvStartSecond.setOnClickListener {
            com.lovely.bear.laboratory.util.startActivity<SecondActivity>(requireContext())
        }

        binding.tvStartStandardActivity.setOnClickListener {
            com.lovely.bear.laboratory.util.startActivity<LaunchTestStandardActivity>(requireContext())
        }

        binding.tvStartSurfaceActivity.setOnClickListener {
            com.lovely.bear.laboratory.util.startActivity<TestSurfaceViewActivity>(requireContext())
        }

        initActionView(binding.actionView)
    }

    private fun initActionView(actionView: ActionView) {
        actionView.addItem(object : ActionItem {
            override val desc: String
                get() = "查看 ClassLoader"

            override fun doAction() {
                val sb: java.lang.StringBuilder = StringBuilder("ClassLoader:\n")
                //Log.d("ClassLoader",)
                var classLoader = this@FirstFragment.javaClass.classLoader
                while (classLoader != null) {
                    sb.append(classLoader).append("\n")
                    classLoader = classLoader.parent
                }
                Log.d("ClassLoader:", sb.toString())
            }
        })

        actionView.addItem(object : ActionItem {
            override val desc: String
                get() = "启动一个未注册的 Activity"

            override fun doAction() {
                startActivity(Intent(activity!!.applicationContext, NoRegisterActivity::class.java))
            }
        })

        actionView.addItem(object : ActionItem {
            override val desc: String
                get() = "使用 AsyncLayoutInflater 预加载 Activity ContentView"

            override fun doAction() {
                AsyncLayoutActivity.preloadContentView(requireContext())
                startActivity(
                    Intent(
                        activity!!.applicationContext,
                        AsyncLayoutActivity::class.java
                    )
                )
            }
        })

        actionView.addItem(object : ActionItem {
            override val desc: String
                get() = "使用 AsyncLayoutInflater 预加载 Activity 对照页面（正常填充View）"

            override fun doAction() {
                startActivity(
                    Intent(
                        activity!!.applicationContext,
                        AsyncLayoutControlActivity::class.java
                    )
                )
            }
        })

        actionView.addItem(object : ActionItem {
            override val desc: String
                get() = "锁等待"

            override fun doAction() {
                val t = LockThread()
                lifecycleScope.launch {
                    t.lock.lock()
                    t.start()
                    delay(1000)
                    t.lock.unlock()
                }
            }
        })

        actionView.addItem(object : ActionItem {
            override val desc: String
                get() = "图像处理"

            override fun doAction() {
//                com.lovely.bear.laboratory.util.startActivity<BitmapAlgorithmActivity>(requireContext())
            }
        })
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}