import com.typesafe.sbt.SbtStartScript 

name := "LiftDrill"

version := "1.0"

organization := "net.liftweb"

scalaVersion := "2.10.2"


resolvers ++= 
	Seq(
		"snapshots"			at "http://oss.sonatype.org/content/repositories/snapshots",
		"releases"          at "http://oss.sonatype.org/content/repositories/releases",
		"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
	)

seq(com.github.siasia.WebPlugin.webSettings :_*)

seq(SbtStartScript.startScriptForClassesSettings: _*)

// net.virtualvoid.sbt.graph.Plugin.graphSettings // for sbt dependency-graph plugin

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-language:implicitConversions", "-language:postfixOps")


libraryDependencies ++= {
  val liftVersion = "2.5"
  Seq(
    "net.liftweb"       %% "lift-webkit"        % liftVersion        %     "compile"  withSources () ,
    "net.liftweb"       %% "lift-wizard"        % liftVersion        %     "compile"  withSources () ,
    "net.liftweb"       %% "lift-mapper"        % liftVersion       %     "compile"  withSources () ,
    "net.liftweb"        %% "lift-mongodb-record"    % liftVersion   %     "compile"  withSources () ,
    "net.liftweb"        %% "lift-mongodb"    % liftVersion   %     "compile"  withSources () ,
    "net.liftweb"        %% "lift-record"    % liftVersion   %     "compile"  withSources () ,
    "net.liftweb"        %% "lift-util"    % liftVersion   %     "compile"  withSources () ,
    "net.liftmodules"   %% "textile_2.5"        % "1.3"      % "compile",
    "net.liftmodules"   %% "widgets_2.5"        % "1.3"      % "compile"   withSources (),
    "org.eclipse.jetty" % "jetty-webapp" % "8.1.14.v20131031" % "compile,container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"    % "logback-classic"     % "1.0.11",
    "com.h2database"    % "h2"     % "1.3.171",
    "org.jsoup"  %  "jsoup"  %  "1.7.3",
    "org.apache.lucene" % "lucene-smartcn" % "3.4.0",
    "org.apache.lucene" % "lucene-highlighter" % "3.4.0",
    "junit" % "junit" % "4.4",
    "com.typesafe.akka" % "akka-actor" % "2.0.1"
  )
}

port in container.Configuration := 80
