package com.rubenalba.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.rubenalba.myapplication.ui.vault.VaultScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    VaultScreen(
                        onAddClick = {
                            Toast.makeText(this, "Botón añadir pulsado", Toast.LENGTH_SHORT).show()
                        },
                        onItemClick = { id ->
                            Toast.makeText(this, "Click en ID: $id", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}