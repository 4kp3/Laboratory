package com.lovely.bear.laboratory.main.fragment

import android.content.Intent
import android.os.Bundle
import android.util.ArrayMap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.lovely.bear.laboratory.R
import com.lovely.bear.laboratory.continuation.view.alert
import com.lovely.bear.laboratory.dan.mu.Danmu2Activity
import com.lovely.bear.laboratory.dan.mu.DanmuActivity
import com.lovely.bear.laboratory.databinding.FragmentFirstBinding
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
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

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
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id= R.id.toolbar_special
//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }
//        binding.progressView.setProgress(
//            ProgressView.Progress(count = 1,total = 12,score = 0F,node = ProgressView.Progress.Node(a=74.3F,b=55F,c=20.9F))
//        )
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
        binding.tvOpenDanmu.setOnClickListener {
            com.lovely.bear.laboratory.util.startActivity<DanmuActivity>(requireContext())
        }
        binding.tvOpenDanmu2.setOnClickListener {
            com.lovely.bear.laboratory.util.startActivity<Danmu2Activity>(requireContext())
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
                get() = "查看 ActivityThread 的 ClassLoader"

            override fun doAction() {
                val atClazz = Class.forName("android.app.ActivityThread")
                val currATField = atClazz.getDeclaredField("sCurrentActivityThread")
                currATField.isAccessible = true
                val appBindDataField = atClazz.getDeclaredField("mBoundApplication")


                val packagesField = atClazz.getDeclaredField("mPackages")
                packagesField.isAccessible = true

//                val appBindDataClazz = Class.forName("android.app.ActivityThread.AppBindData")
//                val loadedApkField = appBindDataClazz.getDeclaredField("info")

                val laClazz = Class.forName("android.app.LoadedApk")
                val baseClassLoaderField = laClazz.getDeclaredField("mBaseClassLoader")
                baseClassLoaderField.isAccessible = true
                //val defaultClassLoaderField = laClazz.getDeclaredField("mDefaultClassLoader")

                val currentAT = currATField.get(atClazz)
//                val appBindData = appBindDataField.get(currentAT)
                val packages = packagesField.get(currentAT) as ArrayMap<String, WeakReference<Any>>
                val sb = java.lang.StringBuilder("Packages:\n")
                for (p in packages) {
                    sb.append(p.key).append("-").append(p.value.get()).append("\n")
                }
                Log.d("packages", sb.toString())

//                val loadedApk = loadedApkField.get(appBindData)
//                val baseClassLoader = baseClassLoaderField.get(loadedApk) as? ClassLoader
//                val defaultClassLoader = defaultClassLoaderField.get(loadedApk) as? ClassLoader
//                val baseClassLoader =

                fun showClassLoader(prefix: String, classLoader: ClassLoader?) {
                    val sb: java.lang.StringBuilder = StringBuilder("$prefix:\n")
                    //Log.d("ClassLoader",)
                    var clazzLoader: ClassLoader? = classLoader
                    while (clazzLoader != null) {
                        sb.append(clazzLoader).append("\n")
                        clazzLoader = clazzLoader.parent
                    }
                    Log.d("ClassLoader:", sb.toString())
                }

//                showClassLoader("baseClassLoader", baseClassLoader)
//                showClassLoader("defaultClassLoader", defaultClassLoader)
            }
        })
        actionView.addItem(object : ActionItem {
            override val desc: String
                get() = "启动一个未注册的 Activity"

            override fun doAction() {
                startActivity(Intent(activity!!.applicationContext, NoRegisterActivity::class.java))
            }
        })
    }

    override fun onResume() {
        super.onResume()
//        binding.textviewFirst.isSelected = true
//        binding.textviewFirst2.isSelected = true
    }

    override fun onPause() {
        super.onPause()
//        binding.textviewFirst.isSelected = false
//        binding.textviewFirst2.isSelected = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}