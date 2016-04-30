Json wrapper is just a simple tool to wrap your models with metadata

JsonWrapper expects you to implement a very simple interface Formatter for the metadata and the data

Internally Json wrapper uses json4s but it does not impose any json library.
The drawback of this approach is extra steps inside the library for writing and parsing

You define your own metadata and your data and its formatters
```scala
  import org.jsonwrapper._

  // Metadata
  case class Metadata(time: String)

  val metaFormatter: Formatter[Metadata] = new Formatter[Metadata] {
    override def writeIt(m: Metadata): String = s"""{\"time\" : \"${m.time}\"}"""

    override def parseIt(s: String): Metadata = ??? // User your preferred Json library
  }

  def generateMetadata():Metadata = ???

  val wrapper = new JsonWrapper(generateMetadata, metaFormatter)


  // Data
  case class Data(id: Long, name: String)

  implicit val formatter: Formatter[Data] = new Formatter[Data] {
    override def writeIt(d: Data): String = ???

    override def parseIt(s: String): Data = ???
  }


  val string = wrapper.wrap(Data(1L, "example"))
  val WrappedValue(data, meta)  = wrapper.unwrap(string)

```
