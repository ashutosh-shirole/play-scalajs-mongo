package daos

import play.api.data.validation.ValidationError
import play.api.libs.json._
import org.joda.time.{LocalDate, DateTimeZone, DateTime, LocalDateTime}
import reactivemongo.bson.BSONObjectID

object ReactiveMongoFormats extends ReactiveMongoFormats

trait ReactiveMongoFormats {

  implicit val localDateRead: Reads[LocalDate] =
    (__ \ "$date").read[Long].map { date => new LocalDate(date, DateTimeZone.UTC) }


  implicit val localDateWrite: Writes[LocalDate] = new Writes[LocalDate] {
    def writes(localDate: LocalDate): JsValue = Json.obj(
      "$date" -> localDate.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis
    )
  }

  implicit val localDateTimeRead: Reads[LocalDateTime] =
    (__ \ "$date").read[Long].map { dateTime => new LocalDateTime(dateTime, DateTimeZone.UTC) }


  implicit val localDateTimeWrite: Writes[LocalDateTime] = new Writes[LocalDateTime] {
    def writes(dateTime: LocalDateTime): JsValue = Json.obj(
      "$date" -> dateTime.toDateTime(DateTimeZone.UTC).getMillis
    )
  }

  implicit val dateTimeRead: Reads[DateTime] =
    (__ \ "$date").read[Long].map { dateTime =>
      new DateTime(dateTime, DateTimeZone.UTC)
    }


  implicit val dateTimeWrite: Writes[DateTime] = new Writes[DateTime] {
    def writes(dateTime: DateTime): JsValue = Json.obj(
      "$date" -> dateTime.getMillis
    )
  }

  implicit val objectIdRead: Reads[BSONObjectID] =
    (__ \ "$oid").read[String].map { oid =>
      BSONObjectID.parse(oid).get
    }


  implicit val objectIdWrite: Writes[BSONObjectID] = new Writes[BSONObjectID] {
    def writes(objectId: BSONObjectID): JsValue = Json.obj(
      "$oid" -> objectId.stringify
    )
  }

  implicit def tuple2Format[A, B](implicit a: Format[A], b: Format[B]): Format[Tuple2[A, B]] = new Format[Tuple2[A, B]] {
    def writes(tuple: Tuple2[A, B]) = JsArray(Seq(a.writes(tuple._1), b.writes(tuple._2)))
    def reads(json: JsValue): JsResult[Tuple2[A, B]] = json match {
      case JsArray(arr) if arr.size == 2 => for {
        av <- a.reads(arr(0))
        bv <- b.reads(arr(1))
      } yield (av,bv)
      case _ => JsError(Seq(JsPath() -> Seq(ValidationError("Expected array of three elements"))))
    }
  }

  implicit val objectIdFormats = Format(objectIdRead, objectIdWrite)
  implicit val dateTimeFormats = Format(dateTimeRead, dateTimeWrite)
  implicit val localDateFormats = Format(localDateRead, localDateWrite)
  implicit val localDateTimeFormats = Format(localDateTimeRead, localDateTimeWrite)


  def mongoEntity[A](baseFormat: Format[A]) : Format[A] = {
    import JsonExtensions._
    val publicIdPath: JsPath = JsPath \ '_id
    val privateIdPath: JsPath = JsPath \ 'id
    new Format[A] {
      def reads(json: JsValue): JsResult[A] = baseFormat.compose(copyKey(publicIdPath, privateIdPath)).reads(json)

      def writes(o: A): JsValue = baseFormat.transform(moveKey(privateIdPath,publicIdPath)).writes(o)
    }
  }
}

object JsonExtensions {

  import play.api.libs.json._

  def copyKey(fromPath: JsPath,toPath:JsPath ) = __.json.update(toPath.json.copyFrom(fromPath.json.pick))
  def moveKey(fromPath:JsPath, toPath:JsPath) =(json:JsValue)=> json.transform(copyKey(fromPath,toPath) andThen fromPath.json.prune).get
}