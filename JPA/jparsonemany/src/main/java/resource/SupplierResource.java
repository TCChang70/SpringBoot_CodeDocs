package resource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.Coffee;
import model.Supplier;
import util.JpaUtil;

import java.util.List;

@Path("/suppliers")
public class SupplierResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Supplier> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Supplier> q = em.createNamedQuery("Supplier.findAll", Supplier.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Supplier s = em.find(Supplier.class, id);
            if (s == null) {
                return Response.status(404).build();
            }
            return Response.ok(s).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}/coffees")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCoffeesBySupplier(@PathParam("id") int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Coffee> q = em.createQuery(
                "SELECT c FROM Coffee c WHERE c.supplier.supId = :supId", Coffee.class);
            q.setParameter("supId", id);
            List<Coffee> coffees = q.getResultList();
            return Response.ok(coffees).build();
        } finally {
            em.close();
        }
    }
}
