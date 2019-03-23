package db

import java.sql._
import java.net.InetAddress
import com.jcraft.jsch._

// This class will hold all the logic that connects to the database and executes our queries
// We aren't using encryption for our password in the app, because we aren't storing it anywhere
class DatabaseInterface(val password: String) {
  val connection: Connection = establishConnection


  private def establishConnection: Connection = {
    val lport = 5432
    val rhost = "localhost"
    val rport = 5432

    val username = "cs421g51"

    try {
      val jsch = new JSch()
      val session = jsch.getSession(username, "comp421.cs.mcgill.ca", 22)
      session.setPassword(password)
      session.setConfig("StrictHostKeyChecking", "no")
      session.connect()
      session.setPortForwardingL(lport, rhost, rport)
    } catch {
      case e: Exception => e.printStackTrace()
    }

    // Register the driver.  You must register the driver before you can use it.
    try {
      DriverManager.registerDriver (new org.postgresql.Driver())
    } catch{
      case cnfe: ClassNotFoundException => println("Class not found")
    }

    val url: String = s"jdbc:postgresql://$rhost:$lport/cs421"
    DriverManager.getConnection(url, username, password)
  }
}
