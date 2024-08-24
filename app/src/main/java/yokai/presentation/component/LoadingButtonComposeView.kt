package yokai.presentation.component

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import eu.kanade.tachiyomi.R
import yokai.presentation.theme.YokaiTheme

class LoadingButtonComposeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    var text by mutableStateOf("placeholder")
    private var onClick: () -> Unit = {}
    private var isLoading by mutableStateOf(false)

    init {
        layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
        attrs?.let {
            val arr = context.obtainStyledAttributes(it, R.styleable.LoadingButtonComposeView)
            text = context.getString(arr.getResourceId(R.styleable.LoadingButtonComposeView_android_text, R.string.log_in))
        }
    }

    fun setOnClick(onClick: () -> Unit) {
        this.onClick = onClick
    }

    fun startAnimation() {
        isLoading = true
    }

    fun revertAnimation(after: () -> Unit = {}) {
        isLoading = false
        after()
    }

    @Composable
    override fun Content() {
        YokaiTheme {
            LoadingButton(
                modifier = Modifier.fillMaxWidth(),
                text = { text },
                loading = { isLoading },
                onClick = onClick,
            )
        }
    }
}
