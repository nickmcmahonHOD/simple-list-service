package uk.gov.digital.ho.hocs;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListRepository extends CrudRepository<DataList, Long> {
    DataList findOneByName(String name);
}