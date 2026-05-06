import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sample.ResponseInterceptionTestScreen

@Composable
fun Screen() {
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "WebGL WebView",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            ResponseInterceptionTestScreen()
        }
    }
}
