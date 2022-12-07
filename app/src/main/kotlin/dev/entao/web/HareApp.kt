package dev.entao.web

import dev.entao.hare.IndexPage
import dev.entao.web.bootpage.BootPage
import dev.entao.web.core.BaseApp
import dev.entao.web.core.HttpService
import dev.entao.web.core.ResController
import dev.entao.web.core.controllers.KeyLabelController
import dev.entao.web.core.controllers.UploadController
import dev.entao.web.core.slices.IPRecordSlice
import dev.entao.web.core.slices.LoginCheckSlice
import dev.entao.web.core.slices.TickSlice
import dev.entao.web.core.slices.TimerProvider
import dev.entao.web.log.logd
import dev.entao.web.sql.ConnPick
import dev.entao.web.sql.addMySQL
import dev.entao.web.sql.addSourceMySQL
import jdk.internal.org.jline.utils.Colors.s

class HareApp(httpService: HttpService) : BaseApp(httpService) {

    override val appName: String = "Hare管理后台"
    private val timerProvider: TimerProvider = TimerProvider()

    override fun onCreate() {
        super.onCreate()
        ConnPick.addMySQL("test", "test", "test", "192.168.3.100", 3306)

        httpService.apply {
            addController(ResController::class)
            addController(UploadController::class)
            addController(KeyLabelController::class)
            addController(IndexPage::class)
        }
        timerProvider.onMinute { m ->
            println("On Timer Minute:$m ")
        }
        addSliceList(TickSlice(this), IPRecordSlice(this), LoginCheckSlice(this))

        ResController.accept(BootPage.bootAcceptor)

    }

    override fun onDestroy() {
        timerProvider.destroy()
        super.onDestroy()
        ConnPick.clean()
    }


}