package live.preventure.shakecounter.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_counter.*
import live.preventure.shakecounter.MainActivity
import live.preventure.shakecounter.R

class CounterFragment : Fragment() {

    private var mListener: CounterFragmentListener? = null
    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            refreshUI()
            sendEmptyMessageDelayed(0, 1000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_counter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        reset_btn.setOnClickListener {
            val sensorService = (context as MainActivity).sensorService
            sensorService?.startListenAcceleration()
            refreshUI()
        }
        stop_btn.setOnClickListener {
            val sensorService = (context as MainActivity).sensorService
            sensorService?.stopListenAcceleration()
            refreshUI()
            mListener?.onStopClick()
        }
        refreshUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CounterFragmentListener) {
            mListener = context
            mHandler.sendEmptyMessage(0)
        } else {
            throw RuntimeException("$context must implement CounterFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
        mHandler.removeMessages(0)
    }

    private fun refreshUI() {
        val sensorService = (context as MainActivity).sensorService
        if (sensorService!!.isCounting) {
            val durationSeconds = ((System.currentTimeMillis() - sensorService.startTime) / 1000).toInt()
            label_time.text = formatTime(durationSeconds)
            label_count.text = "${sensorService.shakeCount}"
        } else {
            label_time.text = "--:--:--"
            label_count.text = "-"
        }
    }

    private fun formatTime(seconds: Int): String = String.format(
        "%02d:%02d:%02d",
        seconds / 3600,
        (seconds % 3600) / 60,
        (seconds % 60)
    )

    interface CounterFragmentListener {
        fun onStopClick()
    }
}
