package daos

import java.util.UUID

import play.api.Logger
import play.api.libs.json._
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.api.commands._
import reactivemongo.api.indexes.Index
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.errors.GenericDatabaseException
import reactivemongo.play.json.collection.JSONBatchCommands.JSONCountCommand
import reactivemongo.play.json.collection._
import reactivemongo.play.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author : ashutosh
  *         07/07/2016
  */
abstract class BaseDAO[T <: Any](domainFormat: Format[T]) {

  import ReactiveMongoFormats._
  import scala.concurrent.ExecutionContext.Implicits.global

  protected def getCollection: Future[JSONCollection]

  implicit val idFormatImplicit = objectIdFormats
  implicit val domainFormatsImplicit = domainFormat

  ensureIndexes(scala.concurrent.ExecutionContext.Implicits.global)

  def findByUUID(uuid: UUID): Future[Option[T]] = {
    getCollection.flatMap(_.find(Json.obj("uuid" -> uuid)).one[T])
  }

  def findByID(id: BSONObjectID): Future[Option[T]] = {
    getCollection.flatMap(_.find(Json.obj("_id" -> id)).one[T])
  }

  def findAll(readPreference: ReadPreference = ReadPreference.primaryPreferred): Future[List[T]] = {
    getCollection.flatMap(_.find(Json.obj()).cursor[T](readPreference).collect[List](-1,Cursor.FailOnError[List[T]]()))
  }

  def countAll: Future[Int] = getCollection.flatMap(col => col.runCommand(JSONCountCommand.Count(Json.obj()),ReadPreference.primaryPreferred).map(_.count))

  def updateById(id: BSONObjectID, entity: T): Future[T] = {
    domainFormat.writes(entity) match {
      case d @ JsObject(_) => onSuccess(getCollection.flatMap(_.update(Json.obj("_id" -> id), d, writeConcern = WriteConcern.Default)), entity)
      case _ =>
        Future.failed[T](new Exception("cannot write object"))
    }
  }

  def insert(entity: T):Future[T] = {
    domainFormat.writes(entity) match {
      case d @ JsObject(_) => onSuccess(getCollection.flatMap(_.insert(d)), entity)
      case _ =>
        Future.failed[T](new Exception("cannot write object"))
    }
  }


  /**
    * Returns some result on success and None on error.
    *
    * @param result The last result.
    * @param entity The entity to return.
    * @return The entity on success or an exception on error.
    */
  protected def onSuccess(result: Future[WriteResult], entity: T): Future[T] = result.recoverWith {
    case e =>
      Logger.error("Error from MongoDB",e)
      Future.failed(new MongoException("Got exception from MongoDB", e.getCause))
  }.map { r =>
    WriteResult.lastError(r) match {
      case Some(e) => throw new MongoException(e.message, e)
      case _ => entity
    }
  }

  /**
    * Returns the number of updated documents on success and None on error.
    *
    * @param result The last result.
    * @return The number of updated documents on success or an exception on error.
    */
  protected def updated(result: Future[WriteResult]): Future[Int] = result.recoverWith {
    case e => Future.failed(new MongoException("Got exception from MongoDB", e.getCause))
  }.map { r =>
    WriteResult.lastError(r) match {
      case Some(e) => throw new MongoException(e.message, e)
      case _ => r.n
    }
  }

  private val DuplicateKeyError = "E11000"
  val message: String = "Failed to ensure index"
  def indexes: Seq[Index] = Seq.empty

  private def ensureIndex(index: Index)(implicit ec: ExecutionContext): Future[Boolean] = {
    getCollection.flatMap(_.indexesManager.create(index).map(wr => {
      if (!wr.ok) {
        val maybeMesg = wr match {
          case WriteResult.Message(msg) => if (msg.contains(DuplicateKeyError)) {
            // this is for backwards compatibility to mongodb 2.6.x
            throw new GenericDatabaseException(msg, wr.code)
          } else Some(msg)
          case _ => Some("")
        }
        Logger.error(s"$message : '${maybeMesg.map(_.toString)}'")
      }
      wr.ok
    }).recover {
      case t =>
        Logger.error(message, t)
        false
    })
  }

  def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = {
    Future.sequence(indexes.map(ensureIndex(_)))
  }

}

class MongoException(msg: String, cause: Throwable = null) extends Exception(msg, cause)