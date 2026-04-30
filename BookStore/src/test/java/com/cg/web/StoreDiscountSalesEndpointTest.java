package com.cg.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Store, Discount, and Sales Spring Data REST endpoints")
class StoreDiscountSalesEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllStores_returnsHalCollection() throws Exception {
        mockMvc.perform(get("/api/stores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.stores", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getStoreById_returnsSeededStore() throws Exception {
        mockMvc.perform(get("/api/stores/{storId}", "7066"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storName", is("Barnum's")))
                .andExpect(jsonPath("$.city", is("Tustin")))
                .andExpect(jsonPath("$.state", is("CA")));
    }

    @Test
    void getStoreById_missingStore_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/stores/{storId}", "ZZZZ"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchStoresByCity_noResults_returnsOk() throws Exception {
        mockMvc.perform(get("/api/stores/search/findByCity").param("city", "Tokyo"))
                .andExpect(status().isOk());
    }

    @Test
    void searchStoresByState_returnsSeededWashingtonStores() throws Exception {
        mockMvc.perform(get("/api/stores/search/findByState").param("state", "WA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.stores", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$._embedded.stores[0].state", is("WA")))
                .andExpect(jsonPath("$._embedded.stores[1].state", is("WA")));
    }

    @Test
    void getAllDiscounts_returnsHalCollection() throws Exception {
        mockMvc.perform(get("/api/discounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.discounts", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void searchDiscountByType_returnsSeededCustomerDiscount() throws Exception {
        mockMvc.perform(get("/api/discounts/search/findByDiscounttype")
                        .param("discounttype", "Customer Discount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.discounts", hasSize(1)))
                .andExpect(jsonPath("$._embedded.discounts[0].discounttype", is("Customer Discount")))
                .andExpect(jsonPath("$._embedded.discounts[0].discount", is(5.0)));
    }

    @Test
    void searchDiscountByStore_noResults_returnsOk() throws Exception {
        mockMvc.perform(get("/api/discounts/search/findByStorId").param("storId", "9999"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllSales_returnsHalCollection() throws Exception {
        mockMvc.perform(get("/api/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.sales", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void searchSalesByTitleId_returnsSeededSales() throws Exception {
        mockMvc.perform(get("/api/sales/search/findByTitleId").param("titleId", "PS2091"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.sales", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$._embedded.sales[0].ordNum", notNullValue()));
    }

    @Test
    void searchSalesByStoreId_noResults_returnsOk() throws Exception {
        mockMvc.perform(get("/api/sales/search/findByStorId").param("storId", "9999"))
                .andExpect(status().isOk());
    }

    @Test
    void searchSalesByStoreId_returnsSeededStoreSales() throws Exception {
        mockMvc.perform(get("/api/sales/search/findByStorId").param("storId", "7131"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.sales", hasSize(greaterThanOrEqualTo(6))))
                .andExpect(jsonPath("$._embedded.sales[0].ordNum", notNullValue()));
    }

    @Test
    void deleteStoreEndpoint_isNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/stores/{storId}", "7066"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void deleteDiscountEndpoint_isNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/discounts/{discounttype}", "Customer Discount"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void deleteSalesCollectionEndpoint_isClientError() throws Exception {
        mockMvc.perform(delete("/api/sales"))
                .andExpect(status().is4xxClientError());
    }
}
