package models

import java.sql.{ Array => _, _ }
import scala.slick.driver.PostgresDriver.simple._
import play.api.Play.current
import play.api.db.{ DB => PlayDB }

case class BbsThreadData(title: String, content: String)

case class BbsPostData(subject: String, content: String)

case class BbsThread(
  id: Int = -1,
  title: String,
  postCount: Int = 1,
  createdAt: Timestamp,
  lastPostedAt: Timestamp,
  deletedAt: Option[Timestamp] = None)

case class BbsPost(
  id: Int = -1,
  subject: String,
  postedAt: Timestamp,
  content: String,
  visible: Boolean = true,
  threadId: Int)

object PlainBbsTables {
  val db = Database.forDataSource(PlayDB.getDataSource("plainbbs"))

  class BbsThreads(tag: Tag) extends Table[BbsThread](tag, "bbs_threads") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def postCount = column[Int]("post_count")
    def createdAt = column[Timestamp]("created_at")
    def lastPostedAt = column[Timestamp]("last_posted_at")
    def deletedAt = column[Option[Timestamp]]("deleted_at")
    def * = (id, title, postCount, createdAt, lastPostedAt, deletedAt) <> (BbsThread.tupled, BbsThread.unapply)

  }

  val bbsThreads = TableQuery[BbsThreads]

  class BbsPosts(tag: Tag) extends Table[BbsPost](tag, "bbs_posts") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def subject = column[String]("subject")
    def postedAt = column[Timestamp]("posted_at")
    def content = column[String]("content")
    def visible = column[Boolean]("visible")
    def threadId = column[Int]("thread_id")
    def thread = foreignKey("thread_fk", threadId, bbsThreads)(_.id)
    def * = (id, subject, postedAt, content, visible, threadId) <> (BbsPost.tupled, BbsPost.unapply)
  }

  val bbsPosts = TableQuery[BbsPosts]

  def getAllThreads: List[BbsThread] = db withSession { implicit session =>
    bbsThreads.sortBy(r => (r.lastPostedAt.desc, r.id.asc)).list
  }

  def createThread(threadData: BbsThreadData): Int = db withTransaction { implicit session =>
    val ts = new Timestamp(System.currentTimeMillis())
    val threadId = (bbsThreads returning bbsThreads.map(_.id)) += BbsThread(
      title = threadData.title,
      createdAt = ts,
      lastPostedAt = ts)
    bbsPosts += BbsPost(
      subject = threadData.title,
      postedAt = ts,
      content = threadData.content,
      threadId = threadId)
    threadId
  }
  
  def postToThread(threadId: Int, postData: BbsPostData): Int = db withTransaction { implicit session =>
    val q = for { t <- bbsThreads if t.id === threadId } yield (t.postCount, t.lastPostedAt)
    q.firstOption.map {
      case (cnt, _) =>
        val ts = new Timestamp(System.currentTimeMillis());
        val postId = (bbsPosts returning bbsPosts.map(_.id)) += BbsPost(
          subject = postData.subject,
          postedAt = ts,
          content = postData.content,
          threadId = threadId)
        q.update(cnt + 1, ts)
        postId
    } getOrElse {
      throw new IllegalArgumentException(s"The thread [$threadId] was not found")
    }
  }

  def findThread(threadId: Int): Option[BbsThread] = db withSession { implicit session =>
    bbsThreads.filter(_.id === threadId).firstOption
  }

  def getPosts(threadId: Int): List[BbsPost] = db withSession { implicit session =>
    bbsPosts.filter(_.threadId === threadId).filter(_.visible).sortBy(_.id.asc).list
  }
}
