package com.ifrn.joaomanoelfontes.helpdesk.service;

import com.ifrn.joaomanoelfontes.helpdesk.exception.InvalidRequestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

final class PaginationSupport {

    private PaginationSupport() {
    }

    static Pageable normalize(Pageable pageable, Map<String, String> allowedProperties, String defaultProperty) {
        if (pageable.getPageNumber() < 0) {
            throw new InvalidRequestException("A página não pode ser negativa");
        }
        int size = Math.min(Math.max(pageable.getPageSize(), 1), 50);
        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String property = allowedProperties.get(order.getProperty());
            if (property == null) {
                throw new InvalidRequestException("Campo de ordenação inválido: " + order.getProperty());
            }
            orders.add(new Sort.Order(order.getDirection(), property));
        }
        Sort sort = orders.isEmpty() ? Sort.by(defaultProperty).ascending() : Sort.by(orders);
        return PageRequest.of(pageable.getPageNumber(), size, sort);
    }
}
