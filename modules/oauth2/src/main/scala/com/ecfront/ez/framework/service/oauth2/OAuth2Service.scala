package com.ecfront.ez.framework.service.oauth2

import java.util.UUID

import com.ecfront.common.Resp
import com.ecfront.ez.framework.service.auth._
import com.ecfront.ez.framework.service.auth.model.{EZ_Account, EZ_Role}
import com.ecfront.ez.framework.service.rpc.foundation.{GET, RPC, RespRedirect}
import com.ecfront.ez.framework.service.rpc.http.HTTP
import com.ecfront.ez.framework.service.storage.foundation.BaseModel
import io.vertx.core.json.JsonObject

import scala.collection.JavaConversions._

@RPC("/public/oauth2/")
@HTTP
object OAuth2Service {

  @GET("code/:app/")
  def login(parameter: Map[String, String], context: EZAuthContext): Resp[RespRedirect] = {
    val appName = parameter("app")
    val processor = getAppProcessor(appName)
    if (processor != null) {
      Resp.success(RespRedirect(processor.fetchCodeUrl))
    } else {
      Resp.badRequest(s"App name [$appName] not found.")
    }
  }

  @GET("callback/:app/")
  def callback(parameter: Map[String, String], context: EZAuthContext): Resp[RespRedirect] = {
    val appName = parameter("app")
    val processor = getAppProcessor(appName)
    if (processor != null) {
      val getTokenR = processor.fetchAccessToken(parameter("code"), parameter("state"))
      if (getTokenR) {
        val oauthAccountR = processor.fetchAccount(getTokenR.body)
        if (oauthAccountR) {
          val oauthAccount = oauthAccountR.body
          val accountR = EZ_Account.getByOAuth(appName, oauthAccount.oauth(appName), "")
          if (accountR.body != null) {
            if (accountR.body.enable) {
              val loginInfo = CacheManager.addTokenInfo(accountR.body)
              Resp.success(RespRedirect(ServiceAdapter.successUrl + "?" + AuthService.VIEW_TOKEN_FLAG + "=" + loginInfo.body.token))
            } else {
              Resp.badRequest(s"Account 【${accountR.body.name}】disabled")
            }
          } else {
            oauthAccount.login_id = oauthAccount.oauth(appName) + "." + appName
            oauthAccount.email = oauthAccount.oauth(appName) + "." + appName + EZ_Account.VIRTUAL_EMAIL
            oauthAccount.password = UUID.randomUUID().toString
            oauthAccount.organization_code = com.ecfront.ez.framework.service.auth.ServiceAdapter.defaultOrganizationCode
            oauthAccount.role_codes = List(oauthAccount.organization_code + BaseModel.SPLIT + EZ_Role.USER_ROLE_FLAG)
            oauthAccount.enable = true
            val loginInfo = CacheManager.addTokenInfo(EZ_Account.save(oauthAccount).body)
            Resp.success(RespRedirect(ServiceAdapter.successUrl + "?" + AuthService.VIEW_TOKEN_FLAG + "=" + loginInfo.body.token))
          }
        } else {
          oauthAccountR
        }
      } else {
        getTokenR
      }
    } else {
      Resp.badRequest(s"App name [$appName] not found.")
    }
  }

  def init(oauth2Config: JsonObject): Unit = {
    oauth2Config.getJsonObject("platform").iterator().foreach {
      item =>
        getAppProcessor(item.getKey).init(item.getValue.asInstanceOf[JsonObject])
    }
  }

  private def getAppProcessor(app: String): AppProcessor = {
    app match {
      case "weixin_mp" =>
        WeixinMPProcessor
      case _ =>
        null
    }
  }

}