package com.facebook.litho

import com.facebook.litho.Component.ContainerBuilder

val ContainerBuilder<*>.innerContext: ComponentContext
    get() = when (this) {
        is Row.Builder -> (this as Row.Builder).mContext
        is Column.Builder -> (this as Column.Builder).mContext
        else -> TODO()
    }