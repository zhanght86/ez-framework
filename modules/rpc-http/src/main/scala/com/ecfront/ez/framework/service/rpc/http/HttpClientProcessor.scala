package com.ecfront.ez.framework.service.rpc.http

import com.ecfront.common.JsonHelper
import com.typesafe.scalalogging.slf4j.LazyLogging
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.http._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}

/**
  * HTTP 请求操作
  *
  * 包含了对HTTP GET POST PUT DELETE 四类常用操作
  *
  */
object HttpClientProcessor extends LazyLogging {

  var httpClient: HttpClient = _

  /**
    * GET 请求
    *
    * @param url         请求URL
    * @param contentType 请求类型，默认为 application/json
    * @return 请求结果，string类型
    */
  def get(url: String, contentType: String = "application/json"): String = {
    Await.result(Async.get(url, contentType), Duration.Inf)
  }

  /**
    * POST 请求
    *
    * @param url         请求URL
    * @param body        请求体
    * @param contentType 请求类型，默认为 application/json
    * @return 请求结果，string类型
    */
  def post(url: String, body: Any, contentType: String = "application/json"): String = {
    Await.result(Async.post(url, body, contentType), Duration.Inf)
  }

  /**
    * PUT 请求
    *
    * @param url         请求URL
    * @param body        请求体
    * @param contentType 请求类型，默认为 application/json
    * @return 请求结果，string类型
    */
  def put(url: String, body: Any, contentType: String = "application/json"): String = {
    Await.result(Async.put(url, body, contentType), Duration.Inf)
  }

  /**
    * DELETE 请求
    *
    * @param url         请求URL
    * @param contentType 请求类型，默认为 application/json
    * @return 请求结果，string类型
    */
  def delete(url: String, contentType: String = "application/json"): String = {
    Await.result(Async.delete(url, contentType), Duration.Inf)
  }

  object Async {

    /**
      * GET 请求
      *
      * @param url         请求URL
      * @param contentType 请求类型，默认为 application/json
      * @return 请求结果，string类型
      */
    def get(url: String, contentType: String = "application/json"): Future[String] = {
      request(HttpMethod.GET, url, null, contentType)
    }

    /**
      * POST 请求
      *
      * @param url         请求URL
      * @param body        请求体
      * @param contentType 请求类型，默认为 application/json
      * @return 请求结果，string类型
      */
    def post(url: String, body: Any, contentType: String = "application/json"): Future[String] = {
      request(HttpMethod.POST, url, body, contentType)
    }

    /**
      * PUT 请求
      *
      * @param url         请求URL
      * @param body        请求体
      * @param contentType 请求类型，默认为 application/json
      * @return 请求结果，string类型
      */
    def put(url: String, body: Any, contentType: String = "application/json"): Future[String] = {
      request(HttpMethod.PUT, url, body, contentType)
    }

    /**
      * DELETE 请求
      *
      * @param url         请求URL
      * @param contentType 请求类型，默认为 application/json
      * @return 请求结果，string类型
      */
    def delete(url: String, contentType: String = "application/json"): Future[String] = {
      request(HttpMethod.DELETE, url, null, contentType)
    }

    private def request(method: HttpMethod, url: String, body: Any, contentType: String): Future[String] = {
      val p = Promise[String]()
      val client = httpClient.requestAbs(method, url, new Handler[HttpClientResponse] {
        override def handle(response: HttpClientResponse): Unit = {
          response.exceptionHandler(new Handler[Throwable] {
            override def handle(event: Throwable): Unit = {
              p.failure(event)
            }
          })
          response.bodyHandler(new Handler[Buffer] {
            override def handle(data: Buffer): Unit = {
              p.success(data.toString("UTF-8"))
            }
          })
        }
      }).putHeader("content-type", contentType)
      if (body != null) {
        contentType.toLowerCase match {
          case c if c == "application/x-www-form-urlencoded" && body.isInstanceOf[Map[_, _]] =>
            client.end(body.asInstanceOf[Map[String, String]].map(i => i._1 + "=" + i._2).mkString("&"))
          case _ =>
            client.end(JsonHelper.toJsonString(body))
        }
      } else {
        client.end()
      }
      p.future
    }

  }

}
