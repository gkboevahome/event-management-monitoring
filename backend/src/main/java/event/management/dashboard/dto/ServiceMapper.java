package event.management.dashboard.dto;

import event.management.dashboard.persistence.model.ServiceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceMapper {

    ServiceDTO toDTO(ServiceEntity service);
    ServiceEntity toEntity(ServiceDTO userDTO);

}
