name := "car-adverts"
 
version := "1.0" 
      
lazy val `car-adverts` = (project in file(".")).enablePlugins(PlayJava)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
scalaVersion := "2.11.11"

libraryDependencies ++= Seq( javaJdbc , cache , javaWs, "com.amazonaws" % "aws-java-sdk" % "1.11.221", "junit" % "junit" % "4.12" % "test" )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      