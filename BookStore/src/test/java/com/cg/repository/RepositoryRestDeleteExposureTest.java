package com.cg.repository;

import com.cg.entity.Discount;
import com.cg.entity.Sales;
import com.cg.entity.SalesId;
import com.cg.entity.Store;
import org.junit.jupiter.api.Test;
import org.springframework.data.rest.core.annotation.RestResource;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RepositoryRestDeleteExposureTest {

    @Test
    void storeDeleteMethods_areNotExported() throws Exception {
        assertDeleteNotExported(StoreRepository.class, "deleteById", String.class);
        assertDeleteNotExported(StoreRepository.class, "delete", Store.class);
        assertDeleteNotExported(StoreRepository.class, "deleteAllById", Iterable.class);
        assertDeleteNotExported(StoreRepository.class, "deleteAll", Iterable.class);
        assertDeleteNotExported(StoreRepository.class, "deleteAll");
    }

    @Test
    void discountDeleteMethods_areNotExported() throws Exception {
        assertDeleteNotExported(DiscountRepository.class, "deleteById", String.class);
        assertDeleteNotExported(DiscountRepository.class, "delete", Discount.class);
        assertDeleteNotExported(DiscountRepository.class, "deleteAllById", Iterable.class);
        assertDeleteNotExported(DiscountRepository.class, "deleteAll", Iterable.class);
        assertDeleteNotExported(DiscountRepository.class, "deleteAll");
    }

    @Test
    void salesDeleteMethods_areNotExported() throws Exception {
        assertDeleteNotExported(SalesRepository.class, "deleteById", SalesId.class);
        assertDeleteNotExported(SalesRepository.class, "delete", Sales.class);
        assertDeleteNotExported(SalesRepository.class, "deleteAllById", Iterable.class);
        assertDeleteNotExported(SalesRepository.class, "deleteAll", Iterable.class);
        assertDeleteNotExported(SalesRepository.class, "deleteAll");
    }

    private void assertDeleteNotExported(
            Class<?> repositoryType,
            String methodName,
            Class<?>... parameterTypes
    ) throws Exception {
        Method method = repositoryType.getMethod(methodName, parameterTypes);
        RestResource restResource = method.getAnnotation(RestResource.class);

        assertNotNull(restResource);
        assertFalse(restResource.exported());
    }
}
