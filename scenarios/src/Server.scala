// PURPOSE: Runnable entrypoint for the Datastar scenarios app — serves every example at once.
// PURPOSE: `./mill scenarios.run` (set PORT to override 8080), then open http://localhost:8080.
package works.iterative.scalatags.datastar.scenarios

import zio.*

/** Boots the scenarios server and runs until interrupted. Serves the union of every example's
  * endpoints, reading the port from `PORT` (default 8080).
  */
object Server extends ZIOAppDefault:

    private val scenarios = CounterServer.serverEndpoints ++ SearchServer.serverEndpoints

    def run: ZIO[Any, Throwable, Unit] =
        for
            port <- System.env("PORT").map(_.flatMap(_.toIntOption).getOrElse(8080))
            _ <- ZIO.scoped:
                for
                    _ <- HttpServer.serve(scenarios, port)
                    _ <- ZIO.logInfo(s"Counter scenario at http://localhost:$port/")
                    _ <- ZIO.logInfo(s"Live-search scenario at http://localhost:$port/search")
                    _ <- ZIO.never
                yield ()
        yield ()

end Server
