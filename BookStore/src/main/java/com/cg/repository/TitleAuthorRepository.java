package com.cg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.cg.entity.TitleAuthor;
import com.cg.entity.TitleAuthorId;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "titleauthors", path = "titleauthors")
public interface TitleAuthorRepository extends JpaRepository<TitleAuthor, TitleAuthorId> {

    List<TitleAuthor> findByAuId(String auId);

    List<TitleAuthor> findByTitleId(String titleId);

}
