package org.jsonwrapper

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

trait Writer[A] {
  def writeIt(a: A): String
}

trait Parser[A] {
  def parseIt(o: String): A
}

trait Formatter[A] extends Writer[A] with Parser[A]

case class WrappedValue[A, M](data: A, meta: M)

class JsonWrapper[M](meta: () => M, metaW: Formatter[M]) {

  def wrap[A](d: A)(implicit writer: Writer[A]): String = JsonWrapper.wrap(meta, metaW)(d)

  def unwrap[A](s: String)(implicit parser: Parser[A]): WrappedValue[A, M] = JsonWrapper.unwrap[A, M](metaW)(s)

}

object JsonWrapper {

  def wrap[A, M](meta: () => M, metaW: Writer[M])(data: A)(implicit dataW: Writer[A]): String = {
    val metadata: JObject = write(meta())(metaW)

    val json: JObject = "data" -> write(data)

    val merged = metadata merge json

    pretty(render(merged))
  }

  def unwrap[A, M](metaP: Parser[M])(full: String)(implicit dataP: Parser[A]): WrappedValue[A, M] = {
    val value: JValue = parse(full)

    val payload = value \ "data"

    val metadata = value.removeField {
      case JField("data", _) => true
      case _ => false
    }

    new WrappedValue(read[A](payload), read[M](metadata)(metaP))
  }

  private def read[A](value: JValue)(implicit parser: Parser[A]): A = parser.parseIt(pretty(render(value)))

  private def write[A](value: A)(implicit writer: Writer[A]): JObject = parse(writer.writeIt(value)).asInstanceOf[JObject]

}
