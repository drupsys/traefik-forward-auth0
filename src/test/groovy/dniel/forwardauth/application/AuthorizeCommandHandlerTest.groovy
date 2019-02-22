package dniel.forwardauth.application

import dniel.forwardauth.ObjectMother
import dniel.forwardauth.domain.Token
import dniel.forwardauth.domain.service.NonceGeneratorService
import dniel.forwardauth.domain.service.VerifyTokenService
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.is
import static spock.util.matcher.HamcrestSupport.that

class AuthorizeCommandHandlerTest extends Specification {

    @Unroll
    def "should verify access to #host#uri based on input parameters"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeCommandHandler.AuthorizeCommand(
                jwt,
                jwt,
                protocol,
                host,
                uri,
                method)


        and: "a stub VerifyTokenService that return a valid JWT Token"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(
                _,
                _,
                _) >> new Token(ObjectMother.jwtToken)

        and: "a command handler that is the system under test"
        AuthorizeCommandHandler sut = new AuthorizeCommandHandler(
                ObjectMother.properties, verifyTokenService, new NonceGeneratorService())

        when: "we authorize the request"
        def result = sut.perform(command)

        then: "we should get a valid response"
        that(result.authenticated, is(authenticated))
        that(result.isRestrictedUrl, is(restricted))

        where:
        jwt                         | protocol | host               | uri              | method  | authenticated | restricted
        ObjectMother.jwtTokenString | "HTTPS"  | "www.example.test" | "/test"          | "GET"  || true          | true
        ObjectMother.jwtTokenString | "HTTPS"  | "www.example.test" | "/oauth2/signin" | "GET"  || true          | false
        ObjectMother.jwtTokenString | "HTTPS"  | "www.example.test" | "/OaUth2/SiGNIn" | "GET"  || true          | false
        ObjectMother.jwtTokenString | "HTTPS"  | "opaque.com"       | "/test"          | "GET"  || true          | true
        ObjectMother.jwtTokenString | "HTTPS"  | "restricted.com"   | "/test"          | "GET"  || true          | false
        ObjectMother.jwtTokenString | "HTTPS"  | "restricted.com"   | "/test"          | "POST" || true          | true
        null                        | "HTTPS"  | "www.example.test" | "/test"          | "GET"  || false         | true
        null                        | "HTTPS"  | "www.example.test" | "/test"          | "GeT"  || false         | true
        null                        | "HTTPS"  | "www.example.test" | "/test"          | "GeT"  || false         | true
        null                        | "hTTpS"  | "WwW.ExaMplE.TeST" | "/test"          | "GeT"  || false         | true
    }
}