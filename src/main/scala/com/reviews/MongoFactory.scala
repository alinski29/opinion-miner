package com.reviews

import java.util.Properties

import com.jcraft.jsch.JSch
import com.mongodb.casbah.Imports._
import play.api.libs.json.{JsSuccess, JsValue, Json}

import scala.util.{Success, Try}

object DBConnection {

  implicit val configReads = Json.reads[Configuration]
  private val configuration: Option[Configuration] = DBConnection.readFromFile("src/main/resources/conf.json")

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
        if (ssh_session.isConnected) {
          println("SSH connection established with: " + ssh_session.getHost + ": " + ssh_session.getServerVersion)
        }
        ssh_session.setPortForwardingL(local_port, host, port)

        val db = MongoClient(local_host, local_port)(database)
        db.setReadPreference(ReadPreference.Nearest)
        return db
      }
      case _ => throw new Error(
        """
          | Error reading the configuration input. Please provide different
          | input.
        """.stripMargin)
    }
  }

}





