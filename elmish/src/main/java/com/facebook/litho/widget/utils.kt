package com.facebook.litho.widget

import com.facebook.litho.ComponentContext

val Text.Builder.innerContext: ComponentContext
    get() = mContext

val EditText.Builder.innerContext: ComponentContext
    get() = mContext