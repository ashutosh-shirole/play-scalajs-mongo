package services

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import daos.UserDAO
import models.User

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 */
class UserServiceImpl @Inject() (userDAO: UserDAO) extends UserService {

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = user.id match {
    case Some(id) => userDAO.find(id).flatMap {
      case Some(euser) => // Update user with profile
        userDAO.save(euser.copy(
          firstName = user.firstName,
          lastName = user.lastName,
          email = user.email
        ))
      case None => // Insert a new user
        userDAO.save(user)
    }
    case None => userDAO.save(user)
  }

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile) = {
    userDAO.find(profile.loginInfo).flatMap {
      case Some(user) => // Update user with profile
        userDAO.save(user.copy(
          firstName = profile.firstName.get,
          lastName = profile.lastName.get,
          email = profile.email.get
        ))
      case None => // Insert a new user
        userDAO.save(User(
          id = Some(UUID.randomUUID()),
          email = profile.email.get,
          emailConfirmed = false,
          password = "",
          nick = "",
          firstName = profile.firstName.get,
          lastName = profile.lastName.get,
          services = List("master")
        ))
    }
  }
}
