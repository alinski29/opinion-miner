package mongo

import com.mongodb.casbah.Imports._

case class MongoFactory(val host: String, val port: Int) {
  def this() = this("localhost", 27017)
  val db = MongoClient(host, port)("yelp")
}






