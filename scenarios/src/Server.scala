// PURPOSE: Runnable entrypoint for the Datastar scenarios app — starts the counter server.
// PURPOSE: `./mill scenarios.run` (set PORT to override 8080), then open http://localhost:8080.
package works.iterative.scalatags.datastar.scenarios

import zio.*

/** Boots the scenarios server and runs until interrupted. Reads the port from `PORT` (default
  * 8080).
  */
object Server extends ZIOAppDefault:

    def run: ZIO[Any, Throwable, Unit] =
        for
            port <- System.env("PORT").map(_.flatMap(_.toIntOption).getOrElse(8080))
            _ <- ZIO.scoped:
                for
                    _ <- CounterServer.serve(port)
                    _ <- ZIO.logInfo(s"Counter scenario at http://localhost:$port")
                    _ <- ZIO.never
                yield ()
        yield ()

end Server
