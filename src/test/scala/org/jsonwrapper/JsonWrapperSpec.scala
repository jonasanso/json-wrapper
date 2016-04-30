package org.jsonwrapper

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.scalatest.{FlatSpec, Matchers}

class JsonWrapperSpec extends FlatSpec with Matchers {

  case class Data(id: Long, name: String)

  implicit val formatter: Formatter[Data] = new Formatter[Data] {
    override def writeIt(d: Data): String = pretty(render(("id" -> d.id) ~ ("name" -> d.name)))

    override def parseIt(s: String): Data = {
      val o = parse(s).asInstanceOf[JObject]
      Data(o.values("id").toString.toLong, o.values("name").toString)
    }
  }

  case class Metadata(time: String)

  val metaFormatter: Formatter[Metadata] = new Formatter[Metadata] {
    override def writeIt(m: Metadata): String = s"""{\"time\" : \"${m.time}\"}"""

    override def parseIt(s: String): Metadata = {
      Metadata(parse(s).asInstanceOf[JObject].values("time").toString)
    }
  }

  val wrapper = new JsonWrapper(() => Metadata("2010-04-13 11:25:00Z"), metaFormatter)

  "JsonWrapper" should "wrap" in {
    val actual = wrapper.wrap(Data(1L, "example"))
    (parse(actual) \ "time") should ===(JString("2010-04-13 11:25:00Z"))
    (parse(actual) \ "data") should ===(("id" -> 1L) ~ ("name" -> "example"))
  }

  "JsonWrapper" should "unwrap" in {
    val WrappedValue(data, meta) = wrapper.unwrap(
      """{
        |  "time":"2010-04-13 11:25:00Z",
        |  "data":{
        |    "id":1,
        |    "name":"file"
        |  }
        |}""".stripMargin)

    data should ===(Data(1L, "file"))
    meta should ===(Metadata("2010-04-13 11:25:00Z"))
  }
}
