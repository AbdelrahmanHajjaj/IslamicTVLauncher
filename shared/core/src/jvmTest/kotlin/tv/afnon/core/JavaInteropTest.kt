package tv.afnon.core

import kotlin.test.Test

class JavaInteropTest {
    @Test
    fun existingJavaCallersCanUseTheCore() = JavaInterop.check()
}
