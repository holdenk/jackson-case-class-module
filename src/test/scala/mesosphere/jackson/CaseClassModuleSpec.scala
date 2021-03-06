package mesosphere.jackson

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.{ Integer => JInt, Double => JDouble }

object CaseClassModuleSpec {
  case class Person(name: String, age: JInt)
  case class Defaults(x: JDouble = math.E, y: JDouble = math.Pi, z: String = "foobar")
  case class ComplexDefaults(xs: Seq[JInt] = Seq(1, 2, 3))
  case class NestedDefaults(defaults: ComplexDefaults = ComplexDefaults(Seq(5)))
  case class WithOption(n: Option[JInt])
}

class CaseClassModuleSpec extends Spec with JacksonHelpers {

  import CaseClassModuleSpec._

  val module = new DefaultScalaModule with CaseClassModule

  "CaseClassModule" should "deserialize basic case classes" in {
    deserialize[Person]("""{"name": "Jaime", "age": 17}""") should equal (Person("Jaime", 17))
  }

  it should "respect default values for basic case classes" in {
    deserialize[Defaults]("{}") should equal (Defaults(math.E, math.Pi, "foobar"))
  }

  it should "respect default values for complex case classes" in {
    deserialize[ComplexDefaults]("{}") should equal (ComplexDefaults(Seq(1, 2, 3)))
  }

  it should "respect default values for nested case classes" in {
    deserialize[NestedDefaults]("{}") should equal (NestedDefaults(ComplexDefaults(Seq(5))))
    deserialize[NestedDefaults]("""{"defaults": {} }""") should equal (NestedDefaults(ComplexDefaults(Seq(1, 2, 3))))
  }

  it should "be liberal in its interpretation of numbers" in {
    deserialize[Defaults]("""{ "x": "5.0" }""") should equal (Defaults(x = 5.0))
  }

  it should "read optional values" in {
    deserialize[WithOption]("""{ "n": 5 }""") should equal (WithOption(Some(5)))
    deserialize[WithOption]("""{ "n": "5" }""") should equal (WithOption(Some(5)))
    deserialize[WithOption]("""{}""") should equal (WithOption(None))
  }

  it should "read optional from example" in {
    case class Person(name: String, age: Integer = 30)
    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.registerModule(CaseClassModule)
    val readResult = mapper.readValue(
      """{ "name": "Alfonso" }""",
      classOf[Person]
    )
    readResult should equal (Person("Alfonso"))

  }

}
