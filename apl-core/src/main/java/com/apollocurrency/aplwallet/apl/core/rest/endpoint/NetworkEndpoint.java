/*
 * Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.core.rest.endpoint;

import com.apollocurrency.aplwallet.api.dto.MyInfoDTO;
import com.apollocurrency.aplwallet.api.dto.PeerDTO;
import com.apollocurrency.aplwallet.api.response.*;
import com.apollocurrency.aplwallet.apl.core.http.APIProxy;
import com.apollocurrency.aplwallet.apl.core.http.JSONData;
import com.apollocurrency.aplwallet.apl.core.peer.Peer;
import com.apollocurrency.aplwallet.apl.core.peer.Peers;
import com.apollocurrency.aplwallet.apl.core.rest.ApiErrors;
import com.apollocurrency.aplwallet.apl.core.rest.converter.Converter;
import com.apollocurrency.aplwallet.apl.core.rest.service.NetworkService;
import com.apollocurrency.aplwallet.apl.core.rest.utils.ResponseBuilder;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static com.apollocurrency.aplwallet.apl.core.http.JSONResponses.*;

/**
 * Apollo network endpoint
 */

@Path("/networking")
public class NetworkEndpoint {

    @Inject
    private Converter<Peer, PeerDTO> converter;

    @Inject
    private NetworkService service;

