package com.example.jaxrs.day2;

import com.example.day2.dao.ItemDao;
import com.example.day2.dto.ItemSearchCriteria;
import com.example.day2.entity.Item;
import com.example.jaxrs.config.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

@Path("/day2/search")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SearchResource {

    private ItemDao itemDao = new ItemDao();

    @GET
    @JsonView(Views.List.class)
    public List<Item> search(@QueryParam("name") String name,
                             @QueryParam("minPrice") Double minPrice,
                             @QueryParam("maxPrice") Double maxPrice,
                             @QueryParam("categoryId") Long categoryId,
                             @QueryParam("active") Boolean active,
                             @QueryParam("tagNames") String tagNamesCsv) {
        ItemSearchCriteria criteria = new ItemSearchCriteria();
        criteria.setName(name);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setCategoryId(categoryId);
        criteria.setActive(active);
        if (tagNamesCsv != null && !tagNamesCsv.isEmpty()) {
            criteria.setTagNames(Arrays.asList(tagNamesCsv.split(",")));
        }
        return itemDao.findByCriteria(criteria);
    }
}
