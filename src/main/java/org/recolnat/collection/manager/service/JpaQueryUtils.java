package org.recolnat.collection.manager.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.util.Locale;

public class JpaQueryUtils {

    public static Predicate likeIgnoreCase(CriteriaBuilder cb, Path<String> path, String value) {
        return cb.like(cb.upper(path), "%" + value.toUpperCase(Locale.ROOT) + "%");
    }

}
