package com.lovely.bear.laboratory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.FragmentFirstBinding
import com.lovely.bear.laboratory.continuation.view.alert
import com.lovely.bear.laboratory.dan.mu.DanmuActivity
import kotlinx.coroutines.*

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

//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }
//        binding.progressView.setProgress(
//            ProgressView.Progress(count = 1,total = 12,score = 0F,node = ProgressView.Progress.Node(a=74.3F,b=55F,c=20.9F))
//        )
        binding.tvConfirmAlert.setOnClickListener {
            lifecycleScope.launch {
                val ok = requireContext().alert("升职加薪", "是否要升职加薪？")
                Toast.makeText(requireContext(), "$ok", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvOpenDanmu.setOnClickListener {
            startActivity<DanmuActivity>(requireContext())
        }
        binding.progressView.setData(3.9F)
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