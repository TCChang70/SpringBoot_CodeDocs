package com.example.jaxrs.day3;

import com.example.day3.dao.SysConfigDao;
import com.example.day3.entity.SysConfig;
import com.example.jaxrs.config.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/day3/configs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigResource {

    private SysConfigDao configDao = new SysConfigDao();

    @GET
    @JsonView(Views.List.class)
    public List<SysConfig> getAll() {
        return configDao.findAll();
    }

    @GET
    @Path("/{id}")
    @JsonView(Views.Detail.class)
    public Response getById(@PathParam("id") Long id) {
        SysConfig config = configDao.findAll().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst().orElse(null);
        if (config == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Config not found\"}").build();
        }
        return Response.ok(config).build();
    }

    @GET
    @Path("/key/{key}")
    @JsonView(Views.Detail.class)
    public Response getByKey(@PathParam("key") String key) {
        SysConfig config = configDao.findByKey(key);
        if (config == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Config key not found\"}").build();
        }
        return Response.ok(config).build();
    }

    @GET
    @Path("/cache-demo")
    public String cacheDemo() {
        String key = "cache.demo.key";
        SysConfig existing = configDao.findByKey(key);
        if (existing == null) {
            SysConfig config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue("cache-demo-value");
            config.setDescription("Cache demo entry");
            configDao.create(config);
        }
        long start1 = System.nanoTime();
        configDao.findByKeyWithCache(key);
        long end1 = System.nanoTime();
        long duration1 = (end1 - start1) / 1000;

        long start2 = System.nanoTime();
        configDao.findByKeyWithCache(key);
        long end2 = System.nanoTime();
        long duration2 = (end2 - start2) / 1000;

        return "{\"firstCallMicros\":" + duration1 + ",\"secondCallMicros\":" + duration2
                + ",\"cachedResult\":\"" + (duration2 < duration1 ? "likely cached" : "not cached") + "\"}";
    }

    @POST
    @JsonView(Views.Detail.class)
    public Response create(SysConfig config) {
        configDao.create(config);
        return Response.status(Response.Status.CREATED).entity(config).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Map<String, Object> body) {
        String newValue = (String) body.get("configValue");
        configDao.updateValue(id, newValue);
        return Response.ok("{\"success\":true,\"id\":" + id + "}").build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        configDao.delete(id);
        return Response.ok("{\"success\":true}").build();
    }
}
