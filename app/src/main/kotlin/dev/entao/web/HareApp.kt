package dev.entao.web

import dev.entao.hare.IndexPage
import dev.entao.web.core.BaseApp
import dev.entao.web.core.HttpService
import dev.entao.web.core.ResController
import dev.entao.web.core.controllers.KeyLabelController
import dev.entao.web.core.controllers.UploadController
import dev.entao.web.core.slices.IPRecordSlice
import dev.entao.web.core.slices.LoginCheckSlice
import dev.entao.web.core.slices.TickSlice
import dev.entao.web.core.slices.TimerProvider
import dev.entao.web.sql.ConnPick
import dev.entao.web.sql.addSourceMySQL

class HareApp(httpService: HttpService) : BaseApp(httpService) {

    override val appName: String = "Hare管理后台"
    private val timerProvider: TimerProvider = TimerProvider()

    override fun onCreate() {
        super.onCreate()
        ConnPick.addSourceMySQL(
            "hare",
            "hare",
            "hare",
            "jdbc:mysql://127.0.0.1:3306/hare?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Hongkong"
        )

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

    }

    override fun onDestroy() {
        timerProvider.destroy()
        super.onDestroy()
        ConnPick.clean()
    }


}