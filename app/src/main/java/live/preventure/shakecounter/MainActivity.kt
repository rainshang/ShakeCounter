package live.preventure.shakecounter

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import live.preventure.shakecounter.fragment.CounterFragment
import live.preventure.shakecounter.fragment.MainFragment

class MainActivity(startTimestamp: Long = System.currentTimeMillis()) : AppCompatActivity(),
    MainFragment.MainFragmentListener, CounterFragment.CounterFragmentListener {

    var sensorService: SensorService? = null
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            sensorService = (service as SensorService.SensorBinder).getService()
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment is MainFragment) {
                currentFragment.refreshUI()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            sensorService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(this, SensorService::class.java).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
            } else {
                startService(it)
            }
        }
        setContentView(R.layout.activity_main)
        supportFragmentManager.apply {
            addOnBackStackChangedListener {
                supportActionBar?.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
            }
            beginTransaction()
                .add(R.id.fragment_container, MainFragment())
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, SensorService::class.java).also {
            bindService(it, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (sensorService != null) {
            unbindService(mConnection)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (android.R.id.home == item?.itemId) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onMainBtnClick(text: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, CounterFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onStopClick() {
        onBackPressed()
    }

}
