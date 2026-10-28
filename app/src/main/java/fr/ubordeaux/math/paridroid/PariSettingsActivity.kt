package fr.ubordeaux.math.paridroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PariSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, PariSettingsFragment())
            .commit()
    }
}
