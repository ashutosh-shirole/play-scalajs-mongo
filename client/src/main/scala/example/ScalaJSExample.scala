package example

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import models.User
import org.scalajs.dom.document
import org.scalajs.dom.ext.{Ajax, LocalStorage}
import org.scalajs.dom.raw.{Event, HTMLDivElement, Node}
import shared.SigninForm

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSON
import upickle.default._


object ScalaJSExample extends js.JSApp {
  implicit def makeIntellijHappy(x: scala.xml.Node): Binding[org.scalajs.dom.raw.Node] = ???

  /**
    * Ajax Request to server, updates data state with number
    * of requests to count.
    * @param data
    */
  def countRequest(data: Var[String]) = {
    val url = "http://localhost:8888/count"
    Ajax.get(url).onSuccess { case xhr =>
      data := JSON.parse(xhr.responseText).count.toString
    }
  }

  def isAuthenticated:Boolean = {
    LocalStorage("token") match {
      case Some(token) => true
      case None => false
    }
  }

  def login(signinForm:SigninForm) = {
    val url = "http://localhost:8888/authenticate"
    Ajax.post(url, write(signinForm)).onSuccess { case xhr =>
      LocalStorage("token") = JSON.parse(xhr.responseText).token.toString
    }
  }

  @dom
  def renderLogin:Binding[Node] = {
    var email:Var[String] = Var("");
    var password:Var[String] = Var("");
    <div>
      <div class="section"></div>
      <main>
        <center>
          <img class="responsive-img" style="width: 250px;" src="http://i.imgur.com/ax0NCsK.gif" />
          <div class="section"></div>

          <h5 class="indigo-text">Please, login into your account</h5>
          <div class="section"></div>

          <div class="container">
            <div class="z-depth-1 grey lighten-4 row" style="display: inline-block; padding: 32px 48px 0px 48px; border: 1px solid #EEE;">

              <form class="col s12" method="post">
                <div class='row'>
                  <div class='col s12'>
                  </div>
                </div>

                <div class='row'>
                  <div class='input-field col s12'>
                    <input class='validate' type='email' name='email' value ={email.bind}/>
                    <label for='email'>Enter your email</label>
                  </div>
                </div>

                <div class='row'>
                  <div class='input-field col s12'>
                    <input class='validate' type='password' name='password' value={password.bind} />
                    <label for='password'>Enter your password</label>
                  </div>
                  <label style='float: right;'>
                    <a class='pink-text' href='#!'><b>Forgot Password?</b></a>
                  </label>
                </div>

                <br />
                <center>
                  <div class='row'>
                    <button type='submit' onclick={} name='btn_login' class='col s12 btn btn-large waves-effect indigo'>Login</button>
                  </div>
                </center>
              </form>
            </div>
          </div>
          <a href="#!">Create account</a>
        </center>

        <div class="section"></div>
        <div class="section"></div>
      </main>
    </div>
  }

  @dom
  def render:Binding[Node] = {
    val data = Var("")
    countRequest(data) // initial population
    <div>
      <button onclick={event: Event => countRequest(data) }>
        Boop
      </button>
      From Play: The server has been booped { data.bind } times. Shared Message: {shared.SharedMessages.itWorks}.
    </div>
  }

  def main(): Unit = {
    dom.render(document.body, render)
  }
}
