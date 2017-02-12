package daos

import javax.inject.Inject

import models.{MailToken, MailTokenUser}
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json._
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
 * @author : ashutosh
 *         19/09/2016
 */
trait MailTokenUserDAO {
  def findById(id: String): Future[Option[MailTokenUser]]
  def save(token: MailTokenUser): Future[MailTokenUser]
  def delete(id: String): Unit
}

/**
 * @author : ashutosh
 *         19/09/2016
 */
class MailTokenUserDAOImpl @Inject() (reactiveMongoApi: ReactiveMongoApi) extends BaseDAO[MailTokenUser](MailTokenUser.jsonFormat) with MailTokenUserDAO {
  override protected def getCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("mailTokenUser"))

  def findById(id: String): Future[Option[MailTokenUser]] = {
    getCollection.flatMap(_.find(Json.obj("id" -> id)).one[MailTokenUser])
  }

  def save(token: MailTokenUser): Future[MailTokenUser] = {
    onSuccess(getCollection.flatMap(_.insert(token)), token)
  }

  def delete(id: String): Unit = getCollection.flatMap(_.remove(Json.obj("id" -> id)))
}
