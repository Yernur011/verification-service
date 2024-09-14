package com.diploma.verivicationdipllom.domain.repository;


import com.diploma.verivicationdipllom.domain.entity.UserRepresent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.UUID;

public interface UserRepresentRepository extends ElasticsearchRepository<UserRepresent, UUID> {
}
