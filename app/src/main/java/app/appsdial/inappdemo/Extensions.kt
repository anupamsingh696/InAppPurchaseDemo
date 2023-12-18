package app.appsdial.inappdemo

import android.app.Activity
import android.util.Log


internal fun Activity.logger(message: String) {
    Log.d(this.localClassName, message)
}

internal fun Activity.logger(message: Int) {
    Log.d(this.localClassName, message.toString())
}
