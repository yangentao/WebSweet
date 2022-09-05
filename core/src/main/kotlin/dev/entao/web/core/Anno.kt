package dev.entao.web.core

import dev.entao.web.base.isPublic
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions

/**
 * 通用注释
 * Created by yangentao on 2016/12/14.
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Controller(val index: Boolean = false, val rename: String = "")

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Action(val index: Boolean = false, val rename: String = "")


//TODO matches("*.jpg")
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MissAction


//登录的uri
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginWebAction

//登录的uri
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogoutAction

//当前登录账号用户信息的uri
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AccountInfoAction

//需要登录后请求, 管理员权限
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class HttpMethod(vararg val value: String)


val KClass<*>.missFunction: KFunction<*>? get() = this.memberFunctions.firstOrNull { it.isPublic && it.hasAnnotation<MissAction>() }




