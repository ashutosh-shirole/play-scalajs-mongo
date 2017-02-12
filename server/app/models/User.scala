package models

import java.util.UUID

import com.mohiva.play.silhouette.api.Identity
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import play.api.libs.json.Json

case class User(
    id: Option[UUID],
    email: String,
    emailConfirmed: Boolean,
    password: String,
    nick: String,
    firstName: String,
    lastName: String,
    services: List[String]
) extends Identity {
  def key = email
  def fullName: String = firstName + " " + lastName
}

object User {
  val services = Seq("serviceA", "serviceB", "serviceC")
  implicit val jsonFormat = Json.format[User]
  val users = List(
    User(Some(UUID.fromString("3f145a02-7e5d-11e6-ae22-56b6b6499611")), "master@myweb.com", true, (new BCryptPasswordHasher()).hash("123123").password, "Eddy", "Eddard", "Stark", List("master")),
    User(Some(UUID.fromString("3f145c82-7e5d-11e6-ae22-56b6b6499611")), "a@myweb.com", true, (new BCryptPasswordHasher()).hash("123123").password, "Maggy", "Margaery", "Tyrell", List("serviceA")),
    User(Some(UUID.fromString("3f145d86-7e5d-11e6-ae22-56b6b6499611")), "b@myweb.com", true, (new BCryptPasswordHasher()).hash("123123").password, "Petyr", "Petyr", "Baelish", List("serviceB")),
    User(Some(UUID.fromString("3f145e62-7e5d-11e6-ae22-56b6b6499611")), "a_b@myweb.com", true, (new BCryptPasswordHasher()).hash("123123").password, "Tyry", "Tyrion", "Lannister", List("serviceA", "serviceB"))
  )
}