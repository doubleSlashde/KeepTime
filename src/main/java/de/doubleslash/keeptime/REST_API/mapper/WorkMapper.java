package de.doubleslash.keeptime.REST_API.mapper;


import de.doubleslash.keeptime.REST_API.DTO.WorkDTO;
import de.doubleslash.keeptime.model.Work;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WorkMapper {

   WorkMapper INSTANCE = Mappers.getMapper(WorkMapper.class);

   WorkDTO workToWorkDTO(Work work);

   Work workDTOToWork(WorkDTO workDTO);
}


