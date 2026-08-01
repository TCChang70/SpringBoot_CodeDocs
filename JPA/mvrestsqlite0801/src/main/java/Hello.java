import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class Hello {
    @GET
    @Produces(MediaType.TEXT_HTML)
	public String sayHello() {
		return "<h1>Hello World</h1>";
	}
}
