package uk.gov.digital.ho.hocs.lists;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.lists.model.DataList;
import uk.gov.digital.ho.hocs.lists.model.DataListEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class DataListService {
    private final DataListRepository repo;

    @Autowired
    public DataListService(DataListRepository repo) {
        this.repo = repo;
    }

    @Cacheable(value = "list", key = "#name")
    public DataList getDataListByName(String name) throws EntityNotFoundException {
       DataList list = repo.findOneByNameAndDeletedIsFalse(name);
        if(list == null) {
            throw new EntityNotFoundException();
        }
        return list;
    }

    @Cacheable(value = "list")
    public Set<DataList> getAllDataLists() {
        return repo.findAllByDeletedIsFalse();
    }

    @CacheEvict(value = "list", allEntries = true)
    public void updateDataList(DataList newDataList) {
        if(newDataList != null && newDataList.getName() != null && newDataList.getEntities() != null) {
            DataList jpaDataList = repo.findOneByName(newDataList.getName());

            // Update existing list
            if (jpaDataList != null) {
                List<DataListEntity> newEntities = new ArrayList<>(newDataList.getEntities());
                Set<DataListEntity> jpaEntities = jpaDataList.getEntities();

                jpaEntities.forEach(item -> item.setDeleted(!newEntities.contains(item)));

                // Add new list items
                newEntities.forEach(newTopic -> {
                    if (!jpaEntities.contains(newTopic)) {
                        jpaEntities.add(newTopic);
                    }
                });

                jpaDataList.setEntities(jpaEntities);

                // Set the data list to deleted if there are no visible entities
                jpaDataList.setDeleted(jpaDataList.getEntities().stream().allMatch(DataListEntity::getDeleted));
            } else {
                jpaDataList = newDataList;
            }

            saveList(jpaDataList);
        } else{
            throw new EntityCreationException("Unable to update entity");
        }
    }

    private void saveList(DataList dataList) throws EntityCreationException {
        try {
            if(dataList != null && dataList.getName() != null)
            repo.save(dataList);
        } catch (DataIntegrityViolationException e) {

            if (e.getCause() instanceof ConstraintViolationException &&
                    ((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("list_name_idempotent") ||
                    ((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("entity_name_ref_idempotent")) {
                throw new EntityCreationException("Identified an attempt to recreate existing entity, rolling back");
            }

            throw e;
        }
    }
}