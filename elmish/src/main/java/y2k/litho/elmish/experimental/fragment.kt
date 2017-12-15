package y2k.litho.elmish.experimental

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView

class LCFragment : Fragment() {

    private lateinit var handler: LifecycleHandler<*, *>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        @Suppress("UNCHECKED_CAST")
        val functions = arguments.getSerializable("f") as Class<ElmFunctions<*, *>>
        handler = LifecycleHandler(functions.newInstance(), activity.applicationContext)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = ComponentContext(MyContext(activity, handler))
        val component = ElmishApplication.create(context).build()
        return LithoView.create(context, component)
    }
}