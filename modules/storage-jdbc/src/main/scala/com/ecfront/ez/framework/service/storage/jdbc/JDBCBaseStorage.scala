package com.ecfront.ez.framework.service.storage.jdbc

import com.ecfront.common.{BeanHelper, Resp}
import com.ecfront.ez.framework.service.storage.foundation.{BaseModel, BaseStorage, EZStorageContext, Page}

/**
  * JDBC基础持久化实现
  *
  * @tparam M 实体类型
  */
trait JDBCBaseStorage[M <: BaseModel] extends BaseStorage[M] {

  protected val _entityInfo =
    if (JDBCEntityContainer.CONTAINER.contains(tableName)) {
      JDBCEntityContainer.CONTAINER(tableName)
    } else {
      JDBCEntityContainer.buildingEntityInfo(_modelClazz, tableName)
      JDBCEntityContainer.CONTAINER(tableName)
    }

  override def doSave(model: M, context: EZStorageContext): Resp[M] = {
    val requireResp = storageCheck(model, _entityInfo)
    if (requireResp) {
      JDBCExecutor.save(_entityInfo, getMapValue(model).filter(_._2 != null), _modelClazz)
    } else {
      requireResp
    }
  }

  override def doUpdate(model: M, context: EZStorageContext): Resp[M] = {
    val requireResp = storageCheck(model, _entityInfo)
    if (requireResp) {
      JDBCExecutor.update(_entityInfo, getIdValue(model), getMapValue(model).filter(_._2 != null), _modelClazz)
    } else {
      requireResp
    }
  }

  override def doSaveOrUpdate(model: M, context: EZStorageContext): Resp[M] = {
    val requireResp = storageCheck(model, _entityInfo)
    if (requireResp) {
      JDBCExecutor.saveOrUpdate(_entityInfo, getIdValue(model), getMapValue(model).filter(_._2 != null), _modelClazz)
    } else {
      requireResp
    }
  }

  override def doDeleteById(id: Any, context: EZStorageContext): Resp[Void] = {
    JDBCProcessor.update(
      s"DELETE FROM $tableName WHERE ${_entityInfo.idFieldName} = ? ",
      List(id)
    )
  }

  override def doGetById(id: Any, context: EZStorageContext): Resp[M] = {
    JDBCProcessor.get(
      s"SELECT * FROM $tableName WHERE ${_entityInfo.idFieldName}  = ? ",
      List(id),
      _modelClazz
    )
  }

  override def doUpdateByCond(newValues: String, condition: String, parameters: List[Any], context: EZStorageContext): Resp[Void] = {
    JDBCProcessor.update(
      s"UPDATE $tableName Set $newValues WHERE $condition",
      parameters
    )
  }

  override def doDeleteByCond(condition: String, parameters: List[Any], context: EZStorageContext): Resp[Void] = {
    JDBCProcessor.update(
      s"DELETE FROM $tableName WHERE $condition ",
      parameters
    )
  }

  override def doGetByCond(condition: String, parameters: List[Any], context: EZStorageContext): Resp[M] = {
    JDBCProcessor.get(
      s"SELECT * FROM $tableName WHERE $condition ",
      parameters,
      _modelClazz
    )
  }

  override def doExistById(id: Any, context: EZStorageContext): Resp[Boolean] = {
    JDBCProcessor.exist(
      s"SELECT 1 FROM $tableName WHERE ${_entityInfo.idFieldName}  = ? ",
      List(id)
    )
  }

  override def doExistByCond(condition: String, parameters: List[Any], context: EZStorageContext): Resp[Boolean] = {
    JDBCProcessor.exist(
      s"SELECT 1 FROM $tableName WHERE $condition ",
      parameters
    )
  }

  override def doFind(condition: String, parameters: List[Any], context: EZStorageContext): Resp[List[M]] = {
    JDBCProcessor.find(
      s"SELECT * FROM $tableName WHERE $condition ",
      parameters,
      _modelClazz
    )
  }

  override def doPage(condition: String, parameters: List[Any], pageNumber: Long, pageSize: Int, context: EZStorageContext): Resp[Page[M]] = {
    JDBCProcessor.page(
      s"SELECT * FROM $tableName WHERE $condition ",
      parameters,
      pageNumber, pageSize,
      _modelClazz
    )
  }

  override def doCount(condition: String, parameters: List[Any], context: EZStorageContext): Resp[Long] = {
    JDBCProcessor.count(
      s"SELECT count(1) FROM $tableName WHERE $condition ",
      parameters
    )
  }

  protected def getMapValue(model: BaseModel): Map[String, Any] = {
    // 获取对象要持久化字段的值，忽略为null的id字段（由seq控制）
    BeanHelper.findValues(model, _entityInfo.ignoreFieldNames)
      .filterNot(item => item._1 == _entityInfo.idFieldName && (item._2 == null || item._2.toString.trim == ""))
  }

  protected def getIdValue(model: BaseModel): Any = {
    if (_entityInfo.idFieldName == BaseModel.Id_FLAG) {
      model.id
    } else {
      getValueByField(model, _entityInfo.idFieldName)
    }
  }

  protected def getValueByField(model: AnyRef, fieldName: String): Any = {
    BeanHelper.getValue(model, fieldName).orNull
  }

  protected def setValueByField(model: AnyRef, fieldName: String, value: Any): Unit = {
    BeanHelper.setValue(model, fieldName, value)
  }

}
