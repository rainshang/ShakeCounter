package live.preventure.shakecounter.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.fragment_main.*
import live.preventure.shakecounter.MainActivity
import live.preventure.shakecounter.R

class MainFragment : Fragment() {

    private var mListener: MainFragmentListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        main_btn.apply {
            setOnClickListener {
                val sensorService = (context as MainActivity).sensorService
                if (sensorService != null && !sensorService.isCounting) {
                    sensorService.startListenAcceleration()
                    refreshUI()
                }
                mListener?.onMainBtnClick((it as Button).text.toString())
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    fun refreshUI() {
        val sensorService = (context as MainActivity).sensorService
        if (sensorService != null) {
            main_btn.apply {
                if (sensorService.isCounting) {
                    setText(R.string.btn_back2counter)
                } else {
                    setText(R.string.btn_start)
                }
            }
        }
    }

    interface MainFragmentListener {
        fun onMainBtnClick(text: String)
    }
}
