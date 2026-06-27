package resource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.Coffee;
import util.JpaUtil;

import java.util.List;

@Path("/coffees")
public class CoffeeResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Coffee> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Coffee> q = em.createNamedQuery("Coffee.findAll", Coffee.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByName(@PathParam("name") String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Coffee c = em.find(Coffee.class, name);
            if (c == null) {
                return Response.status(404).build();
            }
            return Response.ok(c).build();
        } finally {
            em.close();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Coffee coffee) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(coffee);
            em.getTransaction().commit();
            return Response.status(201).build();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @PUT
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("name") String name, Coffee coffee) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Coffee existing = em.find(Coffee.class, name);
            if (existing == null) {
                return Response.status(404).build();
            }
            em.getTransaction().begin();
            existing.setPrice(coffee.getPrice());
            existing.setSales(coffee.getSales());
            if (coffee.getSupplier() != null) {
                existing.setSupplier(coffee.getSupplier());
            }
            existing.setTotal(coffee.getTotal());
            em.getTransaction().commit();
            return Response.ok().build();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @DELETE
    @Path("/{name}")
    public Response delete(@PathParam("name") String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Coffee c = em.find(Coffee.class, name);
            if (c == null) {
                return Response.status(404).build();
            }
            em.getTransaction().begin();
            em.remove(c);
            em.getTransaction().commit();
            return Response.noContent().build();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
