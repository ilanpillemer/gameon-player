package net.wasdev.gameon.player;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("health")
public class Health {

    /**
     * GET /map/v1/health
     */
    @GET
    public Response healthCheck() {
            return Response.ok().build();
    }

}
