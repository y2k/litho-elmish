package com.facebook.litho

import com.facebook.litho.ComponentLayout.ContainerBuilder

val ContainerBuilder.innerContext: ComponentContext
    get() = when (this) {
        is InternalNode -> context
        else -> TODO()
    }