package net.wasdev.gameon.player;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Providers;

import com.mongodb.DB;

@Path("health")
public class Health {
    @Context
    HttpServletRequest httpRequest;

    @Context
    Providers ps;

    @Resource(name = "mongo/playerDB")
    protected DB playerDB;

    @Resource(lookup = "jwtKeyStore")
    String keyStore;

    @Resource(lookup = "jwtKeyStorePassword")
    String keyStorePW;

    @Resource(lookup = "jwtKeyStoreAlias")
    String keyStoreAlias;

    /**
     * GET /play/players/health
     */
    @GET
    public Response healthCheck() {
        if ( playerDB != null 
                && ps != null ) {
            // TODO: More intelligent testing of the DB!
            
            return Response.ok("All is well").build();
        }
        return Response.serverError().build();
    }

}
