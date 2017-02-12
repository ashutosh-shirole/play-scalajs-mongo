package models

import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.json.Json

trait MailToken {
  def id: String
  def email: String
  def expirationTime: DateTime
  def isExpired = expirationTime.isBeforeNow
}

case class MailTokenUser(
  id: String,
  email: String,
  expirationTime: DateTime,
  isSignUp: Boolean
) extends MailToken

object MailTokenUser {
  def apply(email: String, isSignUp: Boolean): MailTokenUser =
    MailTokenUser(UUID.randomUUID().toString, email, new DateTime().plusHours(24), isSignUp)

  implicit val jsonFormat = Json.format[MailTokenUser]
}