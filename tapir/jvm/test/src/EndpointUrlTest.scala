// PURPOSE: Verifies Tapir endpoints reverse-route to the exact relative URLs Datastar actions need.
// PURPOSE: Pins the wire format (leading slash, path params, query encoding) found in the Phase 3 spike.
package works.iterative.scalatags.datastar.tapir

import sttp.tapir.*
import utest.*

import EndpointUrl.urlOf

object EndpointUrlTest extends TestSuite:

    val tests = Tests {

        test("fixed path, no input") {
            val ep = endpoint.get.in("health")
            assert(urlOf(ep)(()) == "/health")
        }

        test("nested fixed path") {
            val ep = endpoint.get.in("api" / "v1" / "status")
            assert(urlOf(ep)(()) == "/api/v1/status")
        }

        test("single path param") {
            val ep = endpoint.get.in("users" / path[Int]("id"))
            assert(urlOf(ep)(42) == "/users/42")
        }

        test("path param between fixed segments") {
            val ep = endpoint.get.in("users" / path[Int]("id") / "posts")
            assert(urlOf(ep)(42) == "/users/42/posts")
        }

        test("single query param") {
            val ep = endpoint.get.in("search").in(query[String]("q"))
            assert(urlOf(ep)("hello") == "/search?q=hello")
        }

        test("query values use form-style encoding that round-trips") {
            val ep = endpoint.get.in("search").in(query[String]("q"))
            // sttp encodes query values application/x-www-form-urlencoded style:
            // space -> '+', literal '+' -> %2B, '&' -> %26, UTF-8 percent-encoded.
            // Tapir's server-side codec decodes these back, so values round-trip.
            assert(urlOf(ep)("hello world") == "/search?q=hello+world")
            assert(urlOf(ep)("a&b") == "/search?q=a%26b")
            assert(urlOf(ep)("a+b") == "/search?q=a%2Bb")
            assert(urlOf(ep)("café") == "/search?q=caf%C3%A9")
        }

        test("apostrophe is a valid sub-delim and stays unencoded") {
            // RFC 3986 lists ' as a sub-delim, so sttp leaves it raw in both path and query.
            // The Datastar action layer must therefore escape it before embedding in @get('...').
            val qep = endpoint.get.in("search").in(query[String]("q"))
            assert(urlOf(qep)("it's") == "/search?q=it's")
            val pep = endpoint.get.in("u" / path[String]("s"))
            assert(urlOf(pep)("o'brien") == "/u/o'brien")
        }

        test("other literal-breaking characters are percent-encoded") {
            // The action layer escapes only the apostrophe; this pins that every other character
            // which could break a single-quoted JS string is encoded by the router instead.
            val pep = endpoint.get.in("u" / path[String]("s"))
            assert(urlOf(pep)("a\\b") == "/u/a%5Cb") // backslash
            assert(urlOf(pep)("a\"b") == "/u/a%22b") // double quote
            assert(urlOf(pep)("a\nb") == "/u/a%0Ab") // newline
        }

        test("path param plus query param") {
            val ep = endpoint.get.in("users" / path[Int]("id")).in(query[Boolean]("active"))
            assert(urlOf(ep)((42, true)) == "/users/42?active=true")
        }

        test("optional query param absent") {
            val ep = endpoint.get.in("search").in(query[Option[String]]("q"))
            assert(urlOf(ep)(None) == "/search")
        }

        test("optional query param present") {
            val ep = endpoint.get.in("search").in(query[Option[String]]("q"))
            assert(urlOf(ep)(Some("x")) == "/search?q=x")
        }

        test("method is independent of url") {
            val ep = endpoint.post.in("save")
            assert(urlOf(ep)(()) == "/save")
        }
    }

end EndpointUrlTest
