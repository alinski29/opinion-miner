package thesis.operations

import java.util.Properties

import com.jcraft.jsch.JSch
import com.mongodb.{BasicDBObject, DBCollection}
import com.mongodb.casbah.Imports.{MongoClient, MongoDB, ObjectId, ReadPreference}
import play.api.libs.json.{JsSuccess, JsValue, Json}

import scala.util.{Success, Try}

object DBOperations {

  def updateCollection(id: ObjectId, field: String, value: Any, collection: DBCollection ) = {
    val q = new BasicDBObject("_id", id)
    val reviewObj = new BasicDBObject()
    reviewObj.put(field, value)
    collection.update(q, new BasicDBObject("$set", reviewObj))
  }

  object DBConnection {

    private case class Configuration(database: String,
                             collection: String = "yelp",
                             host: String,
                             port: Int = 27017,
                             local_host: String = "127.0.0.1",
                             local_port: Int = 27017,
                             ssh_user: String,
                             ssh_port: Int,
                             ssh_password: String)

    private implicit val configReads = Json.reads[Configuration]
    private val configuration: Option[Configuration] = DBConnection.readFromFile("src/main/resources/conf/DBconf.json")

    private def readFromFile(filePath: String): Option[Configuration] = {
      Try(scala.io.Source.fromFile(filePath).getLines() mkString "") match {
        case Success(jsonString: String) => readFromText(jsonString)
        case _ => None
      }
    }

    private def readFromText(jsonString: String): Option[Configuration] = {
      val json: JsValue = Json.parse(jsonString)
      json.validate[Configuration] match {
        case JsSuccess(readConfig: Configuration, _) => Some(readConfig)
        case _ => None
      }
    }

    def getConnection(): MongoDB = {
      configuration match {
        case Some(config: Configuration) => {
          val database = config.database
          val collection = config.collection
          val host = config.host
          val port = config.port
          val local_host = config.local_host
          val local_port = config.local_port
          val ssh_user = config.ssh_user
          val ssh_password = config.ssh_password
          val ssh_port = config.ssh_port
          println(s"$database, $collection, $host, $port, $local_host, $local_port")
          val props = new Properties()
          props.put("StrictHostKeyChecking", "no")
          val jsch = new JSch()
          val ssh_session = jsch.getSession(ssh_user, host, ssh_port)
          ssh_session.setPassword(ssh_password)
          ssh_session.setConfig(props)
          ssh_session.connect()
          ssh_session.setPortForwardingL(local_port, host, port)
          val db = MongoClient(local_host, local_port)(database)
          db.setReadPreference(ReadPreference.Nearest)
          db
        }
        case _ => throw new Error(
          """
            | Error reading the configuration input. Please provide different
            | input.
          """.stripMargin)
      }
    }
  }

}

