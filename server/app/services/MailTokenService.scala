package services

import javax.inject.Inject

import daos.MailTokenUserDAO
import models.{MailToken, MailTokenUser}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MailTokenService[T <: MailToken] {
  def create(token: T): Future[Option[T]]
  def retrieve(id: String): Future[Option[T]]
  def consume(id: String): Unit
}

class MailTokenUserService @Inject() (mailTokenUserDAO: MailTokenUserDAO) extends MailTokenService[MailTokenUser] {
  def create(token: MailTokenUser): Future[Option[MailTokenUser]] = {
    mailTokenUserDAO.save(token).map(Some(_))
  }
  def retrieve(id: String): Future[Option[MailTokenUser]] = {
    mailTokenUserDAO.findById(id)
  }
  def consume(id: String): Unit = {
    mailTokenUserDAO.delete(id)
  }
}