package com.cg.projection;

import com.cg.entity.Store;
import org.springframework.data.rest.core.config.Projection;

// What a non-tech user cares about when browsing stores:
// Where is the store? What is it called?
// storId is an internal DB key — user does not need to see it
@Projection(name = "storeView", types = { Store.class })
public interface StoreProjection {
    String getStorName();       // "Eric the Read Books"
    String getStorAddress();    // "788 Catamaugus Ave"
    String getCity();           // "Seattle"
    String getState();          // "WA"
    String getZip();            // "98056"
}
