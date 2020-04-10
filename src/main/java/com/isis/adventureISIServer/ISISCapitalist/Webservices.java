/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.isis.adventureISIServer.ISISCapitalist;

import java.io.FileNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Netto Léa
 */
@Path("generic")
public class Webservices {

    Services services;

    public Webservices() {
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getXML(@Context HttpServletRequest request) throws JAXBException, FileNotFoundException {
        String username = request.getHeader("X-user");
        World world = services.getWorld(username);
                System.out.println(username);
       // services.saveWorldToXml(world, username);
        return Response.ok(world).build();
    }

    @PUT
    @Path("product")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void putProduct(@Context HttpServletRequest request, ProductType newproduct) throws JAXBException, FileNotFoundException, Exception {
        System.out.println("modification");
        String username = request.getHeader("X-user");
        services.updateProduct(username, newproduct);
    }

    @PUT
    @Path("manager")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void putManager(@Context HttpServletRequest request, PallierType newmanager) throws JAXBException, FileNotFoundException {
        String username = request.getHeader("X-user");
        services.updateManager(username, newmanager);
    }

    @PUT
    @Path("upgrade")
    //@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void putUpgrade(@Context HttpServletRequest request, PallierType upgrade) throws JAXBException, FileNotFoundException {
        String username = request.getHeader("X-user");
        services.updateUpgrade(username, upgrade);
    }

    @DELETE
    @Path("world")
    public void deleteWorld(@Context HttpServletRequest request) throws JAXBException, FileNotFoundException {
        String username = request.getHeader("X-user");
        services.deleteWorld(username);

    }

    @PUT
    @Path("angelUpgrade")
    //@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void putAngelUpgrade(@Context HttpServletRequest request, PallierType angelUpgrade) throws JAXBException, FileNotFoundException {
        String username = request.getHeader("X-user");
        services.angelUpgrade(username, angelUpgrade);
    }
}
