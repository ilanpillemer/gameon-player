/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.gameon.player;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.ektorp.CouchDbConnector;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/")
@Api( value = "players")
public class PlayersResource {

    @Inject
    protected CouchDbConnector db;

    @GET
    @ApiOperation(value = "Ping",
    notes = "")
    public Response basicGet() {
        return Response.ok().build();
    }

    /**
     * GET /players/v1/health
     */
    @GET
    @Path("health")
    @io.swagger.annotations.ApiOperation(value = "Check application health",
        notes = "")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful"),
            @ApiResponse(code = 503, message = "Service unavailable")
        })
    public Response healthCheck() {
//        if ( mapRepository != null && mapRepository.connectionReady() ) {
//            return Response.ok().build();
//        } else {
//            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
//        }
        return Response.ok().build();
    }

    /**
     * GET /players/v1/info
     */
    @GET
    @Path("info")
    @ApiOperation(value = "Application information",
        notes = "")
    public Response info() {
//        if ( mapRepository != null && mapRepository.connectionReady() ) {
//            return Response.ok().build();
//        } else {
//            return Response.serverError().build();
//        }
        return Response.ok().build();
    }
}
