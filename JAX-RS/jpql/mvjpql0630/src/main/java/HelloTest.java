import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/test")
public class HelloTest {
   @GET
   @Produces(MediaType.TEXT_HTML)
   public String sayHello() {
	   return "Hello";
   }
}
