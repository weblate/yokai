package yokai.util.lang

import android.content.Context
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc

fun Context.getMString(stringRes: StringResource): String = StringDesc.Resource(stringRes).toString(this)
