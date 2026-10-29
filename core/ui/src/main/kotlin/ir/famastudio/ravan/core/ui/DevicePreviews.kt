package ir.famastudio.ravan.core.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "phone-light", device = "spec:width=411dp,height=891dp", locale = "fa")
@Preview(
    name = "phone-dark",
    device = "spec:width=411dp,height=891dp",
    locale = "fa",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(name = "tablet", device = "spec:width=1280dp,height=800dp,dpi=240", locale = "fa")
annotation class DevicePreviews
