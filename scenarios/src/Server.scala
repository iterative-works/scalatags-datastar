// PURPOSE: Runnable entrypoint for the Datastar scenarios app — serves every example at once.
// PURPOSE: `./mill scenarios.run` (set PORT to override 8080), then open http://localhost:8080.
package works.iterative.scalatags.datastar.scenarios

import zio.*

/** Boots the scenarios server and runs until interrupted. Serves the union of every example's
  * endpoints, reading the port from `PORT` (default 8080).
  */
object Server extends ZIOAppDefault:

    def run: ZIO[Any, Throwable, Unit] =
        for
            port <- System.env("PORT").map(_.flatMap(_.toIntOption).getOrElse(8080))
            _ <- ZIO.scoped:
                for
                    _ <- HttpServer.serve(Scenarios.endpoints, port)
                    _ <- ZIO.logInfo(s"Gallery at http://localhost:$port/")
                    _ <- ZIO.logInfo(s"  counter:     http://localhost:$port/examples/counter")
                    _ <- ZIO.logInfo(s"  live search: http://localhost:$port/examples/search")
                    _ <- ZIO.never
                yield ()
        yield ()

end Server
