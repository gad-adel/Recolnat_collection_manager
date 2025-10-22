package org.recolnat.collection.manager.web.dto;

import java.util.List;

public record MidsDTO(Integer level, List<List<String>> requiredFields) {
}
