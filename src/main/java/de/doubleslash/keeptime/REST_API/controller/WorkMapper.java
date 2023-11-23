package de.doubleslash.keeptime.REST_API.controller;


import de.doubleslash.keeptime.model.Work;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WorkMapper {

   WorkMapper INSTANCE = Mappers.getMapper(WorkMapper.class);

   WorkDTO workToWorkDTO(Work work);

   Work workDTOToWork(WorkDTO workDTO);
}