    @Path("/myinfo")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Return host information",
            description = "Return the remote host name and address.",
            tags = {"networking"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful execution",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MyInfoDTO.class)))
            })
    public Response getPeer(@Context HttpServletRequest request) {
        ResponseBuilder response = ResponseBuilder.startTiming();
        MyInfoDTO dto = new MyInfoDTO(request.getRemoteHost(), request.getRemoteAddr());
        return response.bind(dto).build();
    }

    @Path("/peer")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Returns peer information",
            description = "Returns peer information by host address.",
            tags = {"networking"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful execution",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PeerDTO.class)))
    })
    public Response getPeer(
            @ApiParam(name = "peer", value = "The certain peer IP address.", type = "string", required = true)
            @QueryParam("peer") String peerAddress ) {

        ResponseBuilder response = ResponseBuilder.startTiming();

        if (peerAddress == null) {
            return response.error( ApiErrors.MISSING_PARAM, "peer").build();
        }

        Peer peer = service.findPeerByAddress(peerAddress);

        if (peer == null) {
            return response.error( ApiErrors.UNKNOWN_VALUE, "peer", peerAddress).build();
        }

        return response.bind(converter.convert(peer)).build();
    }

    @Path("/peer")
    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Add new peer or replace existing.",
            description = "Add new peer or replace existing.",
            method = "POST",
            tags = {"networking"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful execution",
                            content = @Content(mediaType = "text/html",
                                    schema = @Schema(implementation = PeerDTO.class)))
            }
    )
    public Response addOrReplacePeer(
            @RequestBody(content = @Content(
                    schema = @Schema( description = "The certain peer IP address.", required = true)))
            @FormParam("peer") String peerAddress ) {
        ResponseBuilder response = ResponseBuilder.ok();

        if (peerAddress == null) {
            return response.error( ApiErrors.MISSING_PARAM, "peer").build();
        }

        Peer peer = service.findOrCreatePeerByAddress(peerAddress);

        if (peer == null) {
            return response.error( ApiErrors.FAILED_TO_ADD,peerAddress).build();
        }

        boolean isNewlyAdded = service.addPeer(peer, peerAddress);
        PeerDTO dto = converter.convert(peer);
        dto.setIsNewlyAdded(isNewlyAdded);

        return response.bind(dto).build();
    }

    @Path("/peer/all")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Returns peers list",
            description = "Returns all peers list by supplied parameters.",
            tags = {"networking"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful execution",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetPeersResponse.class)))
    })
    public Response getPeersList(
            @ApiParam(value = "include active only peers")
            @QueryParam("active") @DefaultValue("false") Boolean active,
            @ApiParam(value = "include peers in certain state (NON_CONNECTED, CONNECTED, DISCONNECTED)")
            @QueryParam("state") String stateValue,
            @ApiParam(value = "include peer which provides services (HALLMARK, PRUNABLE, API, API_SSL, CORS)")
            @QueryParam("service") List<String> serviceValues,
            @ApiParam(value = "include additional peer information otherwise the host only.")
            @QueryParam("includePeerInfo") @DefaultValue("false") Boolean includePeerInfo ) {

        ResponseBuilder response = ResponseBuilder.startTiming();

        Peer.State state;
        if (stateValue != null) {
            try {
                state = Peer.State.valueOf(stateValue);
            } catch (RuntimeException exc) {
                return response.error(ApiErrors.INCORRECT_VALUE, "state", stateValue).build();
            }
        } else {
            state = null;
        }

        long serviceCodes = 0;
        if (serviceValues != null) {
            for (String serviceValue : serviceValues) {
                try {
                    serviceCodes |= Peer.Service.valueOf(serviceValue).getCode();
                } catch (RuntimeException exc) {
                    return response.error(ApiErrors.INCORRECT_VALUE, "service", serviceValue).build();
                }
            }
        }

        List<Peer> peers = service.getPeersByStateAndService(active, state, serviceCodes);

        return response.bind(mapResponse(peers, includePeerInfo)).build();
    }

    @Path("/peer/inbound")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Returns inbound peers list.",
            description = "Returns a list of inbound peers." +
                    " An inbound peer is a peer that has sent a request to this peer " +
                    "within the previous 30 minutes.",
            tags = {"networking"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful execution",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetPeersResponse.class)))
    })
    public Response getInboundPeersList(
            @Parameter(description = "include additional peer information otherwise the host only.")
            @QueryParam("includePeerInfo") @DefaultValue("false") Boolean includePeerInfo ) {

        ResponseBuilder response = ResponseBuilder.startTiming();

        List<Peer> peers = service.getInboundPeers();

        return response.bind(mapResponse(peers, includePeerInfo)).build();
    }

    @Path("/peer/blacklist")
    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Add peer in the black list.",
            description = "Add peer in the black list.",
            method = "POST",
            tags = {"networking"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful execution",
                            content = @Content(mediaType = "text/html",
                                    schema = @Schema(implementation = ResponseDone.class)))
            }
    )
    public Response addPeerInBlackList(
            @RequestBody(content = @Content(
                    schema = @Schema( description = "The certain peer IP address.", required = true)))
            @FormParam("peer") String peerAddress ) {
        ResponseBuilder response = ResponseBuilder.done();

        if (peerAddress == null) {
            return response.error( ApiErrors.MISSING_PARAM, "peer").build();
        }

        Peer peer = service.putPeerInBlackList(peerAddress);

        if (peer == null) {
            return response.error( ApiErrors.UNKNOWN_VALUE, "peer", peerAddress).build();
        }

        return response.build();
    }

    @Path("/peer/proxyblacklist")
    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Add api proxy peer in the black list.",
            description = "Add api proxy peer in the black list.",
            method = "POST",
            tags = {"networking"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful execution",
                            content = @Content(mediaType = "text/html",
                                    schema = @Schema(implementation = ResponseDone.class)))
            }
    )
    public Response addAPIProxyPeerInBlackList(
            @RequestBody(content = @Content(
                    schema = @Schema( description = "The certain peer IP address.", required = true)))
            @FormParam("peer") String peerAddress ) {
        ResponseBuilder response = ResponseBuilder.startTiming();

        if (peerAddress == null) {
            return response.error( ApiErrors.MISSING_PARAM, "peer").build();
        }

        Peer peer = service.findOrCreatePeerByAddress(peerAddress);

        if (peer == null) {
            return response.error( ApiErrors.UNKNOWN_VALUE, "peer", peerAddress).build();
        }

        ResponseDone body = new ResponseDone(service.putAPIProxyPeerInBlackList(peer));

        return response.bind(body).build();
    }

    @Path("/peer/setproxy")
    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Set peer as a proxy.",
            description = "Set peer as a proxy.",
            method = "POST",
            tags = {"networking"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful execution",
                            content = @Content(mediaType = "text/html",
                                    schema = @Schema(implementation = PeerDTO.class)))
            }
    )
    public Response setAPIProxyPeer(
            @RequestBody(content = @Content(
                    schema = @Schema( description = "The certain peer IP address.", required = true)))
            @FormParam("peer") String peerAddress ) {
        ResponseBuilder response = ResponseBuilder.startTiming();
        Peer peer;
        if (peerAddress == null) {
            peer = service.setForcedPeer(null);
            if ( peer == null ) {
                return response.error(ApiErrors.MISSING_PARAM, "peer").build();
            }
        }else {
            peer = service.findPeerByAddress(peerAddress);

            if (peer == null) {
                return response.error(ApiErrors.UNKNOWN_VALUE, "peer", peerAddress).build();
            }

            if (peer.getState() != Peer.State.CONNECTED) {
                return response.error(ApiErrors.PEER_NOT_CONNECTED).build();
            }
            if (!peer.isOpenAPI()) {
                return response.error(ApiErrors.PEER_NOT_OPEN_API).build();
            }

            service.setForcedPeer(peer);
        }

        return response.bind(converter.convert(peer)).build();
    }

    private ResponseBase mapResponse(List<Peer> peers, boolean includePeerInfo){
        if (includePeerInfo){
            GetPeersResponse peersResponse = new GetPeersResponse();
            peersResponse.setPeers(converter.convert(peers));
            return peersResponse;
        }else{
            GetPeersSimpleResponse peersSimpleResponse = new GetPeersSimpleResponse();
            List<String> hosts = peers.stream().map(Peer::getHost).collect(Collectors.toList());
            peersSimpleResponse.setPeers(hosts);
            return peersSimpleResponse;
        }
    }

}