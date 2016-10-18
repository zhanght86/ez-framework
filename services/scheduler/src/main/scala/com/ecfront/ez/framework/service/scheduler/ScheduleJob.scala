package com.ecfront.ez.framework.service.scheduler

import com.ecfront.common.Resp
import com.ecfront.ez.framework.core.logger.Logging

/**
  * 调度器回调基类，所有调度作业回调处理都要继承此类
  */
trait ScheduleJob extends Logging{

  /**
    * 调度器回调时的执行方法
    *
    * @param scheduler 调度信息
    * @return 执行结果
    */
  def execute(scheduler: EZ_Scheduler): Resp[Void]

}
