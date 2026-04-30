package com.cg.projection;
import com.cg.entity.TitleAuthor    ;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "titleDetail", types = { TitleAuthor.class })
public interface TitleAuthorProjection {

    String getAuId();
    String getTitleId();
    Integer getAuOrd();
    Integer getRoyaltyper();

    // These call into the Title object inline — no extra request needed
    TitleSummary getTitle();

    interface TitleSummary {
        String getTitle();
        String getType();
        Integer getRoyalty();
    }
}