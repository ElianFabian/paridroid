package fr.ubordeaux.math.paridroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.preference.PreferenceManager

class PariActivity : AppCompatActivity() {

    private val viewModel: PariViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val parisizeStr = prefs.getString("parisize", getString(R.string.parisize_default))
        val parisize = parisizeStr?.toLongOrNull() ?: 100000000L

        val map = prefs.all
        for ((key, value) in map) {
            if (key != "parisize" && value != null) {
                ParidroidGP.default.setDefault(key, value.toString())
            }
        }

        val version = try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            pi.versionName ?: ""
        } catch (e: Exception) {
            ""
        }

        ParidroidGP.init(parisize)

        setContent {
            MaterialTheme {
                PariScreen(
                    viewModel = viewModel,
                    versionName = version,
                    onOpenSettings = {
                        val intent = Intent(this, PariSettingsActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
