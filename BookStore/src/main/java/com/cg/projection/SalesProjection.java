package com.cg.projection;

import com.cg.entity.Sales;
import org.springframework.data.rest.core.config.Projection;
import java.time.LocalDateTime;

// What a non-tech user cares about for a sale:
// Which order? When? How many? What payment terms?
// storId and titleId are internal FK keys — not shown
// ordNum IS shown because it's a real-world order reference number
@Projection(name = "salesView", types = { Sales.class })
public interface SalesProjection {
    String getOrdNum();           // "QA7442.3" — actual order reference
    LocalDateTime getOrdDate();   // When the order was placed
    Short getQty();               // How many books ordered
    String getPayterms();         // "Net 30" — payment terms
}
