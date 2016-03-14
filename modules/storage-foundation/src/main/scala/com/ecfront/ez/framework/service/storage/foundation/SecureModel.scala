package com.ecfront.ez.framework.service.storage.foundation

import java.util.Date

import com.ecfront.common.Resp
import com.ecfront.ez.framework.core.helper.TimeHelper

import scala.beans.BeanProperty

/**
  * 带操作信息的实体基类
  * 默认情况下操作信息会根据上下文自动注入
  */
trait SecureModel extends BaseModel {

  @Index
  @BeanProperty var create_user: String = _
  @Index
  @BeanProperty var create_org: String = _
  @Index
  @BeanProperty var create_time: Long = _
  @Index
  @BeanProperty var update_user: String = _
  @Index
  @BeanProperty var update_time: Long = _
  @Index
  @BeanProperty var update_org: String = _


}

object SecureModel {

  val SYSTEM_USER_FLAG = "system"

  val CREATE_USER_FLAG = "create_user"
  val CREATE_ORG_FLAG = "create_org"
  val CREATE_TIME_FLAG = "create_time"
  val UPDATE_USER_FLAG = "update_user"
  val UPDATE_ORG_FLAG = "update_org"
  val UPDATE_TIME_FLAG = "update_time"

}

trait SecureStorage[M <: SecureModel] extends BaseStorage[M] {

  override protected def preSave(model: M, context: EZStorageContext): Resp[M] = {
    wrapSecureSave(model, context)
    super.preSave(model, context)
  }

  override protected def preUpdate(model: M, context: EZStorageContext): Resp[M] = {
    wrapSecureUpdate(model, context)
    super.preUpdate(model, context)
  }

  override protected def preSaveOrUpdate(model: M, context: EZStorageContext): Resp[M] = {
    wrapSecureSave(model, context)
    super.preSaveOrUpdate(model, context)
  }

  /**
    * 注入操作信息
    *
    * @param model   实体对象
    * @param context 上下文
    */
  private def wrapSecureSave(model: M, context: EZStorageContext = EZStorageContext()): Unit = {
    val now = TimeHelper.msf.format(new Date()).toLong
    if (model.create_user == null) {
      model.create_user = context.optAccount
    }
    if (model.create_time == 0) {
      model.create_time = now
    }
    if (model.create_org == null) {
      model.create_org = context.optOrganization
    }
    if (model.update_user == null) {
      model.update_user = context.optAccount
    }
    if (model.update_time == 0) {
      model.update_time = now
    }
    if (model.update_org == null) {
      model.update_org = context.optOrganization
    }
  }

  /**
    * 注入操作信息
    *
    * @param model   实体对象
    * @param context 上下文
    */
  private def wrapSecureUpdate(model: M, context: EZStorageContext = EZStorageContext()): Unit = {
    val now = TimeHelper.msf.format(new Date()).toLong
    if (model.update_user == null) {
      model.update_user = context.optAccount
    }
    if (model.update_time == 0) {
      model.update_time = now
    }
    if (model.update_org == null) {
      model.update_org = context.optOrganization
    }
  }

}

