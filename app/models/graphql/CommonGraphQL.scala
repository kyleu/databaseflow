package models.graphql

import java.util.UUID

import sangria.schema._
import sangria.validation.ValueCoercionViolation

import scala.util.{Failure, Success, Try}

object CommonGraphQL {
  private[this] def badChars = Seq(" " -> "_", "." -> "_", "(" -> "", ")" -> "", "#" -> "", "!" -> "")

  def cleanName(s: String) = {
    val swapped = badChars.foldLeft(s)((l, r) => l.replaceAllLiterally(r._1, r._2))
    if (swapped.head.isLetter) { swapped } else { "_" + swapped }
  }

  case object ShortCoercionViolation extends ValueCoercionViolation("Short value expected in the range of a 16-bit number.")
  case object UuidCoercionViolation extends ValueCoercionViolation("UUID value expected in format [00000000-0000-0000-0000-000000000000].")
  case object Base64CoercionViolation extends ValueCoercionViolation("Base64-encoded value expected.")

  private[this] def parseUuid(s: String) = Try(UUID.fromString(s)) match {
    case Success(u) => Right(u)
    case Failure(_) => Left(UuidCoercionViolation)
  }

  implicit val shortType = ScalarType[Short](
    name = "Short",
    description = Some("A 16 bit number."),
    coerceOutput = (u, _) => u.toInt,
    coerceUserInput = {
      case i: Int if i <= Short.MaxValue && i >= Short.MinValue => Right(i.toShort)
      case _ => Left(ShortCoercionViolation)
    },
    coerceInput = {
      case sangria.ast.IntValue(i, _, _) => Right(i.toShort)
      case _ => Left(ShortCoercionViolation)
    }
  )

  implicit val uuidType = ScalarType[UUID](
    name = "UUID",
    description = Some("A string representing a UUID, in format [00000000-0000-0000-0000-000000000000]."),
    coerceOutput = (u, _) => u.toString,
    coerceUserInput = {
      case s: String => parseUuid(s)
      case _ => Left(UuidCoercionViolation)
    },
    coerceInput = {
      case sangria.ast.StringValue(s, _, _, _, _) => parseUuid(s)
      case _ => Left(UuidCoercionViolation)
    }
  )

  implicit val byteType = ScalarType[Byte](
    name = "Byte",
    description = Some("A single byte, expressed as an integer."),
    coerceOutput = (u, _) => u.toInt,
    coerceUserInput = {
      case i: Int => Right(i.toByte)
      case _ => Left(UuidCoercionViolation)
    },
    coerceInput = {
      case sangria.ast.IntValue(i, _, _) => Right(i.toByte)
      case _ => Left(UuidCoercionViolation)
    }
  )

  val idArg = Argument("id", OptionInputType(IntType), description = "Returns model matching the provided id.")
  val uuidArg = Argument("id", OptionInputType(uuidType), description = "Returns model matching the provided id.")
  val keyArg = Argument("key", StringType, description = "Returns the model matching provided key.")

  def deriveEnumeratumType[T <: enumeratum.EnumEntry](name: String, description: String, values: Seq[(T, String)]) = EnumType(
    name = name,
    description = Some(description),
    values = values.map(t => EnumValue(name = t._1.toString, value = t._1, description = Some(t._2))).toList
  )

  def deriveStringEnumeratumType[T <: enumeratum.values.StringEnumEntry](name: String, description: String, values: Seq[(T, String)]) = EnumType(
    name = name,
    description = Some(description),
    values = values.map(t => EnumValue(name = t._1.toString, value = t._1, description = Some(t._2))).toList
  )
}
