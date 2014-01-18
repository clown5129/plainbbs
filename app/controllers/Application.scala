package controllers

import play.api._
import play.api.cache._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import play.api.i18n.Messages
import models.PlainBbsTables._
import views.html.thread

case class BbsThreadData(title: String, content: String)

case class BbsPostData(subject: String, content: String)

object Application extends Controller {

  val createForm = Form {
    mapping(
      "title" -> nonEmptyText,
      "content" -> nonEmptyText)(BbsThreadData.apply)(BbsThreadData.unapply)
  }

  val postForm = Form {
    mapping(
      "subject" -> nonEmptyText,
      "content" -> nonEmptyText)(BbsPostData.apply)(BbsPostData.unapply)
  }

  def index = Action { implicit request =>
    Ok(views.html.index(getAllThreads, createForm))
  }

  def create = Action { implicit request =>
    createForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.index(getAllThreads, formWithErrors)),
      threadData => {
        val id = createThread(threadData)
        Redirect(routes.Application.view(id))
      })
  }
  
  def view(id: Int) = Action {
    findThread(id) map {
      thread =>
        Ok(views.html.thread(
          thread,
          getPosts(id),
          postForm.fill(BbsPostData("NO SUBJECT", ""))))
    } getOrElse {
      Redirect(routes.Application.index).flashing("error" -> Messages(MessageId.ERROR_THREAD_NOTFOUND))
    }
  }

  def post(id: Int) = Action { implicit request =>
    postForm.bindFromRequest.fold(
      formWithErrors => findThread(id) map {
        thread => Ok(views.html.thread(thread, getPosts(id), formWithErrors))
      } getOrElse {
        Redirect(routes.Application.index).flashing("error" -> Messages(MessageId.ERROR_THREAD_NOTFOUND))
      },
      postData => {
        postToThread(id, postData)
        Redirect(routes.Application.view(id))
      })
  }

}

object MessageId {
  val ERROR_THREAD_NOTFOUND = "error.thread.notfound"
}